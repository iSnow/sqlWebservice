package de.isnow.sqlws.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.isnow.sqlws.rest.TableService;
import lombok.Getter;
import lombok.Setter;


@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY, 
		getterVisibility = JsonAutoDetect.Visibility.NONE, 
		setterVisibility = JsonAutoDetect.Visibility.NONE)

@Getter
@Setter
public class Table {
	@Getter
	@JsonIgnore
	private DBConnection owningConnection;
	
	@Getter
	private Long id;

	@Getter
	private String tableName;

	@Getter
	private String pkColumnName;
	
	@Getter
	private int pkColumnType;
	
	@Getter
	private Set<Column> columns = new LinkedHashSet<>();
	
	@JsonIgnore
	private Map<String, Column> columnsByName = new HashMap<>();
	
	private Map<String, Relation> relations = new HashMap<>();

	public void add(Relation relation) {
		relations.put(relation.getFkColumnName(), relation);
	}
	
	public Collection<Relation> getRelations() {
		return new ArrayList<Relation>(relations.values());
	}

	public Relation getRelation(String fkColumnName) {
		return relations.get(fkColumnName);
	}

	public boolean isFkColumn(String columnName) {
		return null != relations.get(columnName);
	}

	public void add(Column column) {
		columns.add(column);
		columnsByName.put(column.getName(), column);
	}

	public void add(Collection<Column> cols) {
		for (Column col : cols) {
			add(col);
		}
	}

	public Set<String> getColumnNames() {
		return columnsByName.keySet();
	}

	public boolean hasColumn(String name) {
		return columnsByName.containsKey(name);
	}

	public Column getColumn(String columnName) {
		return columnsByName.get(columnName);
	}
	
	public void setRelations(Collection<Relation> rels) {
		for (Relation r : rels) {
			add(r);
		}
	}
	

	@InjectLinks({
    	@InjectLink(
    		resource=TableService.class, 
    		method="getTables",
			bindings ={
				@Binding(name = "cid", 
				value = "${instance.owningConnection.id}"),
				@Binding(name = "dbid", 
				value = "${instance.id}")
			},
    		rel = "self",
    		title = "tables",
    		style =  Style.RELATIVE_PATH
    	)
    })
    List<Link> links;
}
