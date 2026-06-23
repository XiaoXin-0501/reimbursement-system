package com.wtu.syserver.controller;

import com.wtu.syserver.common.result.Result;
import com.wtu.syserver.entity.CostAllocation;
import com.wtu.syserver.service.CostAllocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cost/allocation")
@AllArgsConstructor
public class CostAllocationController {
    private final CostAllocationService costAllocationService;

    @GetMapping("/get/{reimId}")
    public Result<List<CostAllocation>> getCostAllocation(@PathVariable String reimId) {
        return Result.success(costAllocationService.getCostAllocationListByReimId(reimId));
    }
}
