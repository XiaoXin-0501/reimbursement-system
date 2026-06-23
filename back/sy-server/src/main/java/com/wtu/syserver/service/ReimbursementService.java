package com.wtu.syserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wtu.syserver.dto.ReimbursementDetailDTO;
import com.wtu.syserver.dto.ReimbursementPageQueryDTO;
import com.wtu.syserver.entity.Reimbursement;

import java.math.BigDecimal;

public interface ReimbursementService {
    String insertReimbursement(ReimbursementDetailDTO reimbursementDetailDTO);

    int deleteReimbursementByReimId(String reimId);

    String insertReimbursementDetail(ReimbursementDetailDTO reimbursementDetailDTO);

    int deleteReimbursementDetailByReimId(String reimId);

    /**
     * 多条件分页查询，直接返回Page<Reimbursement>
     */
    Page<Reimbursement> getReimbursementPage(ReimbursementPageQueryDTO queryDTO);

    BigDecimal getTotalAllowance(ReimbursementDetailDTO reimbursementDetailDTO);

    BigDecimal getMealAllowance(ReimbursementDetailDTO reimbursementDetailDTO);

    BigDecimal getTransportationAllowance(ReimbursementDetailDTO reimbursementDetailDTO);

    BigDecimal getCommunicationAllowance(ReimbursementDetailDTO reimbursementDetailDTO);
}
