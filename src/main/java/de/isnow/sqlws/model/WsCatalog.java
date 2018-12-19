package de.isnow.sqlws.model;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.isnow.sqlws.resources.SchemaModelService;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.isnow.sqlws.resources.DatabaseModelService;
import lombok.Getter;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Schema;

/**
 * @author tbayer
 *
 */

@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY, 
		getterVisibility = JsonAutoDetect.Visibility.NONE, 
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties({ "params", "rels" })
public class WsCatalog  extends WsObject  {

	@JsonIgnore
	protected Catalog catalog;

	@Getter
	@JsonIgnore
	private WsConnection owningConnection;

	@Getter
	private Set<WsSchema> schemas;

	public WsCatalog(Catalog catalog, WsConnection connection) {
		super();
		Registry<WsObject> wsObjectRegistry = initRegistry(this.getClass());
		wsObjectRegistry.register(this);

		this.owningConnection = connection;
		this.catalog = catalog;
		super.name = catalog.getName();
		super.fullName = catalog.getFullName();
		schemas = getAllSchemas();
	}

	public static Collection<WsCatalog> getAll() {
		return registries
				.get(WsCatalog.class)
				.getAll()
				.stream()
				.map(o -> ((WsCatalog)o))
				.collect(Collectors.toSet());
	}

	public static WsCatalog get(@NotNull String id) {
		return (WsCatalog)registries
				.get(WsCatalog.class)
				.get(id);
	}

	private Set<WsSchema> getAllSchemas() {
		if (null != schemas)
			return schemas;
		schemas = new TreeSet<>();
		for (final Schema schema : catalog.getSchemas()) {
			WsSchema wschema = new WsSchema(schema, this);
			schemas.add(wschema);
		}
		return schemas;
	}


	@InjectLinks({
		@InjectLink(
    		resource= DatabaseModelService.class,
    		method="getDb",
			bindings ={
				@Binding(name = "catid",
					value = "${instance.id}")
			},
    		rel = "self",
    		title = "this Catalog",
			type = "GET",
    		style =  Style.RELATIVE_PATH
		),
    	@InjectLink(
    		resource= SchemaModelService.class,
    		method="getDbSchemas",
			bindings ={
				@Binding(name = "catid",
				value = "${instance.id}")
			},
    		rel = "schemas",
    		title = "get schemas for this catalog",
			type = "GET",
    		style =  Style.RELATIVE_PATH
    	)
    })
    List<Link> links;

}
