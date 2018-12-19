package de.isnow.sqlws.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class WsObject implements Comparable<WsObject>{
    public final static Map<Class, Registry<WsObject>> registries;
    static {
        registries = new HashMap<>();
    }

    @Getter
    private final String id = Long.toString(new Double(Math.random()*1000000).longValue() + System.currentTimeMillis()*1000000);

    @Getter
    protected String name;

    @Getter
    protected String fullName;

    protected Registry<WsObject> initRegistry(Class clazz) {
        Registry<WsObject> wsObjectRegistry = registries.get(clazz);
        if (null == wsObjectRegistry) {
            wsObjectRegistry = new Registry<>();
            registries.put(clazz, wsObjectRegistry);
        }
        return wsObjectRegistry;
    }

    @Override
    public int compareTo(WsObject o) {
        if ((null == id) || (null == o.id))
            return 0;
        return id.compareTo(o.id);
    }
}
