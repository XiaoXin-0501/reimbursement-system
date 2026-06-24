package com.wtu.syserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.common.constants.CityConstant;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.constants.RedisConstant;
import com.wtu.syserver.common.constants.SubsidyConstant;
import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import com.wtu.syserver.common.exception.ExpenseDetailException;
import com.wtu.syserver.common.utils.IdUtils;
import com.wtu.syserver.convert.ExpenseDetailConvert;
import com.wtu.syserver.dto.ExpenseDetailDTO;
import com.wtu.syserver.entity.ExpenseDetail;
import com.wtu.syserver.mapper.ExpenseDetailMapper;
import com.wtu.syserver.service.ExpenseDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ExpenseDetailServiceImpl implements ExpenseDetailService {
    private final RedisUtil redisUtil;

    private final ExpenseDetailMapper expenseDetailMapper;
    private final ExpenseDetailConvert expenseDetailConvert;


    @Override
    public List<String> insertExpenseDetail(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (expenseDetailDTOS == null || expenseDetailDTOS.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExpenseDetail> expenseDetails = expenseDetailConvert.toEntities(expenseDetailDTOS);

        List<String> idList = expenseDetails.stream()
                .map(expenseDetail -> {
                    String id = IdUtils.getSnowflakeId();
                    expenseDetail.setId(id);
                    return id;
                })
                .collect(Collectors.toList());

        expenseDetailMapper.insert(expenseDetails);

        return idList;
    }

    @Override
    public int deleteExpenseDetailBySubsidyInfoId(List<String> subsidyInfoIds) {

        if (subsidyInfoIds == null || subsidyInfoIds.isEmpty()) {
            return 0;
        }

        // 1. 构建 Redis key
        List<String> keysList = subsidyInfoIds.stream()
                .map(RedisKeyEnum.EXPENSE_LIST::getKey)
                .toList();

        String[] keysArray = keysList.toArray(new String[0]);

        // 2. 删除缓存（批量）
        redisUtil.delete(keysArray);

        // 3. 删除数据库
        LambdaQueryWrapper<ExpenseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ExpenseDetail::getSubsidyInfoId, subsidyInfoIds);

        int rows = expenseDetailMapper.delete(wrapper);

        // 4. 延迟双删
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RedisConstant.DELETE_DELAY_TIME);
                redisUtil.delete(keysArray);
            } catch (InterruptedException ignored) {
            }
        });

        return rows;
    }

    @Override
    public int deleteExpenseDetailByReimId(String reimId) {
        redisUtil.incr(RedisKeyEnum.REIM_PAGE_VERSION.getKey());
        LambdaQueryWrapper<ExpenseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseDetail::getReimId, reimId);
        return expenseDetailMapper.delete(wrapper);
    }

    @Override
    public List<ExpenseDetail> getExpenseDetailListBySubsidyInfoId(String subsidyInfoId) {
        List<ExpenseDetail> cache = redisUtil.get(
                RedisKeyEnum.EXPENSE_LIST.getKey(subsidyInfoId),
                new TypeReference<List<ExpenseDetail>>() {
                }
        );
        if (cache != null && !cache.isEmpty()) return cache;

        LambdaQueryWrapper<ExpenseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseDetail::getSubsidyInfoId, subsidyInfoId);

        List<ExpenseDetail> result = expenseDetailMapper.selectList(wrapper);

        redisUtil.set(
                RedisKeyEnum.EXPENSE_LIST.getKey(subsidyInfoId),
                result,
                ExpireTimeConstant.COMMON_OFFSET_TIME,
                ExpireTimeConstant.COMMON_EXPIRE_TIME,
                TimeUnit.SECONDS
        );
        return result;
    }

    @Override
    public Boolean validateExpenseDetail(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (expenseDetailDTOS == null || expenseDetailDTOS.isEmpty()) {
            return true;
        }
        for (ExpenseDetailDTO expenseDetailDTO : expenseDetailDTOS) {
            int cityType = CityConstant.CITY_NO_ITEM_MAP.get(expenseDetailDTO.getCityId()).cityType();

            // 获取该城市最大补助金额
            BigDecimal mealMaxAllowance = SubsidyConstant.MEAL_SUBSIDY_MAP.get(cityType);
            BigDecimal transportationMaxAllowance = SubsidyConstant.TRAFFIC_SUBSIDY;
            BigDecimal communicationMaxAllowance = SubsidyConstant.COMMUNICATION_SUBSIDY;

            // 获取申请的补助金额
            BigDecimal mealAllowance = expenseDetailDTO.getMealAllowance();
            BigDecimal transportationAllowance = expenseDetailDTO.getTransportationAllowance();
            BigDecimal communicationAllowance = expenseDetailDTO.getCommunicationAllowance();

            // 判断申请金额是否合法
            if (
                    mealAllowance.compareTo(mealMaxAllowance) > 0 ||
                            transportationAllowance.compareTo(transportationMaxAllowance) > 0 ||
                            communicationAllowance.compareTo(communicationMaxAllowance) > 0
            ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BigDecimal validateAndGetTotalAmount(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (!validateExpenseDetail(expenseDetailDTOS)) {
            throw new ExpenseDetailException(MessageEnum.EXPENSE_DETAIL_EXCEPTION);
        }

        BigDecimal mealTotal = validateAndGetMealAmount(expenseDetailDTOS);
        BigDecimal transportTotal = validateAndGetTransportationAmount(expenseDetailDTOS);
        BigDecimal communicationTotal = validateAndGetCommunicationAmount(expenseDetailDTOS);

        return mealTotal.add(transportTotal).add(communicationTotal);
    }

    @Override
    public BigDecimal validateAndGetMealAmount(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (!validateExpenseDetail(expenseDetailDTOS)) {
            throw new ExpenseDetailException(MessageEnum.EXPENSE_DETAIL_EXCEPTION);
        }
        return expenseDetailDTOS.stream()
                .map(dto -> dto.getMealAllowance() == null ? BigDecimal.ZERO : dto.getMealAllowance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal validateAndGetTransportationAmount(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (!validateExpenseDetail(expenseDetailDTOS)) {
            throw new ExpenseDetailException(MessageEnum.EXPENSE_DETAIL_EXCEPTION);
        }
        return expenseDetailDTOS.stream()
                .map(dto -> dto.getTransportationAllowance() == null ? BigDecimal.ZERO : dto.getTransportationAllowance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal validateAndGetCommunicationAmount(List<ExpenseDetailDTO> expenseDetailDTOS) {
        if (!validateExpenseDetail(expenseDetailDTOS)) {
            throw new ExpenseDetailException(MessageEnum.EXPENSE_DETAIL_EXCEPTION);
        }
        return expenseDetailDTOS.stream()
                .map(dto -> dto.getCommunicationAllowance() == null ? BigDecimal.ZERO : dto.getCommunicationAllowance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
