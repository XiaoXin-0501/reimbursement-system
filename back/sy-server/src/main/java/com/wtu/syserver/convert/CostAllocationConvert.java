package com.wtu.syserver.convert;

import com.wtu.syserver.dto.CostAllocationDTO;
import com.wtu.syserver.entity.CostAllocation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CostAllocationConvert {

    CostAllocationDTO toDTO(CostAllocation entity);

    List<CostAllocationDTO> toDTOList(List<CostAllocation> entityList);

    CostAllocation toEntity(CostAllocationDTO dto);

    List<CostAllocation> toEntityList(List<CostAllocationDTO> dtoList);
}
