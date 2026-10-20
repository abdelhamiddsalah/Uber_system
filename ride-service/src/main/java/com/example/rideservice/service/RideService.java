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


    private RideResponse mapToResponse(Ride ride) {
        RideResponse response = new RideResponse();

        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());

        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());

        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());

        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());

        return response;
    }


    private double calculateEstimateFare(RideRequest request) {

        double distanceInKm = calculateDistance(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude()
        );

        double baseFare = 15.0;
        double pricePerKm = 5.0;

        return baseFare + (distanceInKm * pricePerKm);
    }

    private double calculateDistance(
            double pickupLatitude,
            double pickupLongitude,
            double dropLatitude,
            double dropLongitude) {

        final double EARTH_RADIUS_KM = 6371.0;

        double latDistance = Math.toRadians(
                dropLatitude - pickupLatitude
        );

        double lonDistance = Math.toRadians(
                dropLongitude - pickupLongitude
        );

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(pickupLatitude))
                * Math.cos(Math.toRadians(dropLatitude))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS_KM * c;
    }

}
