package com.wtu.syserver.common.constants;

public class RedisConstant {
    public static final int TRY_LOCK_TIMES = 3;
    /**
     * 最长等待时间（毫秒）
     */
    public static final long LOCK_WAIT_TIME = 50L;
    /**
     * 最长持有时间（毫秒）
     */
    public static final long LOCK_LEASE_TIME = 100L;
    /**
     * 延迟双删的延迟删除时间（毫秒）
     */
    public static final long DELETE_DELAY_TIME = 500L;
}
