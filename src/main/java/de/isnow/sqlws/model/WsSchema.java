package de.isnow.sqlws.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.isnow.sqlws.resources.SchemaModelService;
import de.isnow.sqlws.resources.TableModelService;
import lombok.Getter;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)

public class WsSchema extends WsObject{

    @JsonIgnore
    @Getter
    private Schema schema;

    @JsonIgnore
    @Getter
    private WsCatalog owningCatalog;


    private WsSchema() {
        super();
        Registry<WsObject> wsObjectRegistry = initRegistry(this.getClass());
        wsObjectRegistry.register(this);
    }

    public WsSchema(Schema schema, WsCatalog catalog) {
        this();
        this.schema = schema;
        this.owningCatalog = catalog;
        name = schema.getName();
        fullName = schema.getFullName();
    }

    @JsonIgnore
    public static Collection<WsSchema> getAll() {
        return registries
                .get(WsSchema.class)
                .getAll()
                .stream()
                .map(o -> ((WsSchema)o))
                .collect(Collectors.toSet());
    }

    public static WsSchema get(@NotNull String id) {
        return (WsSchema)registries
                .get(WsSchema.class)
                .get(id);
    }

    @JsonInclude
    public Collection<WsTable> getTables() {
        Set<WsTable> tables = new TreeSet<>();
        for (Table t : owningCatalog.catalog.getTables(schema)) {
            tables.add(new WsTable(t, this));
        }
        return tables;
    }


    @InjectLinks({
            @InjectLink(
                    resource= SchemaModelService.class,
                    method="getOwningSchema",
                    bindings ={
                            @Binding(name = "id",
                                    value = "${instance.id}")
                    },
                    rel = "self",
                    title = "this Schema",
                    type = "GET",
                    style =  InjectLink.Style.RELATIVE_PATH
            ),
            @InjectLink(
                    resource= TableModelService.class,
                    method="getTables",
                    bindings ={
                            @Binding(name = "schemaid",
                                    value = "${instance.id}")
                    },
                    rel = "tables",
                    title = "tables",
                    type = "GET",
                    style =  InjectLink.Style.RELATIVE_PATH
            )
    })
    List<Link> links;
}
