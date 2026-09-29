package com.example.matchingservice.service;

import com.example.matchingservice.client.LocationServiceClient;
import com.example.matchingservice.dto.NearByDriverResponse;
import com.example.matchingservice.event.RideMatchedEvent;
import com.example.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;

    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    public RideMatchedEvent matchDriverForRide(RideRequestedEvent event) {

        // STEP 1: Find nearby drivers
        List<NearByDriverResponse> nearbyDrivers =
                locationServiceClient.getNearByDrivers(
                        event.getPickupLatitude(),
                        event.getPickupLongitude(),
                        DEFAULT_SEARCH_RADIUS_KM
                );

        if (nearbyDrivers.isEmpty()) {

            log.warn(
                    "No drivers found for ride {}",
                    event.getRideId()
            );

            return null;
        }

        // STEP 2: Find the best driver
        Optional<NearByDriverResponse> bestDriver =
                findBestDriver(nearbyDrivers);

        if (bestDriver.isEmpty()) {

            log.warn(
                    "Could not find suitable driver for ride {}",
                    event.getRideId()
            );

            return null;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        log.info(
                "Driver {} matched with ride {}",
                assignedDriver.getDriverId(),
                event.getRideId()
        );

        // STEP 3: Create RideMatchedEvent
        return new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );
    }

    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers
    ) {

        return drivers.stream()
                .min(
                        Comparator.comparingDouble(
                                NearByDriverResponse::getDistanceInKm
                        )
                );
    }
}