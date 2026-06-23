package com.wtu.syserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wtu.syserver.cache.RedisUtil;
import com.wtu.syserver.cache.RedissonUtil;
import com.wtu.syserver.common.constants.ExpireTimeConstant;
import com.wtu.syserver.common.constants.RedisConstant;
import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.RedisKeyEnum;
import com.wtu.syserver.common.exception.ParamInvalidException;
import com.wtu.syserver.common.exception.ReimbursementException;
import com.wtu.syserver.common.idempotent.IdempotentManager;
import com.wtu.syserver.common.utils.CacheKeyUtils;
import com.wtu.syserver.common.utils.IdUtils;
import com.wtu.syserver.convert.ReimbursementConvert;
import com.wtu.syserver.dto.*;
import com.wtu.syserver.entity.Reimbursement;
import com.wtu.syserver.entity.SubsidyInfo;
import com.wtu.syserver.mapper.ReimbursementMapper;
import com.wtu.syserver.service.*;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class ReimbursementServiceImpl implements ReimbursementService {
    private final IdempotentManager idempotentManager;

    private final RedisUtil redisUtil;
    private final RedissonUtil redissonUtil;

    private final ReimbursementMapper reimbursementMapper;
    private final ReimbursementConvert reimbursementConvert;

    private final CostAllocationService costAllocationService;
    private final TripService tripService;
    private final ExpenseDetailService expenseDetailService;
    private final SubsidInfoService subsidInfoService;

    @Override
    public String insertReimbursement(ReimbursementDetailDTO reimbursementDetailDTO) {
        if (reimbursementDetailDTO == null) {
            return null;
        }
        Reimbursement reimbursement = reimbursementConvert.toEntity(reimbursementDetailDTO.getReimbursementDTO());
        // 创建id和报销单号
        String reimbursementId = IdUtils.getSnowflakeId();
        String reimbursementNo = IdUtils.getReimburseNo();
        // 计算总金额，交通补助总金额，通信补助总金额，餐饮补助总金额
        BigDecimal totalAllowance = getTotalAllowance(reimbursementDetailDTO);
        BigDecimal transportationAllowance = getTransportationAllowance(reimbursementDetailDTO);
        BigDecimal communicationAllowance = getCommunicationAllowance(reimbursementDetailDTO);
        BigDecimal mealAllowance = getMealAllowance(reimbursementDetailDTO);
        // 设置报销单信息
        reimbursement.setSubsidyTotalAmount(totalAllowance);
        reimbursement.setMealAllowanceTotal(mealAllowance);
        reimbursement.setTransportationAllowanceTotal(transportationAllowance);
        reimbursement.setCommunicationAllowanceTotal(communicationAllowance);
        reimbursement.setId(reimbursementId);
        reimbursement.setReimbursementNo(reimbursementNo);
        reimbursement.setCreateTime(new Date());
        // 插入报销单
        reimbursementMapper.insert(reimbursement);
        return reimbursementId;
    }

    @Override
    public int deleteReimbursementByReimId(String reimId) {
        if (reimId == null || reimId.isEmpty()) {
            return 0;
        }
        List<SubsidyInfo> subsidyInfos = subsidInfoService.getSubsidyInfoListByReimId(reimId);
        String[] subsidyInfokeys = subsidyInfos.stream()
                .map(info -> RedisKeyEnum.EXPENSE_LIST.getKey(info.getId()))
                .toArray(String[]::new);
        //删除缓存
        redisUtil.deleteByPrefix(RedisKeyEnum.REIM_PAGE.getKey());
        redisUtil.delete(RedisKeyEnum.ALLOCATION_LIST.getKey(reimId));
        redisUtil.delete(subsidyInfokeys);
        redisUtil.delete(RedisKeyEnum.TRIP_LIST.getKey(reimId));
        redisUtil.delete(RedisKeyEnum.SUBSIDY_LIST.getKey(reimId));

        //删除数据库
        expenseDetailService.deleteExpenseDetailByReimId(reimId);
        subsidInfoService.deleteSubsidInfoByReimId(reimId);
        tripService.deleteTripByReimId(reimId);
        costAllocationService.deleteCostAllocationByReimId(reimId);
        int row = deleteReimbursementDetailByReimId(reimId);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RedisConstant.DELETE_DELAY_TIME);
                redisUtil.deleteByPrefix(RedisKeyEnum.REIM_PAGE.getKey());
                redisUtil.delete(RedisKeyEnum.ALLOCATION_LIST.getKey(reimId));
                redisUtil.delete(subsidyInfokeys);
                redisUtil.delete(RedisKeyEnum.TRIP_LIST.getKey(reimId));
                redisUtil.delete(RedisKeyEnum.SUBSIDY_LIST.getKey(reimId));
            } catch (InterruptedException ignored) {
            }
        });
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String insertReimbursementDetail(ReimbursementDetailDTO reimbursementDetailDTO) {
        if (reimbursementDetailDTO == null ||
                reimbursementDetailDTO.getReimbursementDTO() == null ||
                reimbursementDetailDTO.getIdempotentToken() == null ||
                reimbursementDetailDTO.getTripDTOS() == null ||
                reimbursementDetailDTO.getCostAllocationDTOS() == null) {
            throw new ParamInvalidException(MessageEnum.PARAM_INVALID_ERROR);
        }
        if (!idempotentManager.checkAndSet(reimbursementDetailDTO.getIdempotentToken())) {
            throw new ReimbursementException(MessageEnum.REIMBURSE_SUBMIT_REPEAT);
        }
        try {
            // 获取reimbursement,插入基础信息
            ReimbursementDTO reimbursementDTO = reimbursementDetailDTO.getReimbursementDTO();
            String reimbursementId = insertReimbursement(reimbursementDetailDTO);

            // 获取行程数组
            List<TripDTO> tripDTOS = reimbursementDetailDTO.getTripDTOS();

            // 插入行程，按顺序获取id
            tripDTOS.forEach(tripDTO -> tripDTO.setReimId(reimbursementId));
            List<String> tripIdList = tripService.insertTrip(tripDTOS);

            // 获取所有补助信息并批量设置tripId和reimbursementId
            List<SubsidyInfoDTO> allSubsidyList = new ArrayList<>();
            for (int i = 0; i < tripIdList.size(); i++) {
                List<SubsidyInfoDTO> subsidyInfoDTOS = tripDTOS.get(i).getSubsidyInfoDTOS();
                String tripId = tripIdList.get(i);
                subsidyInfoDTOS.forEach(subsidyInfoDTO -> {
                    subsidyInfoDTO.setTripId(tripId);
                    subsidyInfoDTO.setReimId(reimbursementId);
                });
                allSubsidyList.addAll(subsidyInfoDTOS);
            }

            // 插入补助信息，按顺序获取id
            List<String> subsidyIdList = subsidInfoService.insertSubsidInfo(allSubsidyList);

            // 获取所有费用信息并批量设置subsidyId和reimbursementId
            List<ExpenseDetailDTO> allExpenseList = new ArrayList<>();
            for (int i = 0; i < allSubsidyList.size(); i++) {
                List<ExpenseDetailDTO> expenseDetailDTOS = allSubsidyList.get(i).getExpenseDetailDTOS();
                String subsidyId = subsidyIdList.get(i);
                expenseDetailDTOS.forEach(expenseDetailDTO -> {
                    expenseDetailDTO.setSubsidyInfoId(subsidyId);
                    expenseDetailDTO.setReimId(reimbursementId);
                });
                allExpenseList.addAll(expenseDetailDTOS);
            }

            // 插入费用信息，按顺序获取id
            List<String> expenseIdList = expenseDetailService.insertExpenseDetail(allExpenseList);

            //验证费用分摊的合法性
            costAllocationService.validateCostAllocation(getTotalAllowance(reimbursementDetailDTO), reimbursementDetailDTO.getCostAllocationDTOS());
            // 插入费用分摊信息
            List<CostAllocationDTO> costAllocationDTOS = reimbursementDetailDTO.getCostAllocationDTOS();
            costAllocationDTOS.forEach(costAllocationDTO -> {
                costAllocationDTO.setReimId(reimbursementId);
            });
            costAllocationService.insertCostAllocation(costAllocationDTOS);

            return reimbursementId;
        } catch (Exception e) {
            throw new ReimbursementException(MessageEnum.REIMBURSE_DETAIL_INSERT_FAIL);
        }
    }

    @Override
    public int deleteReimbursementDetailByReimId(String reimId) {
        String key = RedisKeyEnum.REIM_PAGE.getKey();
        redisUtil.deleteByPrefix(key);

        LambdaQueryWrapper<Reimbursement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reimbursement::getId, reimId);
        int rows = reimbursementMapper.delete(wrapper);

        // 延迟二次删除（防止回填）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(RedisConstant.DELETE_DELAY_TIME);
                redisUtil.deleteByPrefix(key);
            } catch (InterruptedException ignored) {
            }
        });
        // 删除匹配条件的数据
        return rows;
    }

    @Override
    public Page<Reimbursement> getReimbursementPage(ReimbursementPageQueryDTO queryDTO) {
        // 构建缓存Key（MD5）
        String cacheKey = CacheKeyUtils.pageKey(
                RedisKeyEnum.REIM_PAGE.getKey(),
                queryDTO
        );

        // 先查缓存（防穿透基础）
        Page<Reimbursement> cache = redisUtil.get(
                cacheKey,
                new TypeReference<Page<Reimbursement>>() {
                }
        );

        if (cache != null && cache.getRecords() != null && !cache.getRecords().isEmpty()) {
            return cache;
        }

        // 分布式锁（防击穿核心）
        RLock lock = redissonUtil.getRedisson().getLock(RedisKeyEnum.REIM_PAGE_LOCK.getKey(cacheKey));

        boolean locked = false;
        try {
            // 只对“获取锁”做重试（3次 + 退避）
            for (int i = 0; i < RedisConstant.TRY_LOCK_TIMES; i++) {
                locked = lock.tryLock(RedisConstant.LOCK_WAIT_TIME, RedisConstant.LOCK_LEASE_TIME, TimeUnit.MILLISECONDS);
                if (locked) {
                    break;
                }
                try {
                    Thread.sleep(20L * (i + 1));
                } catch (InterruptedException ignored) {
                }
            }

            // 没拿到锁：直接查缓存兜底
            if (!locked) {
                return redisUtil.get(cacheKey, new TypeReference<Page<Reimbursement>>() {
                });
            }

            // double check cache
            Page<Reimbursement> doubleCheck = redisUtil.get(cacheKey, new TypeReference<Page<Reimbursement>>() {
                    }
            );

            if (doubleCheck != null && doubleCheck.getRecords() != null && !doubleCheck.getRecords().isEmpty()) {
                return doubleCheck;
            }

            // 建分页查询
            long current = queryDTO.getCurrent() == null ? 1 : queryDTO.getCurrent();
            long size = queryDTO.getSize() == null ? 10 : queryDTO.getSize();

            Page<Reimbursement> page = new Page<>(current, size);

            LambdaQueryWrapper<Reimbursement> wrapper = new LambdaQueryWrapper<>();

            if (StringUtils.hasText(queryDTO.getReimbursementNo())) {
                wrapper.like(Reimbursement::getReimbursementNo, queryDTO.getReimbursementNo());
            }
            if (StringUtils.hasText(queryDTO.getTitle())) {
                wrapper.like(Reimbursement::getTitle, queryDTO.getTitle());
            }
            if (StringUtils.hasText(queryDTO.getReason())) {
                wrapper.like(Reimbursement::getReason, queryDTO.getReason());
            }
            if (StringUtils.hasText(queryDTO.getReimCompanyId())) {
                wrapper.eq(Reimbursement::getReimCompanyId, queryDTO.getReimCompanyId());
            }
            if (StringUtils.hasText(queryDTO.getReimDepartmentId())) {
                wrapper.eq(Reimbursement::getReimDepartmentId, queryDTO.getReimDepartmentId());
            }
            if (StringUtils.hasText(queryDTO.getReimburserId())) {
                wrapper.eq(Reimbursement::getReimburserId, queryDTO.getReimburserId());
            }
            if (StringUtils.hasText(queryDTO.getBusinessTypeId())) {
                wrapper.eq(Reimbursement::getBusinessTypeId, queryDTO.getBusinessTypeId());
            }

            wrapper.orderByDesc(Reimbursement::getCreateTime);

            // 查数据库
            Page<Reimbursement> result = reimbursementMapper.selectPage(page, wrapper);

            // 写缓存（统一交给RedisUtil）
            // 空值缓存
            // TTL随机
            redisUtil.set(cacheKey, result, ExpireTimeConstant.REIM_PAGE_OFFSET_TIME, ExpireTimeConstant.REIM_PAGE_EXPIRE_TIME, TimeUnit.SECONDS);

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public BigDecimal getTotalAllowance(ReimbursementDetailDTO reimbursementDetailDTO) {
        return Stream.of(
                        getMealAllowance(reimbursementDetailDTO),
                        getTransportationAllowance(reimbursementDetailDTO),
                        getCommunicationAllowance(reimbursementDetailDTO)
                ).map(amt -> amt == null ? BigDecimal.ZERO : amt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getMealAllowance(ReimbursementDetailDTO reimbursementDetailDTO) {
        List<ExpenseDetailDTO> list = extractAllExpenseDetail(reimbursementDetailDTO);
        return expenseDetailService.validateAndGetMealAmount(list);
    }

    @Override
    public BigDecimal getTransportationAllowance(ReimbursementDetailDTO reimbursementDetailDTO) {
        List<ExpenseDetailDTO> list = extractAllExpenseDetail(reimbursementDetailDTO);
        return expenseDetailService.validateAndGetTransportationAmount(list);
    }

    @Override
    public BigDecimal getCommunicationAllowance(ReimbursementDetailDTO reimbursementDetailDTO) {
        List<ExpenseDetailDTO> list = extractAllExpenseDetail(reimbursementDetailDTO);
        return expenseDetailService.validateAndGetCommunicationAmount(list);
    }

    private List<ExpenseDetailDTO> extractAllExpenseDetail(ReimbursementDetailDTO dto) {
        if (dto == null || dto.getTripDTOS() == null) {
            return Collections.emptyList();
        }
        return dto.getTripDTOS().stream()
                .filter(Objects::nonNull)
                .flatMap(trip -> Optional.ofNullable(trip.getSubsidyInfoDTOS()).orElse(Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .flatMap(subsidy -> Optional.ofNullable(subsidy.getExpenseDetailDTOS()).orElse(Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
