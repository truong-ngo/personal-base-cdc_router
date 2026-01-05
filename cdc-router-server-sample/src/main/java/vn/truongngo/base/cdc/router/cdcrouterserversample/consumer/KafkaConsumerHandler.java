package vn.truongngo.base.cdc.router.cdcrouterserversample.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumerHandler {

    @KafkaListener(topics = "cdc.events.notes", groupId = "cdc-router-consumer-group")
    public void consume(String message) {
        log.info("Received message from topic cdc.events.notes: {}", message);
    }
}
