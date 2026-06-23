package com.wtu.syserver.common.constants;

public class ExpireTimeConstant {
    /**
     * 幂等性令牌有效期（秒）
     */
    public static final long IDEMPOTENT_TOKEN_EXPIRE_TIME = 60 * 30L;

    /**
     * 空值缓存时间 (秒）
     */
    public static final long REDIS_NULL_EXPIRE_TIME = 60L;

    /**
     * 普通业务缓存时间（秒）
     */
    public static final long COMMON_EXPIRE_TIME = 60 * 30L;

    /**
     * 普通业务存时间偏移量(秒）
     */
    public static final long COMMON_OFFSET_TIME = 60 * 3L;

    /**
     * 报销单分页缓存时间(秒）
     */
    public static final long REIM_PAGE_EXPIRE_TIME = 60 * 5L;

    /**
     * 报销单分页缓存时间偏移量(秒）
     */
    public static final long REIM_PAGE_OFFSET_TIME = 30L;
}
