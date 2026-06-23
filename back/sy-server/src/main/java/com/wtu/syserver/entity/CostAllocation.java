package com.wtu.syserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 费用分摊表实体类
 */
@Data
@TableName("cost_allocation")
public class CostAllocation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID：费用分摊记录唯一标识
     */
    @TableId(type = IdType.INPUT)
    private String id;

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
