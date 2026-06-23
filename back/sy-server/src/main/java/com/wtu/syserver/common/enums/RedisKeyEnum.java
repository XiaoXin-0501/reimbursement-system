package com.wtu.syserver.common.enums;

import lombok.Getter;

@Getter
public enum RedisKeyEnum {
    IDENTITY_TOKEN("identity:token"),

    REIM_PAGE("reim:page"),
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

//    /**
//     * 拼接多个后缀，自动用冒号分隔
//     *
//     * @param suffixes 可变参数多个后缀
//     * @return 完整redis key
//     */
//    public String getKey(String... suffixes) {
//        if (suffixes == null || suffixes.length == 0) {
//            return key;
//        }
//        return key + ":" + String.join(":", suffixes);
//    }
}
