package vn.truongngo.base.cdc.router.engine.debezium.converter;

import io.debezium.engine.ChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.engine.debezium.strategy.DebeziumEventStrategy;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumConverter {

    private final ObjectMapper objectMapper;

    public CdcEventContext convert(ChangeEvent<String, String> rawEvent, DebeziumEventStrategy strategy) {
        try {
            if (rawEvent.value() == null) return null; // Tombstone event

            JsonNode keyNode = (rawEvent.key() != null) ? objectMapper.readTree(rawEvent.key()) : null;
            JsonNode payloadNode = objectMapper.readTree(rawEvent.value());
            JsonNode sourceNode = payloadNode.get("source");

            String connectorName = sourceNode.has("name") ? sourceNode.get("name").asText() : "unknown";
            String table = sourceNode.has("table") ? sourceNode.get("table").asText() : "unknown";
            String db = sourceNode.has("db") ? sourceNode.get("db").asText() : "";

            // --- STRATEGY DELEGATION ---
            // Gọi MySqlStrategy để lấy file/pos
            String eventId = strategy.generateEventId(connectorName, sourceNode);
            // ---------------------------

            return CdcEventContext.builder()
                    .eventId(eventId)
                    .connectorName(connectorName)
                    .destTableName(db + "." + table)
                    .sourceTimestamp(payloadNode.path("ts_ms").asLong(System.currentTimeMillis()))
                    .key(keyNode)
                    .payload(payloadNode)
                    .build();

        } catch (Exception e) {
            log.error("Conversion error", e);
            return null;
        }
    }
}
