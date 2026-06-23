package com.wtu.syserver.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.exception.CacheException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CacheKeyUtils {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .build();

    /**
     * 将param转为hash值
     *
     * @param param 参数对象
     * @return hash
     */
    public static String generate(Object param) {
        try {
            String json = MAPPER.writeValueAsString(param);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(json.getBytes(StandardCharsets.UTF_8));

            return hex(digest);

        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_KEY_CREATE_FAIL);
        }
    }

    public static String pageKey(String prefix, Object param) {
        return prefix + ":" + generate(param);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
