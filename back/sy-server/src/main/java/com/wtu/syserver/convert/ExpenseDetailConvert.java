package com.wtu.syserver.convert;

import com.wtu.syserver.dto.ExpenseDetailDTO;
import com.wtu.syserver.entity.ExpenseDetail;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseDetailConvert {

    ExpenseDetailDTO toDTO(ExpenseDetail entity);

    List<ExpenseDetailDTO> toDTOs(List<ExpenseDetail> entities);

    ExpenseDetail toEntity(ExpenseDetailDTO dto);

    List<ExpenseDetail> toEntities(List<ExpenseDetailDTO> dtoList);
}
