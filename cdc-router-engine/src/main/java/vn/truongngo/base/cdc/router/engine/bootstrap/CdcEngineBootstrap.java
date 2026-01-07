package vn.truongngo.base.cdc.router.engine.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.engine.loader.ConfigProvider;
import vn.truongngo.base.cdc.router.engine.service.ConnectorOrchestrator;
import vn.truongngo.base.cdc.router.engine.validator.ConfigValidator;

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
    private final ConfigValidator configValidator;
    private final ConnectorOrchestrator orchestrator;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting CDC Engine...");
        
        // 1. Load Configs
        List<CdcConnectorConfig> configs = configProvider.loadConfigs();
        
        if (configs.isEmpty()) {
            log.warn("No connector configurations found. Engine is idle.");
            return;
        }

        // 2. Validate Configs
        try {
            configValidator.validate(configs);
        } catch (Exception e) {
            log.error("Configuration validation failed. Engine will NOT start.", e);
            // In production, you might want to System.exit(1) here
            throw e; 
        }

        // 3. Start Engine
        orchestrator.startConnectors(configs);
    }
}
