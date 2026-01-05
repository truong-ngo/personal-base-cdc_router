package vn.truongngo.base.cdc.router.engine.registry;

import org.springframework.stereotype.Component;
import vn.truongngo.base.cdc.router.engine.spi.CdcSourceEngine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectorRegistry {

    private final Map<String, CdcSourceEngine> activeEngines = new ConcurrentHashMap<>();

    public void register(CdcSourceEngine engine) {
        activeEngines.put(engine.getName(), engine);
    }

    public void unregister(String name) {
        activeEngines.remove(name);
    }
    
    public void remove(String name) {
        activeEngines.remove(name);
    }

    public boolean contains(String name) {
        return activeEngines.containsKey(name);
    }

    public CdcSourceEngine get(String name) {
        return activeEngines.get(name);
    }

    public Map<String, CdcSourceEngine> getAll() {
        return activeEngines;
    }
}
