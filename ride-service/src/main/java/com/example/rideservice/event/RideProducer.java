package com.example.rideservice.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideProducer {

    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    private static final String TOPIC = "ride.requested";

    public void publishRideRequested(RideRequestedEvent event) {

        kafkaTemplate.send(TOPIC, event);
    }

}
