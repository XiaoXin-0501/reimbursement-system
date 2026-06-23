package com.wtu.syserver.controller;

import com.wtu.syserver.common.result.Result;
import com.wtu.syserver.entity.SubsidyInfo;
import com.wtu.syserver.service.SubsidInfoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subsidy")
@AllArgsConstructor
public class SubsidInfoController {
    private final SubsidInfoService subsidInfoService;

    @GetMapping("/get/{reimId}")
    public Result<List<SubsidyInfo>> getSubsidyInfo(@PathVariable String reimId) {
        return Result.success(subsidInfoService.getSubsidyInfoListByReimId(reimId));
    }

}
