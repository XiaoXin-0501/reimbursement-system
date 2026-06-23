package com.wtu.syserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 报销单表实体类
 */
@Data
@TableName("reimbursement")
public class Reimbursement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID：报销单唯一标识
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 报销单号：报销单唯一业务编号
     */
    private String reimbursementNo;

    /**
     * 报销标题：报销单标题，长度不超过 500 字
     */
    private String title;

    /**
     * 报销人ID：关联员工控件的 reimburserId
     */
    private String reimburserId;

    /**
     * 报销人姓名：报销人名称
     */
    private String reimburserName;

    /**
     * 报销部门ID：关联报销部门控件的 reimDepartmentId
     */
    private String reimDepartmentId;

    /**
     * 报销部门名称：报销部门名称
     */
    private String reimDepartmentName;

    /**
     * 费用归属公司ID：关联费用归属公司控件的 reimCompanyId
     */
    private String reimCompanyId;

    /**
     * 费用归属公司名称：费用归属公司名称
     */
    private String reimCompanyName;

    /**
     * 业务类型ID：关联业务类型控件的 businessTypeId
     */
    private String businessTypeId;

    /**
     * 业务类型名称：业务类型名称
     */
    private String businessTypeName;

    /**
     * 出差事由：报销事由，长度不超过 500 字
     */
    private String reason;

    /**
     * 备注信息：备注信息，长度不超过 1000 字
     */
    private String remarks;

    /**
     * 报销状态：0=草稿，1=审计，2=通过，3=不通过，4=废除
     */
    private Integer status;

    /**
     * 补助总金额：该报销单所有补助合计金额，单位为元
     */
    private BigDecimal subsidyTotalAmount;

    /**
     * 餐费补助合计：该报销单所有餐费补助合计金额，单位为元
     */
    private BigDecimal mealAllowanceTotal;

    /**
     * 交通补助合计：该报销单所有交通补助合计金额，单位为元
     */
    private BigDecimal transportationAllowanceTotal;

    /**
     * 通讯补助合计：该报销单所有通讯补助合计金额，单位为元
     */
    private BigDecimal communicationAllowanceTotal;

    /**
     * 创建时间：报销单创建日期，格式为年月日
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
