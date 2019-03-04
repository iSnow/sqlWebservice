package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Data

@EqualsAndHashCode(of={"routes"})
@ToString(of={"routes"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RouterConfig {

    List<RouterConfigRecord> routes;

    @EqualsAndHashCode(of={"name", "path"})
    @ToString(of={"name", "path"})
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class RouterConfigRecord {

        String path;

        String name;

        Map<String, String> components = new TreeMap<>();

        Set<RouterConfigRecord> children;
    }
}
