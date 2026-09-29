package com.example.matchingservice.service;

import com.example.matchingservice.event.RideRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public  class MatchingConsumer {

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service"
    )
    public void consumeRideRequested(RideRequestedEvent event) {

        System.out.println("Ride requested!");
        System.out.println("Ride ID: " + event.getRideId());
        System.out.println("Rider ID: " + event.getRiderId());

        System.out.println(
                "Pickup: " +
                        event.getPickupLatitude() +
                        ", " +
                        event.getPickupLongitude()
        );

        System.out.println(
                "Drop: " +
                        event.getDropLatitude() +
                        ", " +
                        event.getDropLongitude()
        );
    }
}