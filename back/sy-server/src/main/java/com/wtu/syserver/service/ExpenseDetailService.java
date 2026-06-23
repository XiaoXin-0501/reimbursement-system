package com.wtu.syserver.service;


import com.wtu.syserver.dto.ExpenseDetailDTO;
import com.wtu.syserver.entity.ExpenseDetail;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseDetailService {
    List<String> insertExpenseDetail(List<ExpenseDetailDTO> expenseDetailDTOS);

    int deleteExpenseDetailBySubsidyInfoId(List<String> subsidyInfoIds);

    int deleteExpenseDetailByReimId(String ReimId);

    List<ExpenseDetail> getExpenseDetailListBySubsidyInfoId(String subsidyInfoId);

    /**
     * 验证报销明细
     *
     * @param expenseDetailDTOS 报销明细
     * @return true: 合法 false: 不合法
     */
    Boolean validateExpenseDetail(List<ExpenseDetailDTO> expenseDetailDTOS);

    /**
     * 批量验证报销明细
     *
     * @param expenseDetailDTOS 报销明细
     * @return 合计金额
     */
    BigDecimal validateAndGetTotalAmount(List<ExpenseDetailDTO> expenseDetailDTOS);

    /**
     * 批量验证 meal补助
     *
     * @param expenseDetailDTOS 报销明细
     * @return meal补助总金额
     */
    BigDecimal validateAndGetMealAmount(List<ExpenseDetailDTO> expenseDetailDTOS);

    /**
     * 批量验证 transportation补助
     *
     * @param expenseDetailDTOS 报销明细
     * @return transportation补助总金额
     */
    BigDecimal validateAndGetTransportationAmount(List<ExpenseDetailDTO> expenseDetailDTOS);

    /**
     * 批量验证 communication补助
     *
     * @param expenseDetailDTOS 报销明细
     * @return communication补助总金额
     */
    BigDecimal validateAndGetCommunicationAmount(List<ExpenseDetailDTO> expenseDetailDTOS);
}
