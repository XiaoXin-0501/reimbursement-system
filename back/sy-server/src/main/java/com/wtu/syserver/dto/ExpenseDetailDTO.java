package com.wtu.syserver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExpenseDetailDTO {
    /**
     * 报销单ID：关联报销单表主键ID
     */
    private String reimId;

    /**
     * 补助信息ID：关联补助信息表主键ID
     */
    private String SubsidyInfoId;

    /**
     * 费用发生日期：费用发生日期，格式为年月日
     */
    private Date expenseDate;

    /**
     * 星期：1=周一，2=周二，3=周三，4=周四，5=周五，6=周六，7=周日
     */
    private Integer week;

    /**
     * 城市ID：关联城市控件的 cityNo
     */
    private String cityId;

    /**
     * 城市名称：费用发生地城市名称
     */
    private String cityName;

    /**
     * 餐费补助：当日餐费补助金额，单位为元
     */
    private BigDecimal mealAllowance;

    /**
     * 交通补助：当日交通补助金额，单位为元
     */
    private BigDecimal transportationAllowance;

    /**
     * 通讯补助：当日通讯补助金额，单位为元
     */
    private BigDecimal communicationAllowance;
}
