package vn.truongngo.base.cdc.router.core.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import vn.truongngo.base.cdc.router.core.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object representing a CDC event.
 * <p>
 * Đối tượng ngữ cảnh đại diện cho một sự kiện CDC.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdcEventContext {
    /**
     * Unique identifier for the event.
     * <p>
     * Định danh duy nhất cho sự kiện.
     */
    private String eventId;

    /**
     * Name of the connector that produced the event.
     * <p>
     * Tên của connector đã tạo ra sự kiện.
     */
    private String connectorName;

    /**
     * Destination table name (e.g., database.table).
     * <p>
     * Tên bảng đích (ví dụ: database.table).
     */
    private String destTableName;

    /**
     * Timestamp when the event occurred at the source.
     * <p>
     * Thời gian sự kiện xảy ra tại nguồn.
     */
    private long sourceTimestamp;

    /**
     * The key of the changed record.
     * <p>
     * Khóa của bản ghi bị thay đổi.
     */
    private JsonNode key;

    /**
     * The payload of the event containing before/after data and metadata.
     * <p>
     * Dữ liệu của sự kiện chứa thông tin trước/sau thay đổi và metadata.
     */
    private JsonNode payload;

    /**
     * Additional metadata for the event.
     * <p>
     * Metadata bổ sung cho sự kiện.
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Gets the operation type (c: create, u: update, d: delete, r: read).
     * <p>
     * Lấy loại thao tác (c: tạo, u: cập nhật, d: xóa, r: đọc).
     *
     * @return The operation type string.
     */
    public String getOperation() {
        if (payload != null && payload.has("op")) {
            return payload.get("op").asString();
        }
        return "unknown";
    }

    /**
     * Gets the data after the change.
     * <p>
     * Lấy dữ liệu sau khi thay đổi.
     *
     * @return The 'after' JsonNode or null.
     */
    public JsonNode getAfterData() {
        if (payload != null && payload.has("after")) {
            return payload.get("after");
        }
        return null;
    }

    /**
     * Gets the data before the change.
     * <p>
     * Lấy dữ liệu trước khi thay đổi.
     *
     * @return The 'before' JsonNode or null.
     */
    public JsonNode getBeforeData() {
        if (payload != null && payload.has("before")) {
            return payload.get("before");
        }
        return null;
    }

    /**
     * Adds a metadata entry.
     * <p>
     * Thêm một mục metadata.
     *
     * @param key   The key.
     * @param value The value.
     * @return This context for chaining.
     */
    public CdcEventContext addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Retrieves a metadata value by key and type.
     * <p>
     * Lấy giá trị metadata theo khóa và kiểu dữ liệu.
     *
     * @param key  The key.
     * @param type The class type of the value.
     * @param <T>  The type parameter.
     * @return An Optional containing the value if found and convertible, otherwise empty.
     */
    public <T> Optional<T> getMetadata(String key, Class<T> type) {
        Object val = metadata.get(key);
        if (val == null) return Optional.empty();
        if (type.isInstance(val)) return Optional.of(type.cast(val));
        return Optional.ofNullable(JsonUtils.fromJson(JsonUtils.toJson(val), type));
    }
}
