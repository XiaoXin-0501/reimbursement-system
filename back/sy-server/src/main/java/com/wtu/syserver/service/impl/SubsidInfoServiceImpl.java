package com.wtu.syserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.constants.RedisConstant;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import com.wtu.syserver.common.utils.IdUtils;
import com.wtu.syserver.convert.SubsidyInfoConvert;
import com.wtu.syserver.dto.SubsidyInfoDTO;
import com.wtu.syserver.entity.SubsidyInfo;
import com.wtu.syserver.mapper.SubsidyInfoMapper;
import com.wtu.syserver.service.ExpenseDetailService;
import com.wtu.syserver.service.SubsidInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SubsidInfoServiceImpl implements SubsidInfoService {
    private final RedisUtil redisUtil;

    private final SubsidyInfoMapper subsidyInfoMapper;
    private final SubsidyInfoConvert subsidyInfoConvert;
    private final ExpenseDetailService expenseDetailService;

    @Override
    public List<String> insertSubsidInfo(List<SubsidyInfoDTO> subsidyInfoDTOS) {
        if (subsidyInfoDTOS == null || subsidyInfoDTOS.isEmpty()) {
            return Collections.emptyList();
        }

        List<SubsidyInfo> subsidyInfos = new ArrayList<>();
        List<String> idList = subsidyInfoDTOS.stream()
                .map(dto -> {
                    // DTO转实体
                    SubsidyInfo entity = subsidyInfoConvert.toEntity(dto);
                    // 计算总金额并赋值
                    BigDecimal totalAmount = validateAndGetTotalAmount(dto);
                    entity.setApplyAmount(totalAmount);
                    // 生成主键ID并赋值
                    String id = IdUtils.getSnowflakeId();
                    entity.setId(id);
                    subsidyInfos.add(entity);
                    return id;
                })
                .collect(Collectors.toList());

        subsidyInfoMapper.insert(subsidyInfos);
        return idList;
    }

    @Override
    public List<SubsidyInfo> getSubsidyInfoListByReimId(String reimId) {
        List<SubsidyInfo> cache = redisUtil.get(
                RedisKeyEnum.SUBSIDY_LIST.getKey(reimId),
                new TypeReference<List<SubsidyInfo>>() {
                }
        );
        if (cache != null && !cache.isEmpty()) return cache;

        LambdaQueryWrapper<SubsidyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubsidyInfo::getReimId, reimId);
        List<SubsidyInfo> result = subsidyInfoMapper.selectList(wrapper);

        redisUtil.set(
                RedisKeyEnum.SUBSIDY_LIST.getKey(reimId),
                result,
                ExpireTimeConstant.COMMON_OFFSET_TIME,
                ExpireTimeConstant.COMMON_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        return result;
    }

    @Override
    public BigDecimal validateAndGetTotalAmount(SubsidyInfoDTO subsidyInfoDTO) {
        return expenseDetailService.validateAndGetTotalAmount(subsidyInfoDTO.getExpenseDetailDTOS());
    }

    @Override
    public int deleteSubsidInfoByReimId(String reimId) {
        String key = RedisKeyEnum.SUBSIDY_LIST.getKey(reimId);
        redisUtil.delete(key);
        redisUtil.incr(RedisKeyEnum.REIM_PAGE_VERSION.getKey());

        LambdaQueryWrapper<SubsidyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubsidyInfo::getReimId, reimId);
        int rows = subsidyInfoMapper.delete(wrapper);

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
