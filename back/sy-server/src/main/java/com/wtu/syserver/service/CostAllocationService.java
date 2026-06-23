package com.wtu.syserver.service;

import com.wtu.syserver.dto.CostAllocationDTO;
import com.wtu.syserver.entity.CostAllocation;

import java.math.BigDecimal;
import java.util.List;

public interface CostAllocationService {
    /**
     * 插入费用分摊数据
     *
     * @param costAllocationDTOS 费用分摊数据
     * @return 费用分摊id
     */
    List<String> insertCostAllocation(List<CostAllocationDTO> costAllocationDTOS);

    int deleteCostAllocationByReimId(String reimId);

    /**
     * 获取费用分摊列表
     *
     * @param reimId 报销单id
     * @return 费用分摊列表
     */
    List<CostAllocation> getCostAllocationListByReimId(String reimId);

    /**
     * 验证费用分摊的合理性
     *
     * @param allAmount          总金额
     * @param costAllocationDTOS 费用分摊列表
     */
    void validateCostAllocation(BigDecimal allAmount, List<CostAllocationDTO> costAllocationDTOS);
}
