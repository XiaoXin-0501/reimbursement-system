package com.wtu.syserver.common.enums;

import lombok.Getter;

@Getter
public enum StateCodeEnum {
    SUCCESS(20000, "操作成功"),

    REQUEST_PARAM_ERROR(40000, "请求参数错误"),

    SERVER_EXCEPTION(50000, "服务内部异常"),
    CACHE_EXCEPTION(51000, "缓存异常"),

    BUSINESS_EXCEPTION(60000, "业务异常"),
    REIMBURSE_EXCEPTION(61000, "报销单异常"),
    TRIP_EXCEPTION(62000, "行程异常"),
    SUBSIDY_INFO_EXCEPTION(63000, "补助信息异常"),
    COST_ALLOCATION_EXCEPTION(64000, "费用分摊异常"),
    EXPENSES_DETAIL_EXCEPTION(65000, "费用详情异常"),
    ;
    private final Integer code;
    private final String message;

    StateCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
