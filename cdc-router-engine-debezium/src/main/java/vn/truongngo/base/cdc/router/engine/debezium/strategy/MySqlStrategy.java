package vn.truongngo.base.cdc.router.engine.debezium.strategy;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class MySqlStrategy implements DebeziumEventStrategy {

    @Override
    public String getSupportedType() {
        return "mysql";
    }

    @Override
    public String generateEventId(String connectorName, JsonNode source) {
        String file = (source != null && source.has("file")) ? source.get("file").asString() : "unknown";
        String pos = (source != null && source.has("pos")) ? source.get("pos").asString() : "0";
        return String.format("%s:%s:%s", connectorName, file, pos);
    }
}