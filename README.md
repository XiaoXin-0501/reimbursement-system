# 差旅报销管理系统 (Travel Reimbursement System)

> 一套前后端分离的企业差旅报销全流程管理系统。
> 后端基于 Spring Boot 3 + MyBatis-Plus + Redis + Redisson，提供幂等、防穿透/击穿/雪崩的高并发报销单服务；
> 前端基于 Vue 3 + TypeScript + Pinia + Element Plus，实现报销单分页查询、详情编辑、本地加密草稿、补助自动核算与费用分摊等业务能力。

---

## 目录

- [功能概览](#功能概览)
- [技术栈](#技术栈)
- [架构总览](#架构总览)
- [仓库结构](#仓库结构)
- [快速开始](#快速开始)
- [后端设计](#后端设计)
- [前端设计](#前端设计)
- [业务规则](#业务规则)
- [常见问题](#常见问题)

---

## 功能概览

- 报销单多条件分页查询（报销单号 / 标题 / 事由 / 费用归属公司 / 部门 / 报销人 / 业务类型）
- 报销单详情编辑：基础信息、补录行程、按天核算补助、费用分摊、备注
- 三级嵌套数据结构：**报销单 → 行程 → 补助 → 日度费用**
- 按城市等级自动计算餐费补助（一线 100 元 / 二线 80 元 / 三线 50 元），交通与通讯补助统一 40 元/天
- 费用分摊双模式：按比例分摊 & 按金额反推，比例自动凑满 100%
- 草稿本地保存：AES 加密落盘、7 天自动过期、最多 5 份草稿
- 幂等性提交：前端生成 token + 后端 Lua 原子校验，防止重复提交
- 列表合并展示：API 单据 + 本地草稿统一分页

---

## 技术栈

### 后端 (back/sy-server)

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 语言 | Java | 17 | LTS |
| 框架 | Spring Boot | 3.5 | 基础框架 |
| ORM | MyBatis-Plus | 3.5.11 | 雪花 ID、分页、逻辑删除 |
| 数据库 | MySQL | 8.0+ | 业务数据存储 |
| 缓存 | Redis | 6.0+ | 热点缓存、空值防穿透 |
| 分布式锁 | Redisson | 3.39 | 防缓存击穿 |
| 对象映射 | MapStruct + Lombok | 1.5.5 | DTO ↔ Entity 自动转换 |
| 构建 | Maven | - | 依赖管理 |

### 前端 (frontend/sy-ui)

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 语言 | TypeScript | 6.x | 类型安全 |
| 框架 | Vue 3 | 3.5 | Composition API |
| 构建 | Vite | 8.x | 开发与打包 |
| UI | Element Plus | 2.x | 组件库 |
| 路由 | Vue Router | 5.x | 页面路由 |
| 状态 | Pinia | 3.x | 全局状态 |
| HTTP | Axios | 1.x | 网络请求 |
| 加密 | crypto-js | 4.x | AES 草稿加密 |

---

## 架构总览

```
┌────────────────────────────────────────────────────────────┐
│                      前端  sy-ui (Vite)                      │
│   页面: 报销单列表 / 报销单详情                              │
│   Store: reimbursement / draft / reference / toast          │
│   Composable: useIdempotentToken / useSubsidyCalendar ...   │
│   Utils: subsidyRules / idGenerator / crypto / format       │
└────────────────────────────────────────────────────────────┘
                           │
                           │  HTTP (/api)
                           │  Vite dev proxy → http://localhost:8080
                           ▼
┌────────────────────────────────────────────────────────────┐
│                   后端  sy-server (Spring Boot)              │
│   Controller  ─  REST API（/api/reimbursement /trip / ...） │
│   Service     ─  业务编排 + 幂等 + 金额校验                  │
│   Mapper      ─  MyBatis-Plus 数据访问                       │
│   Convert     ─  MapStruct DTO↔Entity                       │
│   Common      ─  结果封装 / 异常 / 枚举 / 工具               │
└────────────────────────────────────────────────────────────┘
           │                    │
           ▼                    ▼
      ┌────────┐          ┌────────┐
      │  MySQL │          │  Redis │
      └────────┘          └────────┘
```

---

## 仓库结构

```
差旅报销/
├── back/
│   ├── sy-server/                       # 后端 Spring Boot 项目
│   │   └── src/main/java/com/wtu/syserver/
│   │       ├── cache/                   # RedisUtil / RedissonUtil
│   │       ├── common/                  # 常量/枚举/异常/幂等/结果/工具
│   │       ├── config/                  # Jackson / MyBatisPlus / Redisson 配置
│   │       ├── controller/              # 5 个 REST Controller
│   │       ├── convert/                 # 5 个 MapStruct 转换器
│   │       ├── dto/                     # 6 个 DTO
│   │       ├── entity/                  # 5 个实体（@TableName）
│   │       ├── mapper/                  # 5 个 Mapper
│   │       ├── service/                 # 5 个 Service + impl
│   │       └── SyServerApplication.java
│   └── database.sql                     # 建库建表脚本
└── frontend/
    └── sy-ui/                           # 前端 Vue 3 项目
        ├── src/
        │   ├── apis/                    # HTTP 请求模块（reimbursement/trip/subsidy/expense/costAllocation）
        │   ├── components/common/        # ConfirmDialog / ToastHost
        │   ├── composables/             # useIdempotentToken / useSubsidyCalendar / useCostAllocation
        │   ├── router/                  # 路由配置
        │   ├── stores/                  # pinia: reimbursement / draft / reference / toast
        │   ├── types/                   # TypeScript 类型定义
        │   ├── utils/                   # subsidyRules / crypto / idGenerator / format / businessTypeTree
        │   ├── views/
        │   │   ├── list/ReimbursementList.vue      # 列表页
        │   │   └── detail/ReimbursementDetail.vue  # 详情页（含 9 个面板子组件）
        │   ├── App.vue
        │   └── main.ts
        ├── index.html
        ├── vite.config.ts
        └── package.json
```

### 后端分层职责

| 层 | 包 | 关键类 | 职责 |
| --- | --- | --- | --- |
| Controller | `controller/` | ReimbursementController, TripController, SubsidInfoController, ExpenseDetailController, CostAllocationController | 接收 HTTP 请求，参数校验，封装 `Result<T>` 响应 |
| Service | `service/impl/` | ReimbursementServiceImpl（核心）, TripServiceImpl, SubsidInfoServiceImpl, ExpenseDetailServiceImpl, CostAllocationServiceImpl | 业务编排、金额计算、事务、缓存协同 |
| Mapper | `mapper/` | 对应 5 个 Mapper | MyBatis-Plus 数据访问 |
| Convert | `convert/` | 对应 5 个 MapStruct 接口 | DTO ↔ Entity 自动转换 |
| Entity | `entity/` | 5 个 `@TableName` 实体 | 数据库映射 |
| Common | `common/` | Result / GlobalExceptionHandler / IdempotentManager / StateCodeEnum / MessageEnum / IdUtils / CacheKeyUtils | 基础设施 |

### 前端分层职责

| 目录 | 关键文件 | 职责 |
| --- | --- | --- |
| `views/list` | ReimbursementList.vue | 列表页（筛选、分页、合并草稿） |
| `views/detail` | ReimbursementDetail.vue + 9 个面板子组件 | 详情页（步骤化编辑） |
| `stores/` | reimbursement.ts（核心）/ draft.ts（本地加密草稿）/ reference.ts（下拉字典） | Pinia 全局状态 |
| `apis/` | reimbursement.ts / trip.ts / subsidy.ts / expense.ts / costAllocation.ts | 后端 API 封装 |
| `composables/` | useIdempotentToken.ts / useSubsidyCalendar.ts / useCostAllocation.ts | 复用逻辑 |
| `utils/` | subsidyRules.ts / crypto.ts / idGenerator.ts / format.ts / businessTypeTree.ts | 业务规则与工具 |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 20.19+ 或 22.12+

### 1. 初始化数据库

```bash
mysql -u <username> -p < back/database.sql
```

该脚本会创建 `shengyi` 数据库及 5 张核心业务表。

### 2. 修改后端配置

编辑 `back/sy-server/src/main/resources/application.yml`：

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
      password: <redis-password>   # 无密码则留空
      database: 1
```

### 3. 启动后端

```bash
cd back/sy-server
mvn clean package -DskipTests
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
cd frontend/sy-ui
npm install
npm run dev
```

Vite 会在 `http://localhost:5173` 启动，并把 `/api` 代理到后端 `http://localhost:8080`。

### 5. 打包部署

```bash
# 前端
npm run build     # 产物在 dist/
# 后端
mvn clean package # 产物在 target/sy-server-0.0.1-SNAPSHOT.jar
```

---

## 后端设计

### 数据模型

```
reimbursement (1)
   ├── trip (N)           行程：出发/到达城市、日期
   │     └── subsidy_info (N)       补助：起止日期、补助天数、金额
   │           └── expense_detail (N) 按天的餐费/交通/通讯明细
   └── cost_allocation (N) 费用分摊方案
```

核心字段：
- 报销单号 `BX + yyyymmdd + 雪花ID`，业务可读 + 全局唯一
- 状态：`0 草稿 / 1 审计 / 2 通过 / 3 不通过 / 4 废除`

### 创建报销单主流程（9 步级联）

1. **幂等校验**：Lua 原子脚本 `SETNX + EXPIRE`，token 重复直接抛错
2. **生成 ID**：雪花 ID + 业务编号 `BXyyyyMMdd + snowflakeId`
3. **聚合三项补助总额**（餐费 / 交通 / 通讯）
4. **写入报销单主表**
5. **批量写入行程**
6. **批量写入补助信息**（并计算补助金额）
7. **批量写入日度费用明细**
8. **校验费用分摊合法性**
9. **写入费用分摊**

全程 `@Transactional(rollbackFor = Exception.class)` 保证原子性。

### 高并发缓存工程

| 问题 | 方案 | 位置 |
| --- | --- | --- |
| 缓存穿透 | 空值写入 `__NULL__` 占位 + 短 TTL | `RedisUtil.set()` |
| 缓存击穿 | Redisson 分布式锁 + Double-Check | `ReimbursementServiceImpl#getReimbursementPage` |
| 缓存雪崩 | TTL 随机偏移（`ThreadLocalRandom`） | `RedisUtil.set(key, value, offset, ttl, unit)` |
| 缓存回填 | 立即删 + 异步 `sleep` 后二次删除 | 各 ServiceImpl 的 delete 方法 |

### 分布式幂等

`IdempotentManager` 使用 Lua 脚本原子执行：

```
if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then
    redis.call('expire', KEYS[1], tonumber(ARGV[2]))
    return 1
else
    return 0
end
```

前端提交时携带 `idempotentToken`，后端 1 次判定即可，重复请求直接抛 `ReimbursementException`。

### 金额反篡改校验

`CostAllocationServiceImpl#validateCostAllocation` 强制校验：
- 比例范围 `[0, 1]`，精度 ≤ 4 位
- 比例之和 ≈ 1（四舍五入到 4 位比较）
- `分摊金额 = 总额 × 比例` 服务端重算比对

### 统一异常与响应

- `Result<T>`：`code / message / data` 三要素
- 业务异常按域拆分：`ReimbursementException / TripException / SubsidInfoException / ExpenseDetailException / CostAllocationException / ParamInvalidException`
- `GlobalExceptionHandler` 统一捕获并返回

### 关键 Redis Key 约定（`RedisKeyEnum`）

| Key | 用途 |
| --- | --- |
| `identity:token:{token}` | 幂等 token |
| `reim:page` | 报销单分页缓存 |
| `reim:page:version` | 分页版本号（用于失效） |
| `reim:page:lock:{key}` | 分布式锁 |
| `trip:list:{reimId}` | 行程列表 |
| `subsidy:list:{reimId}` | 补助信息列表 |
| `expense:list:{subsidyInfoId}` | 费用详情列表 |
| `allocation:list:{reimId}` | 费用分摊列表 |

---

## 前端设计

### 页面路由

| 路径 | 组件 | 说明 |
| --- | --- | --- |
| `/` | 重定向到 `/list` | - |
| `/list` | `ReimbursementList.vue` | 报销单列表（支持多条件筛选、分页、合并草稿） |
| `/detail/:id?` | `ReimbursementDetail.vue` | 报销单详情（新增 / 编辑 / 复制 / 草稿恢复） |

### Pinia Store

| Store | 作用 |
| --- | --- |
| `useReimbursementStore` | 详情页核心状态：basicInfo / trips / subsidies / allocations + 提交逻辑 |
| `useDraftStore` | 本地加密草稿（AES 加密、最多 5 份、7 天过期） |
| `useReferenceStore` | 下拉字典：公司 / 部门 / 员工 / 业务类型（树） / 城市 / 项目 |
| `useToastStore` | 全局消息提示 |

### 详情页 6 大面板

1. `DetailHeader` — 页面头部，标题与操作
2. `BasicInfoPanel` — 基础信息（标题、报销人、部门、公司、业务类型、事由、备注）
3. `TripPanel` — 补录行程（支持新增 / 编辑 / 删除 / 复制 / 日期重叠检测）
4. `SubsidyPanel` — 补助信息（按行程自动生成，按天编辑）
5. `ExpenseSummaryPanel` — 补助汇总（餐费 / 交通 / 通讯）
6. `CostAllocationPanel` — 费用分摊（支持按比例 / 按金额 / 均分三种模式）
7. `DetailFooter` — 保存草稿 / 提交按钮

### 本地加密草稿

- 使用 `crypto-js` AES 对称加密，密钥在 `utils/crypto.ts`
- 草稿索引 `sy_reim_draft_index` + 明细 `sy_reim_draft_{timestamp}`
- 上限 5 份，过期 7 天，登出/过期自动清理
- 页面 `beforeunload` 自动触发 `saveAsDraft`
- 列表页合并展示「API 单据 + 本地草稿」并统一分页

### 下拉字典（`useReferenceStore`）

- 公司 5 家（北京 / 上海 / 武汉 / 杭州 / 荆州 分公司）
- 部门 7 个（客户成功 / 企业消费 / 企业费控 / 集采 / 航旅 / 运营 / 营销）
- 员工 6 人（演示账号）
- 业务类型 3 大类 + 13 个子项（支持树形选择）
- 城市 5 个（3 种等级）
- 项目 8 个（含 1 个「非项目类费用归集」）

### 与后端的契约

- 前端 `POST /api/reimbursement/create`，携带 `idempotentToken` + 嵌套 DTO
- 前端 `POST /api/reimbursement/page`，拉全量后在前端做分页 + 草稿合并
- 详情页通过 `GET /api/trip/get/{reimId}` / `GET /api/subsidy/get/{reimId}` / `GET /api/expense/get/{subsidyInfoId}` / `GET /api/cost/allocation/get/{reimId}` 分别拉取子表数据
- Axios 统一响应拦截：`code === 20000` 视为成功

---

## 业务规则

### 城市等级

| 等级 | 城市 | 餐费补助（元/天） |
| --- | --- | --- |
| 1 一线 | 北京、上海 | 100 |
| 2 二线 | 武汉、杭州 | 80 |
| 3 三线 | 荆州 | 50 |

- 交通补助：所有城市统一 40 元/天
- 通讯补助：所有城市统一 40 元/天

### 费用分摊规则

- 比例范围 `[0, 1]`，精度 ≤ 4 位小数
- 比例之和必须 = 1（±0.0001 容错）
- 分摊金额之和必须 = 补助总金额（±0.01 容错）
- 按金额反推比例时自动保留 4 位小数
- 支持「均分」一键分摊

### 行程重叠检测

同一出行人的两段行程若日期区间存在交集，前端会提示冲突。

### 幂等 Token 生成规则（前端）

```
sub_{userId}_{timestamp}_{uuid}
```

每次进入详情页生成一次，提交后失效。

---

## 常见问题

**Q: 启动后端报 `Communications link failure`？**
A: 检查 MySQL 是否启动，`application.yml` 的 `username / password / url` 是否正确，数据库 `shengyi` 是否已创建（运行 `database.sql`）。

**Q: 启动前端报 `Cannot connect to proxy target`？**
A: 后端未启动。先启动 `mvn spring-boot:run`，再启动 `npm run dev`。

**Q: 提交报销单返回「重复提交」？**
A: 幂等 token 已被 Redis 记录。在详情页点击「重新生成 token」或刷新页面，再提交即可。

**Q: 草稿在另一台电脑看不到？**
A: 草稿存储在浏览器 `localStorage`，不会同步到服务器。需要跨设备同步请等待后续版本接入云端草稿。

**Q: 列表页数据很多时前端卡顿？**
A: 目前列表采用「后端拉全量 + 前端分页」方案。后续版本将改为「后端真实分页」，请关注版本更新。

**Q: 如何切换到其他 Redis 实例？**
A: 修改 `application.yml` 中的 `spring.data.redis.host / port / password`，无需改代码。

---

## 目录结构约定

- 后端代码修改 → 重启 `mvn spring-boot:run`
- 前端代码修改 → Vite HMR 自动热更新
- 数据库结构变更 → 修改 `back/database.sql` 并同步实体类
- 接口变更 → 同步修改前端 `src/apis/*.ts`

---

## 后续规划

- [ ] 接入统一认证（Spring Security + JWT）
- [ ] 后端真实分页（替换前端全量拉取方案）
- [ ] 云端草稿同步
- [ ] OpenAPI / Swagger 文档
- [ ] 单元测试 + 集成测试覆盖率 > 80%
- [ ] 审计日志与操作追踪
- [ ] 审批流（工作流引擎）

---

**License**：本项目仅用于学习与内部演示。
