package vn.truongngo.base.cdc.router.sink.serializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.core.util.JsonUtils;

/**
 * Default serializer that converts events to JSON strings.
 * <p>
 * Serializer mặc định chuyển đổi các sự kiện thành chuỗi JSON.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonEventSerializer implements EventSerializer {

    @Override
    public String serializeKey(CdcEventContext event) {
        if (event.getKey() != null) {
            return event.getKey().toString();
        }
        return event.getEventId();
    }

    @Override
    public String serializeValue(CdcEventContext event) {
        // Assuming payload is a JsonNode, toString() returns its JSON representation.
        // If payload structure changes, use JsonUtils.toJson(event.getPayload())
        if (event.getPayload() != null) {
            return event.getPayload().toString();
        }
        // Fallback to full event serialization if payload is missing
        return JsonUtils.toJson(event);
    }
}
