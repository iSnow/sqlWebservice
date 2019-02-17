package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Data

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@EqualsAndHashCode(of={"routes"})
@ToString(of={"routes"})
public class RouterConfig {

    List<RouterConfigRecord> routes;

    @EqualsAndHashCode(of={"name", "path"})
    @ToString(of={"name", "path"})
    public class RouterConfigRecord {

        public RouterConfigRecord(){}

        String path;

        String name;

        Map<String, String> components = new TreeMap<>();

        Set<RouterConfig> children;
    }
}
