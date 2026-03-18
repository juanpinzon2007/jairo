package com.jairo.inventory.inventory.event;

import com.jairo.inventory.shared.events.InventoryMovementEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InventoryMovementEventPublisher {

    private final KafkaTemplate<String, InventoryMovementEvent> kafkaTemplate;
    private final String topic;

    public InventoryMovementEventPublisher(KafkaTemplate<String, InventoryMovementEvent> kafkaTemplate,
                                           @Value("${app.kafka.topics.inventory-movements:inventory.movements}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public Mono<Void> publish(InventoryMovementEvent event) {
        return Mono.fromFuture(kafkaTemplate.send(topic, event.productId().toString(), event))
                .then();
    }
}
