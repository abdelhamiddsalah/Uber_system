
package com.example.locationservice.controller;

import com.example.locationservice.dto.DriverLocationRequest;
import com.example.locationservice.dto.NearByDriverResponse;
import com.example.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/drivers")
    public void updateDriverLocation(
            @RequestBody DriverLocationRequest request) {

        locationService.updateDriverLocation(request);
    }

    @GetMapping("/drivers/nearby")
    public List<NearByDriverResponse> findNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusInKm) {

        return locationService.findNearbyDrivers(
                latitude,
                longitude,
                radiusInKm
        );
    }
}
