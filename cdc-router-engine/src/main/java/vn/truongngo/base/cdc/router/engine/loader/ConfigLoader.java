package vn.truongngo.base.cdc.router.engine.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Component responsible for loading connector configurations.
 * <p>
 * Component chịu trách nhiệm tải cấu hình connector.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigLoader {

    @Value("${cdc.config.path:./config/connectors.json}")
    private String configPath;

    private final ObjectMapper objectMapper;

    /**
     * Loads connector configurations from the file specified in 'cdc.config.path'.
     * <p>
     * Tải cấu hình connector từ file được chỉ định trong 'cdc.config.path'.
     *
     * @return A list of CdcConnectorConfig.
     *         <p>Danh sách CdcConnectorConfig.</p>
     */
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
