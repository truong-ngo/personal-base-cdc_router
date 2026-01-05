package vn.truongngo.base.cdc.router.sink.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.exception.ConfigurationException;
import vn.truongngo.base.cdc.router.core.spi.CdcSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Factory class for managing and retrieving CDC Sink implementations.
 * <p>
 * Lớp Factory để quản lý và lấy các triển khai CDC Sink.
 */
@Slf4j
@Component
public class SinkFactory {

    private final Map<String, CdcSink> sinkMap = new HashMap<>();

    /**
     * Constructor that auto-discovers all beans implementing CdcSink.
     * <p>
     * Constructor tự động phát hiện tất cả các bean triển khai CdcSink.
     *
     * @param sinks List of CdcSink beans injected by Spring.
     *              <p>Danh sách các bean CdcSink được Spring inject.</p>
     */
    public SinkFactory(List<CdcSink> sinks) {
        if (sinks == null || sinks.isEmpty()) {
            log.warn("No CdcSink implementations found! Please check your @Component annotations.");
        } else {
            for (CdcSink sink : sinks) {
                String type = sink.getType();

                if (sinkMap.containsKey(type)) {
                    throw new ConfigurationException(String.format(
                            "Duplicate Sink Type detected: '%s'. Found in: %s and %s",
                            type,
                            sinkMap.get(type).getClass().getSimpleName(),
                            sink.getClass().getSimpleName()
                    ));
                }

                sinkMap.put(type, sink);
                log.info("Registered CDC Sink: [{}] -> {}", type, sink.getClass().getSimpleName());
            }
        }
    }

    /**
     * Retrieves a sink implementation by its type.
     * <p>
     * Lấy triển khai sink theo loại của nó.
     *
     * @param type The sink type (e.g., "kafka", "jdbc").
     *             <p>Loại sink (ví dụ: "kafka", "jdbc").</p>
     * @return The corresponding CdcSink instance.
     *         <p>Instance CdcSink tương ứng.</p>
     * @throws ConfigurationException if no sink is found for the given type.
     *                                <p>nếu không tìm thấy sink cho loại đã cho.</p>
     */
    public CdcSink getSink(String type) {
        return Optional.ofNullable(sinkMap.get(type))
                .orElseThrow(() -> new ConfigurationException(
                        "Sink implementation not found for type: '" + type + "'. " + "Available types: " + sinkMap.keySet()
                ));
    }

    /**
     * Returns a map of registered sink types and their class names.
     * <p>
     * Trả về map chứa các loại sink đã đăng ký và tên class của chúng.
     *
     * @return Map of sink type to class name.
     *         <p>Map từ loại sink đến tên class.</p>
     */
    public Map<String, String> getRegisteredSinks() {
        return sinkMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getClass().getSimpleName()));
    }
}
