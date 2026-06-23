package com.wtu.syserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SubsidyInfoDTO {
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
     * 费用明细id：关联费用详情表主键ID
     */
    @JsonProperty("expenses")
    private List<ExpenseDetailDTO> expenseDetailDTOS;
}
