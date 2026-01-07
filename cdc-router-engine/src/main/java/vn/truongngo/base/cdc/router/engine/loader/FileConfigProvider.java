package vn.truongngo.base.cdc.router.engine.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of ConfigProvider that loads configurations from a local JSON file.
 * <p>
 * Triển khai ConfigProvider tải cấu hình từ file JSON cục bộ.
 */
@Slf4j
@Component
@Order(1) // Priority order if multiple providers are used
@RequiredArgsConstructor
public class FileConfigProvider implements ConfigProvider {

    @Value("${cdc.config.path:./config/connectors.json}")
    private String configPath;

    private final ObjectMapper objectMapper;

    @Override
    public List<CdcConnectorConfig> loadConfigs() {
        try {
            File file = new File(configPath);
            if (!file.exists()) {
                log.warn("Config file not found at: {}", file.getAbsolutePath());
                return Collections.emptyList();
            }
            log.info("Loading configs from: {}", file.getAbsolutePath());
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to load connector configs from: {}", configPath, e);
            throw new RuntimeException("Failed to load connector configs", e);
        }
    }
}
