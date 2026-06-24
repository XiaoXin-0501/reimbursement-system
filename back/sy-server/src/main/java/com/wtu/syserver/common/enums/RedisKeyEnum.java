package com.wtu.syserver.common.enums;

import lombok.Getter;

@Getter
public enum RedisKeyEnum {
    IDENTITY_TOKEN("identity:token"),

    REIM_PAGE("reim:page"),
    REIM_PAGE_VERSION("reim:page:version"),
    REIM_PAGE_LOCK("reim:page:lock"),

    TRIP_LIST("trip:list"),
    SUBSIDY_LIST("subsidy:list"),
    EXPENSE_LIST("expense:list"),
    ALLOCATION_LIST("allocation:list"),
    ;

    private final String key;

    RedisKeyEnum(String key) {
        this.key = key;
    }

    public String getKey(String suffix) {
        return key + ":" + suffix;
    }

}
