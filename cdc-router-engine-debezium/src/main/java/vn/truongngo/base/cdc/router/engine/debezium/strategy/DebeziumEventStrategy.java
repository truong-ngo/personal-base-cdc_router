package vn.truongngo.base.cdc.router.engine.debezium.strategy;

import tools.jackson.databind.JsonNode;

public interface DebeziumEventStrategy {
    String getSupportedType();
    String generateEventId(String connectorName, JsonNode sourceNode);
}
