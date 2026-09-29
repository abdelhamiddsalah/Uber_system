package com.example.rideservice.service;

import com.example.rideservice.dto.RideRequest;
import com.example.rideservice.dto.RideResponse;
import com.example.rideservice.event.RideRequestedEvent;
import com.example.rideservice.model.Ride;
import com.example.rideservice.model.RideStatus;
import com.example.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
    private final RideRepository rideRepository;

    public RideResponse requestRide(RideRequest request){
        log.info("New ride request from rider: {}", request.getRiderId());

        //Step 1: save ride to database
        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));

        Ride savedRide = rideRepository.save(ride);

        // Step 2: Publish even to Kafka
        // Matching service will consume this and find nearest driver

       /* RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to Kafka for ride: {}", savedRide.getId());*/

        //Update status to Matching
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);
    }
}
