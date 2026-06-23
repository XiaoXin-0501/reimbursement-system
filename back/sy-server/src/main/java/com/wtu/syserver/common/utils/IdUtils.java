package com.wtu.syserver.common.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IdUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取雪花算法 ID（19位）
     */
    public static String getSnowflakeId() {
        return IdWorker.getIdStr();
    }

    /**
     * 原有 UUID 方法（32位）
     */
    public static String getUuid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成报销单号：BX + 日期 + 雪花ID；配置正确的情况下不会出现重复
     * 总长29位
     */
    public static String getReimburseNo() {
        String dateStr = LocalDate.now().format(FORMATTER);
        return "BX" + dateStr + getSnowflakeId();
    }

    public static String getIdempotentId(String sense) {
        return sense + "_" + getSnowflakeId();
    }
}
