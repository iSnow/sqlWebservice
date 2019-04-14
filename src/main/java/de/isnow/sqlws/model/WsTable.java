package de.isnow.sqlws.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.isnow.sqlws.resources.ColumnModelService;
import de.isnow.sqlws.resources.TableContentService;
import de.isnow.sqlws.util.DbUtil;
import lombok.Data;
import lombok.ToString;
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
@ToString(of={"table", "owningSchema"})
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

	@JsonIgnore
	@Getter
	private Map<String, WsColumn> columnsByFullName = new LinkedHashMap<>();

	/*@Getter
	@JsonIgnore
	private Map<String, WsRelation> relations = new HashMap<>();
*/
	@Getter
	@JsonIgnore
	private Collection<Index> indexes;
/*
	@Getter
	@JsonIgnore
	private Collection<Table> childTables;


	@Getter
	Set<WsForeignKey> foreignKeys;
*/

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
		register(this);
	}

	public WsTable(Table t, WsSchema schema) {
		super(t);
		register(this);
		this.table = t;
		indexes = t.getIndexes();
		//childTables = t.getRelatedTables(TableRelationshipType.child);
		//tableConstraints = t.getTableConstraints();
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
			columnsByFullName.put(column.getFullName(), column);
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

	public Map<String,List<WsColumn>> getColumnsByType() {
		Map<String,List<WsColumn>> colMap = new HashMap<>();
		columns.forEach((c) -> {
			if (null == colMap.get(c.getDataType())){
				colMap.put(c.getDataType(), new ArrayList<>());
			}
			colMap.get(c.getDataType()).add(c);
		});
		return colMap;
	}

	public List<WsColumn> getPrimaryKeyColumns() {
		List<WsColumn> pkCols = columns
				.stream()
				.filter(WsColumn::isPrimaryKey)
				.collect(Collectors.toList());
		return pkCols;
	}

	public List<WsColumn> getForeignKeyColumns() {
		List<WsColumn> pkCols = columns
				.stream()
				.filter(WsColumn::isForeignKey)
				.collect(Collectors.toList());
		return pkCols;
	}

	public WsColumn getColumnByFullName(String fullName) {
		List<WsColumn> pkCols = columns
				.stream()
				.filter((c) -> c.getFullName().equals(fullName))
				.collect(Collectors.toList());
		if (pkCols.isEmpty())
			return null;
		return pkCols.get(0);
	}


	public WsColumn getColumnByName(String name) {
		List<WsColumn> pkCols = columns
				.stream()
				.filter((c) -> c.getName().equals(name))
				.collect(Collectors.toList());
		if (pkCols.isEmpty())
			return null;
		return pkCols.get(0);
	}

	public Set<WsColumn> getColumnsToShow(Collection<String> columnsToShow) {
		Set<String> lColumnsToShow =
				((null == columnsToShow) || (columnsToShow.isEmpty())) ?
						getColumnsByName().keySet()
						: new LinkedHashSet<>(columnsToShow);
		return lColumnsToShow.stream().map((name) ->
				getColumnByName(name))
				.collect(Collectors.toSet());
	}


	public Map<WsColumn, WsColumn> getMatchingFKs(WsTable childTable) {
		final Map<WsColumn, WsColumn> childPks = new HashMap<>();
		Set<WsTable.WsForeignKey> fkCols = childTable.getForeignKeys();
		fkCols.forEach((c) -> {
			if (c.getParentTableKey().equals(getFullName())) {
				c.getPrimaryForeignKeyRelationships().forEach((f) -> {
					WsColumn pfkCol = getColumnByFullName(f.getPkColumnName());
					WsColumn ffkCol = childTable.getColumnByFullName(f.getFkColumnName());
					childPks.put(pfkCol, ffkCol);
				});
			}
		});
		return childPks;
	}

	public List<WsTable> getChildTables() {
		Registry<WsObject> tablesReg = registries.get(WsTable.class);
		Registry<WsObject> columnReg = registries.get(WsColumn.class);
		if (null == tablesReg)
			return null;
		List<WsTable> children = table.getRelatedTables(TableRelationshipType.child)
			.stream()
			.map((t) -> {
				String idString = WsObject.idString(t);
				return (WsTable)tablesReg.get(idString);})
			.collect(Collectors.toList());
		Set<WsForeignKey> fks = parseForeignKeys ();
		children.stream().forEach((c) -> {
			String childId = c.getId();
			List<WsForeignKey> matchingFks = fks.stream().filter((k) -> {
				if (null == k.getChildTableKey())
					return false;
				return k.getChildTableKey().equalsIgnoreCase(childId);
			}).collect(Collectors.toList());
			if (matchingFks.size() > 0) {
				matchingFks.get(0).primaryForeignKeyRelationships.forEach((k) -> {
					WsColumn col = (WsColumn)columnReg.get(k.getFkColumnName());
					col.setReferences(k.getPkColumnName());
				});
			}
		});
		return children;
	}

	public List<TableConstraint> getConstraints() {
		Collection<TableConstraint> tableConstraints = table.getTableConstraints();
		List<TableConstraint> constraints = new ArrayList<>(tableConstraints);
		return constraints;
	}

	public Set<WsForeignKey> getForeignKeys() {
		return parseForeignKeys ();
	}

	public Set<WsForeignKey> parseForeignKeys () {
		Collection<ForeignKey> fks = table.getForeignKeys();
		if ((null == fks) || (fks.isEmpty()))
			return new HashSet<>();
		Registry<WsColumn> colsRegistry = WsObject.getRegistry(WsColumn.class);
		Set<WsForeignKey> fkSet = fks.stream()
			.map((fk) -> {
				WsForeignKey k = new WsForeignKey();
				List<WsRelation> keyRelationsships = new ArrayList<>();
				List<ForeignKeyColumnReference> refs = fk.getColumnReferences();
				refs.forEach((r) -> {
					String fkc = r.getForeignKeyColumn().getFullName();
					k.setChildTableKey(r.getForeignKeyColumn().getParent().getFullName());
					String pkc = r.getPrimaryKeyColumn().getFullName();
					k.setParentTableKey(r.getPrimaryKeyColumn().getParent().getFullName());
					List <WsColumn> pKCols = this.columns
							.stream()
							.filter((c) -> {return c.fullName.equals(pkc);})
							.collect(Collectors.toList());
					WsColumn fKCol =  colsRegistry.get(fkc);
					pKCols.forEach((c)-> c.addReferencedBy(fKCol));
					//System.out.println(pKCols);
					WsRelation kv = new WsRelation();
					kv.setPkColumnName(pkc);
					kv.setFkColumnName(fkc);
					keyRelationsships.add(kv);
				});
				k.setPrimaryForeignKeyRelationships(keyRelationsships);
				return k;
			})
			.collect(Collectors.toSet());
		return fkSet;
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

	@Data
	public static class WsForeignKey {

		String childTableKey;

		String parentTableKey;

		List<WsRelation> primaryForeignKeyRelationships;
	}
}
