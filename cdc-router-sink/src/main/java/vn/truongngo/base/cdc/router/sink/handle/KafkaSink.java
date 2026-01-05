package vn.truongngo.base.cdc.router.sink.handle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.exception.SinkExecutionException;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.core.spi.CdcSink;
import vn.truongngo.base.cdc.router.sink.serializer.EventSerializer;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Sink implementation that sends CDC events to a Kafka topic.
 * <p>
 * Triển khai Sink gửi các sự kiện CDC đến một Kafka topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaSink implements CdcSink {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventSerializer serializer;

    @Override
    public String getType() {
        return "kafka";
    }

    @Override
    public void execute(Map<String, Object> props, CdcEventContext event) {
        String topic = (String) props.get("topic");
        if (topic == null || topic.isEmpty()) {
            throw new SinkExecutionException("Missing 'topic' in kafka sink properties", false);
        }

        // Use serializer to convert event to string
        String messageKey = serializer.serializeKey(event);
        String messageValue = serializer.serializeValue(event);

        try {
            // Synchronous send to ensure data is persisted before committing offset
            kafkaTemplate.send(topic, messageKey, messageValue).get();
            log.info("Sent event {} to topic {}", event.getEventId(), topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SinkExecutionException("Interrupted while sending to Kafka", e, true);
        } catch (ExecutionException e) {
            throw new SinkExecutionException("Failed to send to Kafka topic: " + topic, e.getCause(), true);
        }
    }
}
