package vn.truongngo.base.cdc.router.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;
import vn.truongngo.base.cdc.router.core.spi.DistributedLock;
import vn.truongngo.base.cdc.router.engine.registry.ConnectorRegistry;
import vn.truongngo.base.cdc.router.engine.spi.CdcSourceEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectorOrchestrator {

    private final ConnectorRegistry registry;
    private final DistributedLock distributedLock;
    private final ApplicationContext applicationContext; // Dùng để tạo Prototype Bean

    // Cache config để dùng cho việc restart
    private final Map<String, CdcConnectorConfig> configCache = new ConcurrentHashMap<>();

    // Giả sử config đã được load sẵn từ file/db
    public void startConnectors(List<CdcConnectorConfig> configs) {
        for (CdcConnectorConfig config : configs) {
            configCache.put(config.getConnectorName(), config);
            if (!config.isEnabled()) continue;

            String connectorName = config.getConnectorName();
            tryStartConnector(connectorName);
        }
    }

    private void tryStartConnector(String connectorName) {
        // 1. Kiểm tra Lock (HA)
        boolean isLeader = distributedLock.tryLock("lock:cdc:" + connectorName, 30000);

        if (isLeader) {
            log.info("Acquired lock for connector: {}. Starting engine...", connectorName);
            CdcConnectorConfig config = configCache.get(connectorName);
            if (config != null) {
                startEngine(config);
            }
        } else {
            log.debug("Connector {} is running on another node or locked. Standing by.", connectorName);
        }
    }

    private void startEngine(CdcConnectorConfig config) {
        try {
            // 2. Tạo Instance Engine mới (Lookup từ Spring Context)
            // Bean "debeziumEngineAdapter" sẽ được define ở module cdc-source-debezium
            CdcSourceEngine engine = applicationContext.getBean(CdcSourceEngine.class);

            // 3. Cấu hình và Start
            engine.configure(config);
            engine.start();

            // 4. Đưa vào sổ quản lý
            registry.register(engine);

        } catch (Exception e) {
            log.error("Failed to start connector: {}", config.getConnectorName(), e);
            // Cần logic release lock nếu start thất bại
            distributedLock.unlock("lock:cdc:" + config.getConnectorName());
        }
    }

    public void shutdownAll() {
        registry.getAll().values().forEach(engine -> {
            engine.stop();
            distributedLock.unlock("lock:cdc:" + engine.getName());
        });
    }

    /**
     * Watchdog task: Runs every 30 seconds to check health of connectors.
     * - Restarts failed connectors.
     * - Refreshes distributed locks.
     */
    @Scheduled(fixedDelay = 30000)
    public void monitorConnectors() {
        // 1. Check existing running connectors
        for (CdcSourceEngine engine : registry.getAll().values()) {
            String name = engine.getName();
            
            // Refresh lock to keep leadership
            boolean lockRefreshed = distributedLock.refreshLock("lock:cdc:" + name, 30000);
            if (!lockRefreshed) {
                log.warn("Lost lock for connector [{}]. Stopping...", name);
                engine.stop();
                registry.remove(name); // Remove from registry
                continue;
            }

            // Check if engine failed
            if (engine.isFailed()) {
                log.error("Connector [{}] detected as FAILED. Last error: {}. Attempting restart...", 
                        name, engine.getLastError());
                
                // Stop cleanly just in case
                engine.stop();
                registry.remove(name);

                // Restart
                tryStartConnector(name);
            } else if (!engine.isRunning()) {
                // Engine stopped but not marked as failed (maybe manual stop?)
                // For now, we assume if it's in registry, it should be running.
                log.warn("Connector [{}] is not running but in registry. Restarting...", name);
                tryStartConnector(name);
            }
        }

        // 2. Check for orphaned configs (enabled but not running anywhere)
        // This part is tricky in distributed mode without a shared state store.
        // Assuming 'configCache' contains all desired connectors.
        for (String name : configCache.keySet()) {
            CdcConnectorConfig config = configCache.get(name);
            if (config.isEnabled() && !registry.contains(name)) {
                // Try to acquire lock and start (maybe previous leader died)
                tryStartConnector(name);
            }
        }
    }
}
