package com.wtu.syserver.controller;

import com.wtu.syserver.common.result.Result;
import com.wtu.syserver.entity.ExpenseDetail;
import com.wtu.syserver.service.ExpenseDetailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expense")
@AllArgsConstructor
public class ExpenseDetailController {
    private final ExpenseDetailService expenseDetailService;

    @GetMapping("/get/{subsidyInfoId}")
    public Result<List<ExpenseDetail>> getExpenseDetail(@PathVariable String subsidyInfoId) {
        return Result.success(expenseDetailService.getExpenseDetailListBySubsidyInfoId(subsidyInfoId));
    }
}
