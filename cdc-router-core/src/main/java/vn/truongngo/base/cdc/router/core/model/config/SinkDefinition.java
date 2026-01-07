package vn.truongngo.base.cdc.router.core.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Definition of a data sink.
 * <p>
 * Định nghĩa của một đích dữ liệu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SinkDefinition {
    /**
     * The name of the sink.
     * <p>
     * Tên của đích dữ liệu.
     */
    private String name;

    /**
     * The type of the sink (e.g., kafka, jdbc, etc.).
     * <p>
     * Loại của đích dữ liệu (ví dụ: kafka, jdbc, v.v.).
     */
    private String type;

    /**
     * The condition to route data to this sink (SpEL expression).
     * <p>
     * Điều kiện để định tuyến dữ liệu đến đích này (biểu thức SpEL).
     */
    private String condition;

    /**
     * Maximum number of retries when execution fails. Default is 3.
     * <p>
     * Số lần thử lại tối đa khi thực thi thất bại. Mặc định là 3.
     */
    private Integer maxRetries = 3;

    /**
     * Time to wait between retries in milliseconds. Default is 1000ms.
     * <p>
     * Thời gian chờ giữa các lần thử lại tính bằng mili giây. Mặc định là 1000ms.
     */
    private Long retryWaitMillis = 1000L;

    /**
     * Additional properties for the sink configuration.
     * <p>
     * Các thuộc tính bổ sung cho cấu hình đích dữ liệu.
     */
    private Map<String, Object> properties;

    public Integer getMaxRetries() {
        if (maxRetries == null) return 3;
        return maxRetries;
    }

    public Long getRetryWaitMillis() {
        if (retryWaitMillis == null) return 1000L;
        return retryWaitMillis;
    }
}
