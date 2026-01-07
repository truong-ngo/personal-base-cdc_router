package vn.truongngo.base.cdc.router.engine.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.exception.ConfigurationException;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.core.model.config.SinkDefinition;
import vn.truongngo.base.cdc.router.sink.factory.SinkFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Component responsible for validating connector configurations.
 * <p>
 * Component chịu trách nhiệm kiểm tra tính hợp lệ của cấu hình connector.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigValidator {

    private final SinkFactory sinkFactory;

    /**
     * Validates a list of connector configurations.
     * <p>
     * Kiểm tra danh sách cấu hình connector.
     *
     * @param configs The list of configurations to validate.
     * @throws ConfigurationException If any validation error occurs.
     */
    public void validate(List<CdcConnectorConfig> configs) {
        List<String> errors = new ArrayList<>();
        Set<String> registeredSinkTypes = sinkFactory.getRegisteredSinks().keySet();

        for (int i = 0; i < configs.size(); i++) {
            CdcConnectorConfig config = configs.get(i);
            String configContext = String.format("Config[%d] '%s'", i, config.getConnectorName());

            // 1. Validate Basic Info
            if (config.getConnectorName() == null || config.getConnectorName().trim().isEmpty()) {
                errors.add(configContext + ": 'connectorName' is required.");
            }

            // 2. Validate Source Properties
            if (config.getSourceProperties() == null || config.getSourceProperties().isEmpty()) {
                errors.add(configContext + ": 'sourceProperties' must not be empty.");
            } else {
                if (!config.getSourceProperties().containsKey("connector.type")) {
                    errors.add(configContext + ": sourceProperties must contain 'connector.type'.");
                }
            }

            // 3. Validate Sinks
            Map<String, List<SinkDefinition>> sinksMap = config.getSinks();
            if (sinksMap != null) {
                sinksMap.forEach((tableName, sinks) -> {
                    if (sinks == null || sinks.isEmpty()) {
                        errors.add(configContext + ": Table '" + tableName + "' has no sinks defined.");
                    } else {
                        for (SinkDefinition sink : sinks) {
                            validateSink(sink, registeredSinkTypes, errors, configContext + " -> Table '" + tableName + "'");
                        }
                    }
                });
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Configuration Validation Failed:\n");
            errors.forEach(e -> sb.append(" - ").append(e).append("\n"));
            throw new ConfigurationException(sb.toString());
        }
        
        log.info("Configuration validation passed for {} connectors.", configs.size());
    }

    private void validateSink(SinkDefinition sink, Set<String> validTypes, List<String> errors, String context) {
        if (sink.getName() == null || sink.getName().isEmpty()) {
            errors.add(context + ": Sink name is missing.");
        }
        
        String type = sink.getType();
        if (type == null || type.isEmpty()) {
            errors.add(context + ": Sink type is missing for sink '" + sink.getName() + "'.");
        } else if (!validTypes.contains(type)) {
            errors.add(context + ": Unknown sink type '" + type + "'. Supported types: " + validTypes);
        }

        // Validate specific sink properties (Basic check)
        if ("kafka".equals(type)) {
            if (sink.getProperties() == null || !sink.getProperties().containsKey("topic")) {
                errors.add(context + ": Kafka sink '" + sink.getName() + "' missing required property 'topic'.");
            }
        }
    }
}
