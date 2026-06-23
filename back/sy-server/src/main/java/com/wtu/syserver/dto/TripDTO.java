package com.wtu.syserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TripDTO {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date departureDate;

    /**
     * 到达日期：到达时间，格式为年月日时分秒
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arrivalDate;

    /**
     * 行程说明：行程说明，长度不超过 500 字
     */
    private String description;

    /**
     * 补助信息
     */
    @JsonProperty("subsidies")
    private List<SubsidyInfoDTO> subsidyInfoDTOS;
}
