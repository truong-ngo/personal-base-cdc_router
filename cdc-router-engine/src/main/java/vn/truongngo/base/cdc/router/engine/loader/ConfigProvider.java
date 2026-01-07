package vn.truongngo.base.cdc.router.engine.loader;

import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

import java.util.List;

/**
 * Interface for providing CDC connector configurations.
 * <p>
 * Giao diện cung cấp cấu hình cho CDC connector.
 */
public interface ConfigProvider {
    /**
     * Loads connector configurations.
     * <p>
     * Tải các cấu hình connector.
     *
     * @return A list of CdcConnectorConfig.
     *         <p>Danh sách CdcConnectorConfig.</p>
     */
    List<CdcConnectorConfig> loadConfigs();
}
