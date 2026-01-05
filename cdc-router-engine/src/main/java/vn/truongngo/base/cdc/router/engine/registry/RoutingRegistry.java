package vn.truongngo.base.cdc.router.engine.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.config.SinkDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for storing routing rules (table -> sinks).
 * <p>
 * Registry để lưu trữ các quy tắc định tuyến (bảng -> các sink).
 */
@Slf4j
@Component
public class RoutingRegistry {

    private final Map<String, List<SinkDefinition>> routes = new ConcurrentHashMap<>();

    /**
     * Registers a list of sinks for a specific table.
     * <p>
     * Đăng ký một danh sách các sink cho một bảng cụ thể.
     *
     * @param tableName The name of the table (case-insensitive).
     *                  <p>Tên của bảng (không phân biệt hoa thường).</p>
     * @param sinks     The list of sink definitions.
     *                  <p>Danh sách các định nghĩa sink.</p>
     */
    public void register(String tableName, List<SinkDefinition> sinks) {
        if (tableName == null || sinks == null || sinks.isEmpty()) {
            return;
        }

        String normalizedKey = tableName.toLowerCase();
        routes.put(normalizedKey, sinks);
        log.info("Registry: Added route for table '{}' with {} sinks", normalizedKey, sinks.size());
    }

    /**
     * Retrieves the list of sinks configured for a table.
     * <p>
     * Lấy danh sách các sink được cấu hình cho một bảng.
     *
     * @param tableName The name of the table.
     *                  <p>Tên của bảng.</p>
     * @return A list of SinkDefinition, or an empty list if no route exists.
     *         <p>Danh sách SinkDefinition, hoặc danh sách rỗng nếu không có tuyến nào tồn tại.</p>
     */
    public List<SinkDefinition> getSinks(String tableName) {
        if (tableName == null) return Collections.emptyList();
        return routes.getOrDefault(tableName.toLowerCase(), Collections.emptyList());
    }
    
    /**
     * Clears all registered routes.
     * <p>
     * Xóa tất cả các tuyến đã đăng ký.
     */
    public void clear() {
        routes.clear();
    }
}
