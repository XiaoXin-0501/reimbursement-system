package com.wtu.syserver.dto;

import lombok.Data;

@Data
public class ReimbursementDTO {

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
}
