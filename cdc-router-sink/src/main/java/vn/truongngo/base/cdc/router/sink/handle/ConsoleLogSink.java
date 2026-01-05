package vn.truongngo.base.cdc.router.sink.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.core.spi.CdcSink;

import java.util.Map;

/**
 * A simple sink that logs events to the console. Useful for debugging.
 * <p>
 * Một sink đơn giản ghi log sự kiện ra console. Hữu ích cho việc gỡ lỗi.
 */
@Slf4j
@Component
public class ConsoleLogSink implements CdcSink {

    @Override
    public String getType() {
        return "console";
    }

    @Override
    public void execute(Map<String, Object> props, CdcEventContext event) {
        String prefix = (String) props.getOrDefault("prefix", "CDC");
        String level = (String) props.getOrDefault("level", "INFO");
        String logMessage = String.format("[%s] Table: %s | Op: %s | ID: %s",
                prefix,
                event.getDestTableName(),
                event.getOperation(),
                event.getEventId());

        if ("DEBUG".equalsIgnoreCase(level)) {
            log.info("{} | Payload: {}", logMessage, event.getPayload());
        } else {
            log.info(logMessage);
        }
    }
}
