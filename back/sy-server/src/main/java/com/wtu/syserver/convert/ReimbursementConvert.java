package com.wtu.syserver.convert;

import com.wtu.syserver.dto.ReimbursementDTO;
import com.wtu.syserver.entity.Reimbursement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReimbursementConvert {

    Reimbursement toEntity(ReimbursementDTO dto);


    ReimbursementDTO toDTO(Reimbursement entity);

}