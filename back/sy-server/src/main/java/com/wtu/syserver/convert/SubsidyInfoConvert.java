package com.wtu.syserver.convert;

import com.wtu.syserver.dto.SubsidyInfoDTO;
import com.wtu.syserver.entity.SubsidyInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubsidyInfoConvert {

    SubsidyInfoDTO toDTO(SubsidyInfo entity);

    List<SubsidyInfoDTO> toDTOs(List<SubsidyInfo> entities);

    SubsidyInfo toEntity(SubsidyInfoDTO dto);

    List<SubsidyInfo> toEntities(List<SubsidyInfoDTO> dtoList);
}
