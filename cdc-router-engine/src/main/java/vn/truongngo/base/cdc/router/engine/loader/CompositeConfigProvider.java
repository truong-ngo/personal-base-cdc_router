package vn.truongngo.base.cdc.router.engine.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * A ConfigProvider that aggregates configurations from multiple sources.
 * <p>
 * Một ConfigProvider tổng hợp cấu hình từ nhiều nguồn khác nhau.
 */
@Slf4j
@Primary // This will be the main bean injected into Orchestrator
@Component
@RequiredArgsConstructor
public class CompositeConfigProvider implements ConfigProvider {

    private final List<ConfigProvider> providers;

    @Override
    public List<CdcConnectorConfig> loadConfigs() {
        List<CdcConnectorConfig> allConfigs = new ArrayList<>();
        
        for (ConfigProvider provider : providers) {
            // Skip self to avoid infinite recursion
            if (provider == this) continue;
            
            try {
                List<CdcConnectorConfig> configs = provider.loadConfigs();
                if (configs != null) {
                    allConfigs.addAll(configs);
                }
            } catch (Exception e) {
                log.error("Failed to load configs from provider: {}", provider.getClass().getSimpleName(), e);
                // Continue loading from other providers
            }
        }
        
        log.info("Loaded total {} connector configurations from {} providers.", allConfigs.size(), providers.size() - 1);
        return allConfigs;
    }
}
