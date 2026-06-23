package com.wtu.syserver.convert;

import com.wtu.syserver.dto.TripDTO;
import com.wtu.syserver.entity.Trip;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TripConvert {

    TripDTO toDTO(Trip entity);

    List<TripDTO> toDTOs(List<Trip> entities);

    Trip toEntity(TripDTO dto);

    List<Trip> toEntities(List<TripDTO> dtoList);
}
