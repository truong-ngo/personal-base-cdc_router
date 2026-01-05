package vn.truongngo.base.cdc.router.cdcrouterserversample.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.engine.loader.ConfigLoader;
import vn.truongngo.base.cdc.router.engine.registry.RoutingRegistry;
import vn.truongngo.base.cdc.router.engine.service.ConnectorOrchestrator;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorBootstrap implements CommandLineRunner {

    private final ConfigLoader configLoader;
    private final ConnectorOrchestrator orchestrator;
    private final RoutingRegistry routingRegistry;

    @Override
    public void run(String... args) {
        log.info("Starting CDC Router Server...");

        // 1. Load configs từ file JSON
        List<CdcConnectorConfig> configs = configLoader.loadConfigs();
        if (configs.isEmpty()) {
            log.warn("No CDC configurations found. Server is idle.");
            return;
        }

        // 2. Setup Routing Rules
        for (CdcConnectorConfig config : configs) {
            if (config.isEnabled() && config.getSinks() != null) {
                // Đăng ký map: TableName -> List<Sink>
                config.getSinks().forEach(routingRegistry::register);
            }
        }

        // 3. Start Engines
        // (Lưu ý: EngineAdapter bây giờ đã tự Inject Router qua Constructor,
        // nên ta chỉ cần pass list configs vào orchestrator)
        orchestrator.startConnectors(configs);
    }
}