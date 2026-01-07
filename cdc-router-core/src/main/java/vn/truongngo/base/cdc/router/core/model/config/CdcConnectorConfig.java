package vn.truongngo.base.cdc.router.core.model.config;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Configuration for a CDC Connector.
 * <p>
 * Cấu hình cho một CDC Connector.
 */
@Data
public class CdcConnectorConfig {
    /**
     * The unique name of the connector.
     * <p>
     * Tên duy nhất của connector.
     */
    private String connectorName;

    /**
     * Whether the connector is enabled.
     * <p>
     * Trạng thái kích hoạt của connector.
     */
    private boolean enabled = true;

    /**
     * Properties for the source connector (e.g., database connection details).
     * <p>
     * Các thuộc tính cho connector nguồn (ví dụ: thông tin kết nối cơ sở dữ liệu).
     */
    private Map<String, String> sourceProperties;

    /**
     * Mapping of table names to their sink definitions.
     * <p>
     * Ánh xạ tên bảng tới các định nghĩa đích dữ liệu của chúng.
     */
    private Map<String, List<SinkDefinition>> sinks;
}
