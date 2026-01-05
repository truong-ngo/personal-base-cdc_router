package vn.truongngo.base.cdc.router.core.spi;

import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;

import java.util.Map;

/**
 * Interface for a CDC Sink destination.
 * <p>
 * Giao diện cho một đích đến CDC (Sink).
 */
public interface CdcSink {
    /**
     * Gets the type of the sink (e.g., "kafka", "jdbc").
     * <p>
     * Lấy loại của sink (ví dụ: "kafka", "jdbc").
     *
     * @return The sink type.
     */
    String getType();

    /**
     * Executes the sink logic to send data.
     * <p>
     * Thực thi logic của sink để gửi dữ liệu.
     *
     * @param props Configuration properties for this sink execution.
     *              <p>Các thuộc tính cấu hình cho lần thực thi sink này.</p>
     * @param event The CDC event context.
     *              <p>Ngữ cảnh sự kiện CDC.</p>
     * @throws Exception If an error occurs during execution.
     */
    void execute(Map<String, Object> props, CdcEventContext event) throws Exception;
}
