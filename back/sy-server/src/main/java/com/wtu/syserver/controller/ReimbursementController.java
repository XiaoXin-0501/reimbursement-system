package com.wtu.syserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wtu.syserver.common.result.Result;
import com.wtu.syserver.dto.ReimbursementDetailDTO;
import com.wtu.syserver.dto.ReimbursementPageQueryDTO;
import com.wtu.syserver.entity.Reimbursement;
import com.wtu.syserver.service.ReimbursementService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reimbursement")
@AllArgsConstructor
public class ReimbursementController {
    private final ReimbursementService reimbursementService;

    @PostMapping("/create")
    public Result<String> createReimbursement(@RequestBody ReimbursementDetailDTO reimbursementDetailDTO) {
        String reimbursementId = reimbursementService.insertReimbursementDetail(reimbursementDetailDTO);
        return Result.success(reimbursementId);
    }

    @PostMapping("/page")
    public Result<Page<Reimbursement>> getReimbursementPage(@RequestBody ReimbursementPageQueryDTO queryDTO) {
        Page<Reimbursement> page = reimbursementService.getReimbursementPage(queryDTO);
        return Result.success(page);
    }

    @DeleteMapping("/{reimId}")
    public Result<Object> deleteReimbursement(@PathVariable String reimId) {
        reimbursementService.deleteReimbursementByReimId(reimId);
        return Result.success();
    }

}
