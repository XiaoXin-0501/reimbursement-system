package com.wtu.syserver.dto;

import lombok.Data;

@Data
public class ReimbursementPageQueryDTO {
    /**
     * 当前页，默认1
     */
    private Integer current;
    /**
     * 每页条数，默认10
     */
    private Integer size;

    /**
     * 报销单号
     */
    private String reimbursementNo;
    /**
     * 报销标题
     */
    private String title;
    /**
     * 出差事由
     */
    private String reason;
    /**
     * 费用归属公司ID
     */
    private String reimCompanyId;
    /**
     * 报销部门ID
     */
    private String reimDepartmentId;
    /**
     * 报销人ID
     */
    private String reimburserId;
    /**
     * 业务类型ID
     */
    private String businessTypeId;
}
