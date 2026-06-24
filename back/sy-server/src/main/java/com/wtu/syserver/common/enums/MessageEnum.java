package com.wtu.syserver.common.enums;

import lombok.Getter;

@Getter
public enum MessageEnum {
    SUCCESS("操作成功"),
    PARAM_INVALID_ERROR("请求参数错误"),

    REDIS_SET_ERROR("redis set error"),
    REDIS_GET_ERROR("redis get error"),
    REDIS_KEY_CREATE_FAIL("redis key create fail"),
    REDIS_DELETE_FAIL("redis delete fail"),
    REDIS_INCR_ERROR("redis incr error"),

    REIMBURSE_DETAIL_INSERT_FAIL("报销单插入失败"),
    REIMBURSE_SUBMIT_REPEAT("报销单重复提交"),

    SUBSID_INFO_EXCEPTION("补助信息异常"),

    EXPENSE_DETAIL_EXCEPTION("费用明细异常"),

    COST_ALLOCATION_AMOUNT_ERROR("报销金额不合法"),
    COST_ALLOCATION_EMPTY_EXCEPTION("费用分摊明细为空"),
    COST_ALLOCATION_PROPORTION_EXCEPTION("费用分摊比例异常"),
    COST_ALLOCATION_AMOUNT_EXCEPTION("费用分摊金额异常"),
    COST_ALLOCATION_CALCULATE_EXCEPTION("费用分摊计算异常"),
    ;

    private final String message;

    MessageEnum(String message) {
        this.message = message;
    }
}
