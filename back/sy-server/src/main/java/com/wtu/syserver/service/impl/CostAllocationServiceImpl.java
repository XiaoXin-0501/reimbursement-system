package com.wtu.syserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.constants.RedisConstant;
import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import com.wtu.syserver.common.exception.CostAllocationException;
import com.wtu.syserver.common.utils.IdUtils;
import com.wtu.syserver.convert.CostAllocationConvert;
import com.wtu.syserver.dto.CostAllocationDTO;
import com.wtu.syserver.entity.CostAllocation;
import com.wtu.syserver.mapper.CostAllocationMapper;
import com.wtu.syserver.service.CostAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CostAllocationServiceImpl implements CostAllocationService {
    private final RedisUtil redisUtil;

    private final CostAllocationMapper costAllocationMapper;
    private final CostAllocationConvert costAllocationConvert;

    @Override
    public List<String> insertCostAllocation(List<CostAllocationDTO> costAllocationDTOS) {
        if (costAllocationDTOS == null || costAllocationDTOS.isEmpty()) {
            return Collections.emptyList();
        }
        List<CostAllocation> costAllocations = costAllocationConvert.toEntityList(costAllocationDTOS);

        List<String> idList = costAllocations.stream()
                .map(costAllocation -> {
                    String id = IdUtils.getSnowflakeId();
                    costAllocation.setId(id);
                    return id;
                })
                .collect(Collectors.toList());

        costAllocationMapper.insert(costAllocations);
        return idList;
    }

    @Override
    public int deleteCostAllocationByReimId(String reimId) {
        String key = RedisKeyEnum.ALLOCATION_LIST.getKey(reimId);
        redisUtil.delete(key);
        redisUtil.incr(RedisKeyEnum.REIM_PAGE_VERSION.getKey());

        LambdaQueryWrapper<CostAllocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostAllocation::getReimId, reimId);
        int rows = costAllocationMapper.delete(wrapper);

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

    @Override
    public List<CostAllocation> getCostAllocationListByReimId(String reimId) {
        List<CostAllocation> cache = redisUtil.get(
                RedisKeyEnum.ALLOCATION_LIST.getKey(reimId),
                new TypeReference<List<CostAllocation>>() {
                }
        );

        if (cache != null && !cache.isEmpty()) return cache;

        LambdaQueryWrapper<CostAllocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostAllocation::getReimId, reimId);
        List<CostAllocation> result = costAllocationMapper.selectList(wrapper);

        redisUtil.set(
                RedisKeyEnum.ALLOCATION_LIST.getKey(reimId),
                result,
                ExpireTimeConstant.COMMON_OFFSET_TIME,
                ExpireTimeConstant.COMMON_EXPIRE_TIME,
                TimeUnit.SECONDS
        );

        return result;
    }

    @Override
    public void validateCostAllocation(BigDecimal allAmount, List<CostAllocationDTO> costAllocationDTOS) {
        if (allAmount == null || allAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CostAllocationException(MessageEnum.COST_ALLOCATION_AMOUNT_ERROR);
        }

        if (costAllocationDTOS == null || costAllocationDTOS.isEmpty()) {
            throw new CostAllocationException(MessageEnum.COST_ALLOCATION_EMPTY_EXCEPTION);
        }

        BigDecimal totalProportion = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CostAllocationDTO dto : costAllocationDTOS) {

            if (dto == null) {
                throw new CostAllocationException(MessageEnum.COST_ALLOCATION_EMPTY_EXCEPTION);
            }

            BigDecimal proportion = dto.getProportion();
            BigDecimal amount = dto.getAmount();

            // 非空校验
            if (proportion == null || amount == null) {
                throw new CostAllocationException(MessageEnum.COST_ALLOCATION_EMPTY_EXCEPTION);
            }

            // 比例范围校验
            if (proportion.compareTo(BigDecimal.ZERO) < 0 ||
                    proportion.compareTo(BigDecimal.ONE) > 0 ||
                    proportion.scale() > 4
            ) {
                throw new CostAllocationException(MessageEnum.COST_ALLOCATION_PROPORTION_EXCEPTION);
            }

            // 金额计算校验（防止前端篡改）
            BigDecimal expectedAmount = allAmount.multiply(proportion).setScale(2, RoundingMode.HALF_UP);

            if (amount.compareTo(expectedAmount) != 0) {
                throw new CostAllocationException(MessageEnum.COST_ALLOCATION_CALCULATE_EXCEPTION);
            }

            totalProportion = totalProportion.add(proportion);
            totalAmount = totalAmount.add(amount);
        }

        // 比例总和必须为 1
        if (totalProportion.setScale(4, RoundingMode.HALF_UP)
                .compareTo(BigDecimal.ONE) != 0) {
            throw new CostAllocationException(MessageEnum.COST_ALLOCATION_PROPORTION_EXCEPTION);
        }

        // 金额总和必须等于报销总金额
        if (totalAmount.setScale(2, RoundingMode.HALF_UP)
                .compareTo(allAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new CostAllocationException(MessageEnum.COST_ALLOCATION_AMOUNT_EXCEPTION);
        }
    }
}
