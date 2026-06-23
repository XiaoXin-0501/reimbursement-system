package com.wtu.syserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 补助信息表实体类
 */
@Data
@TableName("subsidy_info")
public class SubsidyInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID：补助信息唯一标识
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 报销单ID：关联报销单表主键ID
     */
    private String reimId;

    /**
     * 行程表ID：关联行程表主键ID
     */
    private String tripId;

    /**
     * 出行人ID：关联员工控件的 reimburserId
     */
    private String travelerId;

    /**
     * 出行人姓名：出行人名称
     */
    private String travelerName;

    /**
     * 出行开始日期：出行起始日期，格式为年月日
     */
    private Date travelStartDate;

    /**
     * 出行结束日期：出行结束日期，格式为年月日
     */
    private Date travelEndDate;

    /**
     * 补助天数：享受补助的天数
     */
    private Integer travelDays;

    /**
     * 途经行程：途经城市，格式示例："天津/上海/北京"
     */
    private String itinerary;

    /**
     * 补助城市：享受补助的城市，格式示例："天津/上海/北京"
     */
    private String subsidyCity;

    /**
     * 补助申请金额：申请补助金额，单位为元
     */
    private BigDecimal applyAmount;

    /**
     * 实际补助金额：实际补助金额，单位为元
     */
    private BigDecimal subsidyAmount;
}
