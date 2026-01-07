package vn.truongngo.base.cdc.router.engine.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.engine.loader.ConfigProvider;
import vn.truongngo.base.cdc.router.engine.service.ConnectorOrchestrator;

import java.util.List;

/**
 * Bootstrap class to start the CDC Engine on application startup.
 * <p>
 * Class khởi động để chạy CDC Engine khi ứng dụng bắt đầu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdcEngineBootstrap implements CommandLineRunner {

    private final ConfigProvider configProvider;
    private final ConnectorOrchestrator orchestrator;

    @Override
    @NullMarked
    public void run(String... args) {
        log.info("Starting CDC Engine...");
        
        List<CdcConnectorConfig> configs = configProvider.loadConfigs();
        
        if (configs.isEmpty()) {
            log.warn("No connector configurations found. Engine is idle.");
        } else {
            orchestrator.startConnectors(configs);
        }
    }
}
