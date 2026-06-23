package com.wtu.syserver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostAllocationDTO {
    /**
     * 报销单ID：关联报销单表主键ID
     */
    private String reimId;
    
    /**
     * 费用归属ID：关联费用归属公司或部门 ID
     */
    private String costOwnerId;

    /**
     * 费用归属名称：费用归属名称
     */
    private String costOwnerName;

    /**
     * 项目ID：关联项目控件的 projectId
     */
    private String projectId;

    /**
     * 项目名称：项目名称
     */
    private String projectName;

    /**
     * 分摊比例：分摊比例，范围 0-1，最多保留四位小数
     */
    private BigDecimal proportion;

    /**
     * 分摊金额：分摊金额，单位为元
     */
    private BigDecimal amount;
}
