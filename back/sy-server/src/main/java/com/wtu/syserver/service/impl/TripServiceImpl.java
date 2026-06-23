package com.wtu.syserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.constants.RedisConstant;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import com.wtu.syserver.common.utils.IdUtils;
import com.wtu.syserver.convert.TripConvert;
import com.wtu.syserver.dto.TripDTO;
import com.wtu.syserver.entity.Trip;
import com.wtu.syserver.mapper.TripMapper;
import com.wtu.syserver.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TripServiceImpl implements TripService {
    private final RedisUtil redisUtil;

    private final TripMapper tripMapper;
    private final TripConvert tripConvert;

    @Override
    public List<String> insertTrip(List<TripDTO> tripDTOS) {
        if (tripDTOS == null || tripDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        List<Trip> trips = tripConvert.toEntities(tripDTOS);

        // 流式赋值ID + 收集ID，顺序严格一致
        List<String> idList = trips.stream()
                .map(trip -> {
                    String id = IdUtils.getSnowflakeId();
                    trip.setId(id);
                    return id;
                })
                .collect(Collectors.toList());

        tripMapper.insert(trips);
        return idList;
    }

    @Override
    public List<Trip> getTripListByReimId(String reimId) {
        List<Trip> cache = redisUtil.get(
                RedisKeyEnum.TRIP_LIST.getKey(reimId),
                new TypeReference<List<Trip>>() {
                }
        );
        if (cache != null && !cache.isEmpty()) return cache;

        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getReimId, reimId);
        List<Trip> result = tripMapper.selectList(wrapper);

        redisUtil.set(
                RedisKeyEnum.TRIP_LIST.getKey(reimId),
                result,
                ExpireTimeConstant.COMMON_OFFSET_TIME,
                ExpireTimeConstant.COMMON_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTripByReimId(String reimId) {
        String key = RedisKeyEnum.TRIP_LIST.getKey(reimId);
        redisUtil.delete(key);

        LambdaQueryWrapper<Trip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trip::getReimId, reimId);
        int rows = tripMapper.delete(wrapper);

        // 延迟二次删除（防止回填）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RedisConstant.DELETE_DELAY_TIME);
                redisUtil.delete(key);
            } catch (InterruptedException ignored) {
            }
        });
        // 删除匹配条件的数据
        return rows;
    }
}
