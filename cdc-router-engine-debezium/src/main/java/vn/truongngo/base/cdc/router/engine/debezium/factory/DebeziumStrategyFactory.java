package vn.truongngo.base.cdc.router.engine.debezium.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.exception.ConfigurationException;
import vn.truongngo.base.cdc.router.engine.debezium.strategy.DebeziumEventStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DebeziumStrategyFactory {

    private final Map<String, DebeziumEventStrategy> strategyMap = new HashMap<>();

    public DebeziumStrategyFactory(List<DebeziumEventStrategy> strategies) {
        for (DebeziumEventStrategy s : strategies) {
            log.info("Registered Debezium Strategy for DB type: [{}]", s.getSupportedType());
            strategyMap.put(s.getSupportedType(), s);
        }
    }

    public DebeziumEventStrategy getStrategy(String type) {
        if (!strategyMap.containsKey(type)) {
            throw new ConfigurationException(String.format(
                    "No CDC Strategy found for connector type: '%s'. " +
                    "Please implement a strategy class or check your 'connector.type' config. " +
                    "Supported types: %s",
                    type,
                    strategyMap.keySet()
            ));
        }
        return strategyMap.get(type);
    }
}