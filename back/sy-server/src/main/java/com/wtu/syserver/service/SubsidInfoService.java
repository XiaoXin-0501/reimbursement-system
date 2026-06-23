package com.wtu.syserver.service;

import com.wtu.syserver.dto.SubsidyInfoDTO;
import com.wtu.syserver.entity.SubsidyInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SubsidInfoService {
    List<String> insertSubsidInfo(List<SubsidyInfoDTO> subsidyInfoDTOS);

    List<SubsidyInfo> getSubsidyInfoListByReimId(String reimId);

    BigDecimal validateAndGetTotalAmount(SubsidyInfoDTO subsidyInfoDTO);

    int deleteSubsidInfoByReimId(String reimId);
}
