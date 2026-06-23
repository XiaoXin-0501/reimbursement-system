package com.wtu.syserver.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String REDIS_HOST;

    @Value("${spring.data.redis.port}")
    private String REDIS_PORT;

    @Value("${spring.data.redis.database}")
    private int database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + REDIS_HOST + ":" + REDIS_PORT)
                .setDatabase(database);
        return Redisson.create(config);
    }
}
