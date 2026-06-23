package com.wtu.syserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 行程表实体类
 */
@Data
@TableName("trip")
public class Trip implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID：行程唯一标识
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 报销单ID：关联报销单表主键ID
     */
    private String reimId;

    /**
     * 出行人ID：关联员工控件的 reimburserId
     */
    private String travelerId;

    /**
     * 出行人姓名：出行人名称
     */
    private String travelerName;

    /**
     * 出发城市ID：关联城市控件的 cityNo
     */
    private String departureCityId;

    /**
     * 出发城市名称：出发城市名称
     */
    private String departureCityName;

    /**
     * 到达城市ID：关联城市控件的 cityNo
     */
    private String arrivalCityId;

    /**
     * 到达城市名称：到达城市名称
     */
    private String arrivalCityName;

    /**
     * 出发日期：出发时间，格式为年月日时分秒
     */
    private Date departureDate;

    /**
     * 到达日期：到达时间，格式为年月日时分秒
     */
    private Date arrivalDate;

    /**
     * 行程说明：行程说明，长度不超过 500 字
     */
    private String description;
}