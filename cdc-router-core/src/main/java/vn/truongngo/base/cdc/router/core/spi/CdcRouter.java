package vn.truongngo.base.cdc.router.core.spi;

import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;

/**
 * Interface for routing CDC events to appropriate sinks.
 * <p>
 * Giao diện để định tuyến các sự kiện CDC đến các sink phù hợp.
 */
public interface CdcRouter {

    /**
     * Routes the given event to configured sinks.
     * <p>
     * Định tuyến sự kiện được cung cấp đến các sink đã được cấu hình.
     *
     * @param event The CDC event context.
     *              <p>Ngữ cảnh sự kiện CDC.</p>
     */
    void route(CdcEventContext event);
}
