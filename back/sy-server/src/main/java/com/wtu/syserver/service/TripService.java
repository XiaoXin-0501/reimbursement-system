package com.wtu.syserver.service;

import com.wtu.syserver.dto.TripDTO;
import com.wtu.syserver.entity.Trip;

import java.util.List;

public interface TripService {
    List<String> insertTrip(List<TripDTO> tripDTOS);

    List<Trip> getTripListByReimId(String reimId);

    int deleteTripByReimId(String reimId);
}
