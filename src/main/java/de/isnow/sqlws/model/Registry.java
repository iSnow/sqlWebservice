package de.isnow.sqlws.model;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Registry<T> {

    private Map<String, T> byId = new HashMap<>();


    T register(@NotNull T obj) {
        if (obj instanceof WsObject)
            byId.put(((WsObject)obj).getId(), obj);
        return obj;
    }

    public T get(@NotNull String id) {
        return byId.get(id);
    }

    public Collection<T> getAll() {
        return byId.values();
    }
}
