package com.wtu.syserver.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.exception.CacheException;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class RedisUtil {
    private static final String NULL_VALUE = "__NULL__";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtil(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 设置缓存
     *
     * @param key   缓存key
     * @param value 缓存值
     * @param <T>   缓存值类型
     */
    public <T> void set(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_SET_ERROR);
        }
    }

    /**
     * 设置缓存（带随机过期时间，防缓存雪崩）
     *
     * @param key         缓存key
     * @param value       缓存值
     * @param offsetRange 随机偏移范围
     * @param expire      基础过期时间
     * @param timeUnit    时间单位
     * @param <T>         类型
     */
    public <T> void set(
            String key,
            T value,
            long offsetRange,
            long expire,
            TimeUnit timeUnit
    ) {
        try {
            // 空值缓存（防穿透）
            if (value == null) {
                redisTemplate.opsForValue().set(key, NULL_VALUE, ExpireTimeConstant.REDIS_NULL_EXPIRE_TIME, TimeUnit.SECONDS);
                return;
            }
            String json = objectMapper.writeValueAsString(value);

            long offset = 0L;
            if (offsetRange > 0) {
                offset = ThreadLocalRandom.current().nextLong(-offsetRange, offsetRange + 1);
            }

            long finalExpire = expire + offset;

            // 防止过期时间非法
            if (finalExpire <= 0) {
                finalExpire = 1;
            }
            redisTemplate.opsForValue().set(key, json, finalExpire, timeUnit);
        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_SET_ERROR);
        }
    }

    /**
     * 获取缓存
     *
     * @param key   缓存key
     * @param clazz 缓存值类型
     * @param <T>   缓存值类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || NULL_VALUE.equals(json)) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_GET_ERROR);
        }
    }

    // GET（泛型版本）
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null || NULL_VALUE.equals(json)) {
                return null;
            }

            return objectMapper.readValue(json, typeReference);

        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_GET_ERROR);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        try {
            if (key == null || key.isEmpty()) {
                return false;
            }
            return redisTemplate.delete(key);
        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_DELETE_FAIL);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存key集合
     * @return 删除数量
     */
    public Long delete(String... keys) {
        try {
            if (keys == null || keys.length == 0) {
                return 0L;
            }
            return redisTemplate.delete(java.util.Arrays.asList(keys));
        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_DELETE_FAIL);
        }
    }

    /**
     * 按前缀删除
     */
    public void deleteByPrefix(String prefix) {
        try {
            if (prefix == null || prefix.isEmpty()) {
                return;
            }

            var keys = redisTemplate.keys(prefix + "*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

        } catch (Exception e) {
            throw new CacheException(MessageEnum.REDIS_DELETE_FAIL);
        }
    }
}
