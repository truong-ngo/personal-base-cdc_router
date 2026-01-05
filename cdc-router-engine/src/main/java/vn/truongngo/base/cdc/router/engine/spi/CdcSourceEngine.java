package vn.truongngo.base.cdc.router.engine.spi;

import vn.truongngo.base.cdc.router.core.model.config.CdcConnectorConfig;

public interface CdcSourceEngine {

    String getEngineType();

    void configure(CdcConnectorConfig config);

    void start() throws Exception;

    void stop();

    boolean isRunning();
    
    /**
     * Checks if the engine has failed due to an error.
     * @return true if the engine is in a failed state.
     */
    boolean isFailed();
    
    /**
     * Gets the last error that caused the failure.
     * @return The throwable error or null.
     */
    Throwable getLastError();

    String getName();
}
