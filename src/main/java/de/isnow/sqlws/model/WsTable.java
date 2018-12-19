package de.isnow.sqlws.model;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.isnow.sqlws.resources.ColumnModelService;
import de.isnow.sqlws.resources.TableContentService;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.isnow.sqlws.resources.TableModelService;
import lombok.Getter;
import schemacrawler.schema.*;


@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY, 
		getterVisibility = JsonAutoDetect.Visibility.NONE, 
		setterVisibility = JsonAutoDetect.Visibility.NONE)

@JsonIgnoreProperties({
		"filters"
})
public class WsTable extends WsObject{

	@JsonIgnore
	Table table;

	@JsonIgnore
	@Getter
	private WsSchema owningSchema;

	@Getter
	private String tableType;
	/*
        @Getter
        private String pkColumnName;
        @Getter
        private int pkColumnType;

        */
	@Getter
	private Set<WsColumn> columns;
	
	@JsonIgnore
	@Getter
	private Map<String, WsColumn> columnsByName = new LinkedHashMap<>();

	@Getter
	@JsonIgnore
	private Map<String, Relation> relations = new HashMap<>();

	@Getter
	@JsonIgnore
	private Collection<ForeignKey> fks;

	@Getter
	@JsonIgnore
	private Collection<Index> indexes;

	@Getter
	@JsonIgnore
	private Collection<Table> childTables;

	@Getter
	@JsonIgnore
	Collection<TableConstraint> tableConstraints;

	/**
	 * Ignore this property, it exists only to satisfy
	 * Jersey
	 */
	@JsonIgnore
	@Getter
	Long maxrecords;

	/**
	 * Ignore this property, it exists only to satisfy
	 * Jersey
	 */
	@JsonIgnore
	@Getter
	Long startrecord;

	/**
	 * Ignore this property, it exists only to satisfy
	 * Jersey
	 */
	@JsonIgnore
	@Getter
	Collection<String> columnsToShow;
	/**
	 * Ignore this property, it exists only to satisfy
	 * Jersey
	 */
	@JsonIgnore
	@Getter
	Collection<String> filters;

	public WsTable() {
		super();
		Registry<WsObject> wsObjectRegistry = initRegistry(this.getClass());
		wsObjectRegistry.register(this);
	}

	public WsTable(Table t, WsSchema schema) {
		this();
		this.table = t;
		fks = t.getForeignKeys();
		indexes = t.getIndexes();
		childTables = t.getRelatedTables(TableRelationshipType.child);
		tableConstraints = t.getTableConstraints();
		this.owningSchema = schema;
		name = t.getName();
		fullName = t.getFullName();
		tableType = t.getTableType().getTableType();
		columns = new LinkedHashSet<>(t.getColumns()
				.stream()
				.map(c -> new WsColumn(c))
				.collect(Collectors.toList()));
		for (WsColumn column : columns) {
			columnsByName.put(column.getName(), column);
		}
	}

	public static WsTable get(@NotNull String id) {
		Registry<WsObject> reg = registries.get(WsTable.class);
		if (null == reg)
			return null;
		return (WsTable)registries
				.get(WsTable.class)
				.get(id);
	}
	/*
        binary
            bit
            character
            id
            integer
            large_object
            object
            real
            reference
            temporal
            unknown
            url
            xml
         */
	public Map<String, Map<String,String>> getColumnFilters() {
		Map<String, Map<String,String>> filters = new HashMap<>();
		columns.forEach((c) -> {
			Map<String,String> filter = new HashMap<>();
			filter.put("name", c.getName());
			String type = c.getDataType();
			switch (type) {
				case "bit":
					filter.put("type", "checkbox");
					break;
				case "character":
					filter.put("type", "textfield");
					break;
				case "large_object":
					filter.put("type", "textfield");
					break;
				case "integer":
					filter.put("type", "range:integer");
					break;
				case "real":
					filter.put("type", "range:float");
					break;
				case "temporal":
					filter.put("type", "datetimepicker");
					break;
			}
			filters.put(c.getId(), filter);
		});
		return filters;
	}

	@InjectLinks({
			@InjectLink(
					resource= TableModelService.class,
					method="getTable",
					bindings ={
							@Binding(name = "id",
									value = "${instance.id}")
					},
					rel = "self",
					title = "this Table",
					type = "GET",
					style =  Style.RELATIVE_PATH
			),
			@InjectLink(
					resource= ColumnModelService.class,
					method="getColumns",
					bindings ={
							@Binding(name = "tableid",
									value = "${instance.id}")
					},
					rel = "columns",
					title = "get columns for this table",
					type = "GET",
					style =  Style.RELATIVE_PATH
			),
			@InjectLink(
					resource=TableContentService.class,
					method="getContents",
					bindings ={
							@Binding(name = "tableid",
									value = "${instance.id}")
					},
					rel = "contents",
					title = "get contents for this table",
					type = "GET",
					style =  Style.RELATIVE_PATH
			)
	})
    List<Link> links;
}
