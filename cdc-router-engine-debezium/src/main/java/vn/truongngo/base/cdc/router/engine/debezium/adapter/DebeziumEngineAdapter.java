package vn.truongngo.base.cdc.router.engine.debezium.adapter;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.exception.ConfigurationException;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.core.spi.CdcRouter;
import vn.truongngo.base.cdc.router.engine.debezium.converter.DebeziumConverter;
import vn.truongngo.base.cdc.router.engine.debezium.factory.DebeziumPropertiesFactory;
import vn.truongngo.base.cdc.router.engine.debezium.factory.DebeziumStrategyFactory;
import vn.truongngo.base.cdc.router.engine.debezium.strategy.DebeziumEventStrategy;
import vn.truongngo.base.cdc.router.engine.spi.CdcSourceEngine;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@Scope("prototype") // Mỗi Connector Config sẽ tạo 1 instance mới
@RequiredArgsConstructor
public class DebeziumEngineAdapter implements CdcSourceEngine {

    private final DebeziumConverter converter;
    private final DebeziumStrategyFactory strategyFactory;
    private final CdcRouter router;

    // State
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<Throwable> lastError = new AtomicReference<>();
    private DebeziumEngine<ChangeEvent<String, String>> engine;
    private ExecutorService executor;
    private DebeziumEventStrategy currentStrategy;
    private String name;

    @Override
    public void configure(CdcConnectorConfig config) {
        this.name = config.getConnectorName();

        String type = config.getSourceProperties().get("connector.type");

        if (type == null || type.isEmpty()) {
            throw new ConfigurationException("Missing required property 'connector.type' in source configuration for: " + name);
        }

        // 2. Factory sẽ ném exception nếu type không được hỗ trợ (vd: "oracle")
        this.currentStrategy = strategyFactory.getStrategy(type);

        log.info("[{}] Initialized with strategy: {}", name, currentStrategy.getClass().getSimpleName());

        // 2. Tạo Properties (Load class MySQL Connector)
        Properties props = DebeziumPropertiesFactory.createProperties(config);

        // 3. Tạo Debezium Engine
        this.engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(this::handleEvent)
                .using(this::handleCompletion)
                .build();
    }

    @Override
    public void start() {
        if (running.get()) {
            log.warn("Connector [{}] is already running.", name);
            return;
        }

        // Reset error state before starting
        this.lastError.set(null);
        this.running.set(true);
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.execute(engine);
        log.info("Connector [{}] started.", name);
    }

    @Override
    public void stop() {
        if (!running.get()) return;
        log.info("Stopping connector [{}]...", name);
        try {
            if (engine != null) {
                engine.close();
            }
            if (executor != null) {
                executor.shutdown();
            }
        } catch (IOException e) {
            log.error("Error stopping engine: {}", name, e);
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isFailed() {
        return lastError.get() != null;
    }

    @Override
    public Throwable getLastError() {
        return lastError.get();
    }

    @Override
    public String getEngineType() {
        return "debezium";
    }

    @Override
    public String getName() {
        return name;
    }

    private void handleEvent(ChangeEvent<String, String> rawEvent) {
        CdcEventContext context = converter.convert(rawEvent, this.currentStrategy);
        if (context != null) {
            router.route(context);
        }
    }

    private void handleCompletion(boolean success, String message, Throwable error) {
        this.running.set(false);
        if (success) {
            log.info("Connector [{}] stopped gracefully. Message: {}", name, message);
        } else {
            log.error("Connector [{}] stopped with ERROR. Message: {}", name, message, error);
            this.lastError.set(error);
            // Có thể bắn event cảnh báo vào hệ thống monitoring ở đây
        }
    }
}
