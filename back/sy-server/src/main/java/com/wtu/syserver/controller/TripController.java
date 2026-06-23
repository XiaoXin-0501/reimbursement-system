package com.wtu.syserver.controller;

import com.wtu.syserver.common.result.Result;
import com.wtu.syserver.entity.Trip;
import com.wtu.syserver.service.TripService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trip")
@AllArgsConstructor
public class TripController {
    private final TripService tripService;

    @GetMapping("/get/{reimId}")
    public Result<List<Trip>> getTrip(@PathVariable String reimId) {
        return Result.success(tripService.getTripListByReimId(reimId));
    }
}
