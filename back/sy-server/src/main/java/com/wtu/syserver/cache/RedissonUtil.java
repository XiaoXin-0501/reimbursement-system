package com.wtu.syserver.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Getter
@Component
public class RedissonUtil {
    private final RedissonClient redisson;
}
