package com.wtu.syserver.common.idempotent;

import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class IdempotentManager {

    /**
     * SETNX + EXPIRE（原子幂等）
     * 1 -> 不存在，写入成功
     * 0 -> 已存在
     */
    private static final String LUA_SCRIPT =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                    "redis.call('expire', KEYS[1], tonumber(ARGV[2])) " +
                    "return 1 " +
                    "else return 0 end";

    private static final DefaultRedisScript<Long> SCRIPT;

    static {
        SCRIPT = new DefaultRedisScript<>();
        SCRIPT.setScriptText(LUA_SCRIPT);
        SCRIPT.setResultType(Long.class);
    }

    private final RedisUtil redisUtil;

    /**
     * 幂等校验 + 写入
     *
     * @param token 前端传入幂等token
     * @return true = 首次请求 / false = 重复请求
     */
    public boolean checkAndSet(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        String key = RedisKeyEnum.IDENTITY_TOKEN.getKey(token);

        Long result = redisUtil.getRedisTemplate().execute(
                SCRIPT,
                Collections.singletonList(key),
                token,
                String.valueOf(ExpireTimeConstant.IDEMPOTENT_TOKEN_EXPIRE_TIME)
        );

        return Long.valueOf(1).equals(result);
    }
}