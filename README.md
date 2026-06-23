# 差旅报销系统后端 (sy-server)

基于 Spring Boot 3 构建的差旅报销管理系统后端服务，提供报销单全生命周期管理、行程追踪、费用核算与分摊等核心能力。

> 前端项目位于 `frontend/sy-ui`（基于 Vue 3 + TypeScript），当前尚未与后端完整联调，本文档仅聚焦后端实现。

---

## 目录

- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [核心特性](#核心特性)
- [数据库模型](#数据库模型)
- [快速开始](#快速开始)
- [业务流程](#业务流程)

---

## 技术栈

| 类别 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 语言 | Java | 17 | LTS 版本 |
| 框架 | Spring Boot | 3.5.15 | 基础框架 |
| ORM | MyBatis-Plus | 3.5.11 | MyBatis 增强，简化 CRUD |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 缓存 | Redis | - | 热点数据缓存 |
| 分布式锁 | Redisson | 3.39.0 | 分布式互斥锁，防缓存击穿 |
| 对象映射 | MapStruct | 1.5.5.Final | DTO / Entity 转换器 |
| 简化 | Lombok | - | 减少样板代码 |
| 构建工具 | Maven | - | 项目构建 |

---

## 项目结构

```
sy-server/
├── src/main/java/com/wtu/syserver/
│   ├── cache/               # Redis 与 Redisson 工具类
│   │   ├── RedisUtil.java
│   │   └── RedissonUtil.java
│   ├── common/              # 通用组件
│   │   ├── constants/       # 业务常量（城市、业务类型、Redis Key 等）
│   │   ├── enums/           # 状态码、消息枚举
│   │   ├── exception/       # 自定义异常 & 全局异常处理器
│   │   ├── idempotent/      # 幂等性管理器（Lua 原子实现）
│   │   ├── result/          # 统一响应体 Result<T>
│   │   └── utils/           # ID 生成、缓存 Key 工具
│   ├── config/              # Jackson / MyBatisPlus / Redisson 配置
│   ├── controller/          # REST 控制层（对外 API）
│   ├── convert/             # MapStruct 转换器（DTO ↔ Entity）
│   ├── dto/                 # 数据传输对象
│   ├── entity/              # 数据库实体（@TableName）
│   ├── mapper/              # MyBatis-Plus Mapper 接口
│   ├── service/             # 业务接口
│   │   └── impl/            # 业务实现
│   └── SyServerApplication.java
├── src/main/resources/
│   └── application.yml      # 应用配置
└── pom.xml
```

### 分层职责

- **Controller**：对外暴露 REST 接口，只做参数接收与 `Result` 封装。
- **Service**：承载核心业务逻辑，包括金额计算、事务控制、缓存协同。
- **Mapper**：基于 MyBatis-Plus 提供的数据访问层，默认使用雪花 ID。
- **Convert**：MapStruct 自动生成 DTO 与 Entity 的转换代码，保证类型安全。
- **common**：异常、枚举、幂等、统一响应等横切关注点。

---

## 核心特性

1. **幂等性提交**：通过 `IdempotentManager` + Lua 原子脚本实现 SETNX + EXPIRE，前端提交时携带 token 防止重复创建。
2. **缓存穿透与击穿防护**：
   - 分页查询采用 **Redis 缓存 + Redisson 分布式锁 + Double-Check** 机制；
   - 列表查询使用随机 TTL 避免缓存雪崩；
   - 删除时采用「立即删除 + 延迟二次删除」策略防止缓存回填。
3. **事务一致性**：报销单创建过程涉及主表 + 行程 + 补助 + 费用 + 分摊五张表写入，使用 `@Transactional(rollbackFor = Exception.class)` 保证原子性。
4. **金额合规校验**：费用分摊模块在服务端严格校验比例范围（0–1，最多 4 位小数）、比例之和等于 1、分摊金额之和等于报销总金额，防止前端篡改。
5. **统一响应结构**：所有接口使用 `Result<T>` 返回，包含 `code`、`message`、`data` 三要素，便于前端统一处理。
6. **全局异常处理**：`GlobalExceptionHandler` 集中捕获业务异常与系统异常，避免错误堆栈泄露。

---

## 数据库模型

共 5 张核心表，使用 MyBatis-Plus 注解 `@TableName` 映射：

### `reimbursement` 报销单主表
核心字段：报销单号、标题、报销人/部门/归属公司、业务类型、事由、状态（0 草稿 / 1 审计 / 2 通过 / 3 不通过 / 4 废除）、各项补助合计、创建时间。

### `trip` 行程表
按报销单记录每段行程：出发/到达城市、日期、出行人、行程说明，通过 `reimId` 关联报销单。

### `subsidy_info` 补助信息表
按行程记录补助：出行起止、补助天数、途经与补助城市、申请/实际补助金额，通过 `tripId`、`reimId` 双向关联。

### `expense_detail` 费用详情表
按天记录餐费、交通、通讯三项补助明细：费用日期、星期、城市与金额，通过 `subsidyInfoId`、`reimId` 关联。

### `cost_allocation` 费用分摊表
按报销单记录费用分摊方案：费用归属主体、项目、分摊比例（0–1）与金额，通过 `reimId` 关联，比例之和必须为 1。

### 表关系

```
reimbursement (1)
   ├── trip (N)            按行程拆分
   │     └── subsidy_info (N)      按行程申请补助
   │           └── expense_detail (N) 按天记录费用
   └── cost_allocation (N)  按报销单分摊费用
```

---

## 快速开始

### 前置依赖

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 1. 克隆项目

```bash
git clone <repo-url>
cd back/sy-server
```

### 2. 初始化数据库

项目根目录已提供 `back/database.sql`，包含完整的建库与建表脚本，在 MySQL 客户端中执行即可：

```bash
mysql -u <username> -p < back/database.sql
```

或在 MySQL 交互会话内：

```sql
source /path/to/back/database.sql;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shengyi?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
    username: root
    password: <your-password>
  data:
    redis:
      host: localhost
      port: 6379
      password: <redis-password>   # 若无密码则留空
      database: 1
```

### 4. 构建并运行

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

启动成功后，访问 `http://localhost:8080` 即可调用接口。

---

## 业务流程

### 创建报销单主流程

```
客户端
  │  携带 idempotentToken
  ▼
ReimbursementController#create
  ▼
ReimbursementServiceImpl#insertReimbursementDetail
  ├─ 1. 幂等校验（Lua 原子）
  ├─ 2. 生成报销单雪花 ID 与业务编号
  ├─ 3. 聚合计算三项补助总额
  ├─ 4. 写入 reimbursement 主表
  ├─ 5. 级联写入 trip 行程
  ├─ 6. 级联写入 subsidy_info 补助
  ├─ 7. 级联写入 expense_detail 费用明细
  ├─ 8. 校验 cost_allocation 比例/金额合法性
  └─ 9. 写入 cost_allocation 分摊
  ▼
返回 reimbursementId
```

### 查询报销单分页流程（缓存 + 分布式锁）

```
请求 → Redis 命中？→ 直接返回
    ↓ 未命中
    尝试获取 Redisson 分布式锁
    ↓ 获取成功
    Double-Check Redis
    ↓ 仍未命中
    查询 MySQL → 写入 Redis（随机 TTL）→ 释放锁 → 返回
    ↓ 获取失败
    回源 Redis 兜底 → 返回
```

---

## 后续规划

- [ ] 接入统一认证（Spring Security / JWT）
- [ ] 补充单元测试与集成测试（覆盖率 > 80%）
- [ ] 提供 OpenAPI / Swagger 文档
- [ ] 引入审计日志与操作追踪
- [ ] 与前端 `frontend/sy-ui` 联调

---

**License**：本项目仅用于学习与内部演示。
