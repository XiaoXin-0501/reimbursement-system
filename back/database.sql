-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: shengyi
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cost_allocation`
--

DROP TABLE IF EXISTS `cost_allocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cost_allocation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID，费用分摊记录唯一标识',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID，关联报销单表主键',
  `cost_owner_id` varchar(32) NOT NULL COMMENT '费用归属ID',
  `cost_owner_name` varchar(100) NOT NULL COMMENT '费用归属名称',
  `project_id` varchar(32) NOT NULL COMMENT '项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `proportion` decimal(5,4) NOT NULL DEFAULT '0.0000' COMMENT '分摊比例，范围0-1，保留四位小数',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '分摊金额，单位：元',
  PRIMARY KEY (`id`),
  KEY `idx_reim_id` (`reim_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用分摊表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `expense_detail`
--

DROP TABLE IF EXISTS `expense_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense_detail` (
  `id` varchar(32) NOT NULL COMMENT '主键ID，费用详情唯一标识',
  `subsidy_info_id` varchar(32) NOT NULL COMMENT '补助信息ID，关联补助信息表主键',
  `expense_date` date NOT NULL COMMENT '费用发生日期，格式：年月日',
  `week` int DEFAULT NULL COMMENT '星期：1=周一，2=周二，3=周三，4=周四，5=周五，6=周六，7=周日',
  `city_id` varchar(32) NOT NULL COMMENT '城市ID',
  `city_name` varchar(50) NOT NULL COMMENT '城市名称',
  `meal_allowance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当日餐费补助，单位：元',
  `transportation_allowance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当日交通补助，单位：元',
  `communication_allowance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当日通讯补助，单位：元',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单id',
  PRIMARY KEY (`id`),
  KEY `idx_subsidy_info_id` (`subsidy_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reimbursement`
--

DROP TABLE IF EXISTS `reimbursement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reimbursement` (
  `id` varchar(32) NOT NULL COMMENT '主键ID，报销单唯一标识',
  `reimbursement_no` varchar(32) NOT NULL COMMENT '报销单号，报销单唯一业务编号',
  `title` varchar(500) NOT NULL COMMENT '报销标题，长度不超过500字',
  `reimburser_id` varchar(32) NOT NULL COMMENT '报销人ID，关联员工控件',
  `reimburser_name` varchar(100) NOT NULL COMMENT '报销人姓名',
  `reim_department_id` varchar(32) NOT NULL COMMENT '报销部门ID',
  `reim_department_name` varchar(100) NOT NULL COMMENT '报销部门名称',
  `reim_company_id` varchar(32) NOT NULL COMMENT '费用归属公司ID',
  `reim_company_name` varchar(100) NOT NULL COMMENT '费用归属公司名称',
  `business_type_id` varchar(32) NOT NULL COMMENT '业务类型ID',
  `business_type_name` varchar(100) NOT NULL COMMENT '业务类型名称',
  `reason` varchar(500) NOT NULL COMMENT '出差事由，长度不超过500字',
  `remarks` varchar(1000) DEFAULT NULL COMMENT '备注信息，长度不超过1000字',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '报销状态：0=草稿，1=审计，2=通过，3=不通过，4=废除',
  `subsidy_total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '补助总金额，单位：元',
  `meal_allowance_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '餐费补助合计，单位：元',
  `transportation_allowance_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '交通补助合计，单位：元',
  `communication_allowance_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '通讯补助合计，单位：元',
  `create_time` date NOT NULL COMMENT '创建时间，格式：年月日',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报销单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subsidy_info`
--

DROP TABLE IF EXISTS `subsidy_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subsidy_info` (
  `id` varchar(32) NOT NULL COMMENT '主键ID，补助信息唯一标识',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID，关联报销单表主键',
  `trip_id` varchar(32) DEFAULT NULL COMMENT '行程表ID，关联行程表主键',
  `traveler_id` varchar(32) NOT NULL COMMENT '出行人ID',
  `traveler_name` varchar(100) NOT NULL COMMENT '出行人姓名',
  `travel_start_date` date NOT NULL COMMENT '出行开始日期，格式：年月日',
  `travel_end_date` date NOT NULL COMMENT '出行结束日期，格式：年月日',
  `travel_days` int NOT NULL COMMENT '补助天数',
  `itinerary` varchar(500) DEFAULT NULL COMMENT '途经行程，示例：天津*上海*北京',
  `subsidy_city` varchar(500) DEFAULT NULL COMMENT '补助城市，示例：天津*上海*北京',
  `apply_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '补助申请金额，单位：元',
  `subsidy_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实际补助金额，单位：元',
  PRIMARY KEY (`id`),
  KEY `idx_reim_id` (`reim_id`),
  KEY `idx_trip_id` (`trip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补助信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trip`
--

DROP TABLE IF EXISTS `trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trip` (
  `id` varchar(32) NOT NULL COMMENT '主键ID，行程唯一标识',
  `reim_id` varchar(32) NOT NULL COMMENT '报销单ID，关联报销单表主键',
  `traveler_id` varchar(32) NOT NULL COMMENT '出行人ID',
  `traveler_name` varchar(100) NOT NULL COMMENT '出行人姓名',
  `departure_city_id` varchar(32) NOT NULL COMMENT '出发城市ID',
  `departure_city_name` varchar(50) NOT NULL COMMENT '出发城市名称',
  `arrival_city_id` varchar(32) NOT NULL COMMENT '到达城市ID',
  `arrival_city_name` varchar(50) NOT NULL COMMENT '到达城市名称',
  `departure_date` datetime NOT NULL COMMENT '出发日期，格式：年月日时分秒',
  `arrival_date` datetime NOT NULL COMMENT '到达日期，格式：年月日时分秒',
  `description` varchar(500) DEFAULT NULL COMMENT '行程说明，长度不超过500字',
  PRIMARY KEY (`id`),
  KEY `idx_reim_id` (`reim_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行程表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-23 14:56:40
