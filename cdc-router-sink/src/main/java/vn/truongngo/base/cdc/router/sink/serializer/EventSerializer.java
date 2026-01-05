package vn.truongngo.base.cdc.router.sink.serializer;

import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;

/**
 * Interface for serializing CDC events before sending to sinks.
 * <p>
 * Giao diện để tuần tự hóa các sự kiện CDC trước khi gửi đến các sink.
 */
public interface EventSerializer {
    /**
     * Serializes the event key.
     * <p>
     * Tuần tự hóa khóa của sự kiện.
     *
     * @param event The CDC event.
     * @return The serialized key as a String.
     */
    String serializeKey(CdcEventContext event);

    /**
     * Serializes the event value (payload).
     * <p>
     * Tuần tự hóa giá trị (payload) của sự kiện.
     *
     * @param event The CDC event.
     * @return The serialized value as a String.
     */
    String serializeValue(CdcEventContext event);
}
