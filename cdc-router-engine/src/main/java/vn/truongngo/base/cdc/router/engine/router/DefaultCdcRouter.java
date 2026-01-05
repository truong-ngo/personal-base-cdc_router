package vn.truongngo.base.cdc.router.engine.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import vn.truongngo.base.cdc.router.core.exception.SinkExecutionException;
import vn.truongngo.base.cdc.router.core.model.config.SinkDefinition;
import vn.truongngo.base.cdc.router.core.model.event.CdcEventContext;
import vn.truongngo.base.cdc.router.core.spi.CdcRouter;
import vn.truongngo.base.cdc.router.core.spi.CdcSink;
import vn.truongngo.base.cdc.router.engine.registry.RoutingRegistry;
import vn.truongngo.base.cdc.router.sink.factory.SinkFactory;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCdcRouter implements CdcRouter {

    private final RoutingRegistry registry;
    private final SinkFactory sinkFactory;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public void route(CdcEventContext event) {
        String tableName = event.getDestTableName();
        List<SinkDefinition> sinks = registry.getSinks(tableName);

        if (sinks.isEmpty()) {
            log.trace("No routes configured for table: {}", tableName);
            return;
        }

        for (SinkDefinition def : sinks) {
            processSinkWithRetry(def, event);
        }
    }

    private void processSinkWithRetry(SinkDefinition def, CdcEventContext event) {
        if (!isConditionMet(def, event)) {
            return;
        }

        int attempt = 0;
        int maxRetries = def.getMaxRetries();
        long waitMillis = def.getRetryWaitMillis();

        while (true) {
            try {
                attempt++;
                CdcSink sinkImpl = sinkFactory.getSink(def.getType());
                sinkImpl.execute(def.getProperties(), event);
                return;
            } catch (SinkExecutionException e) {
                handleException(def, e, attempt, maxRetries, waitMillis);
            } catch (Exception e) {
                handleException(def, new SinkExecutionException("Unexpected error", e, true), attempt, maxRetries, waitMillis);
            }
        }
    }

    private void handleException(SinkDefinition def, SinkExecutionException e, int attempt, int maxRetries, long waitMillis) {
        if (!e.isRetryable()) {
            log.error("Non-retryable error in sink [{}]. Stopping engine. Error: {}", def.getName(), e.getMessage());
            throw e;
        }

        if (attempt > maxRetries) {
            log.error("Sink [{}] failed after {} attempts. Stopping engine. Last error: {}", def.getName(), attempt, e.getMessage());
            throw e;
        }

        log.warn("Sink [{}] failed (attempt {}/{}). Retrying in {}ms. Error: {}", def.getName(), attempt, maxRetries, waitMillis, e.getMessage());
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry wait", ie);
        }
    }

    private boolean isConditionMet(SinkDefinition def, CdcEventContext event) {
        String condition = def.getCondition();
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(event);
            return Boolean.TRUE.equals(parser.parseExpression(condition).getValue(context, Boolean.class));
        } catch (Exception e) {
            log.warn("Invalid condition '{}' for sink '{}'. Error: {}", condition, def.getName(), e.getMessage());
            return false;
        }
    }
}
