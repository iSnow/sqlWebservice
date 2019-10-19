package de.isnow.sqlws.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import schemacrawler.schema.NamedObject;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class WsObject implements Comparable<WsObject>{
    public final static Map<Class, Registry<WsObject>> registries;
    static {
        registries = new HashMap<>();
    }

    @Getter
    private String id = Long.toString(new Double(Math.random()*1000000).longValue() + System.currentTimeMillis()*1000000);

    protected List<String> schemaCrawlerLookupKey;

    @Getter
    protected String name;

    @Getter
    protected String fullName;

    WsObject() {}

    WsObject(NamedObject schemaCrawlerObj) {
        setId(schemaCrawlerObj);
    }

    public void setId(NamedObject schemaCrawlerObj) {
        schemaCrawlerLookupKey = schemaCrawlerObj.toUniqueLookupKey();
        this.id = idString(schemaCrawlerObj);
    }

    public void setId(String id) {
        this.id = id;
        schemaCrawlerLookupKey = Arrays.asList(id);
    }

    public static String idString(NamedObject schemaCrawlerObj) {
        List<String> schemaCrawlerKey = schemaCrawlerObj.toUniqueLookupKey();
        if ((null == schemaCrawlerKey) || (schemaCrawlerKey.isEmpty()))
            throw new IllegalArgumentException("SchemaCrawler object with empty key");
        return schemaCrawlerKey.stream().filter(Objects::nonNull).collect(Collectors.joining("."));
    }

    void register (Object o) {
        Registry<WsObject> wsObjectRegistry = initRegistry(o.getClass());
        wsObjectRegistry.register(this);
    }

    Registry<WsObject> initRegistry(Class clazz) {
        Registry<WsObject> wsObjectRegistry = registries.get(clazz);
        if (null == wsObjectRegistry) {
            wsObjectRegistry = new Registry<>();
            registries.put(clazz, wsObjectRegistry);
        }
        return wsObjectRegistry;
    }

    public static Registry getRegistry (Class clazz) {
       return registries.get(clazz);
    }

    @Override
    public int compareTo(@NotNull WsObject o) {
        return id.compareTo(o.id);
    }
}
