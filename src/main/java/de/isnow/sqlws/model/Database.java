package de.isnow.sqlws.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.isnow.sqlws.rest.DatabaseService;
import de.isnow.sqlws.rest.TableService;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tbayer
 *
 */

@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY, 
		getterVisibility = JsonAutoDetect.Visibility.NONE, 
		setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Database implements Comparable<Database>{
	@Getter
	private Long id;
	
	
	@Getter
	@Setter
	@JsonIgnore
	private DBConnection owningConnection;

	@JsonIgnore
	private Map<String, Table> tables = new HashMap<>();


	public Database() {
		id = new Double(Math.random() * 1000000).longValue() + System.currentTimeMillis() * 1000000;
	}
	
	public void add(Table tableInfo) {
		tables.put(tableInfo.getTableName(), tableInfo);
	}

	public Collection<Table> getTables() {
		return new ArrayList<Table>(tables.values());
	}

	public Table getTable(String tableName) {
		return tables.get(tableName);
	}

	@Override
	public int compareTo(Database o) {
		if (null == o)
			return -1;
		return id.compareTo(o.id);
	}
	

	@InjectLinks({
		@InjectLink(
    		resource=DatabaseService.class, 
    		method="getDb",
			bindings ={
				@Binding(name = "dbid", 
				value = "{id}"),
			},
    		rel = "self",
    		title = "this Databases",
    		style =  Style.RELATIVE_PATH
		    ),
		@InjectLink(
    		resource=DatabaseService.class, 
    		method="getDbs",
			bindings ={
				@Binding(name = "cid", 
				value = "${instance.owningConnection.id}"),
			},
    		rel = "collection",
    		title = "all Databases",
    		style =  Style.RELATIVE_PATH
	    ),
    	@InjectLink(
    		resource=TableService.class, 
    		method="getTables",
			bindings ={
				@Binding(name = "cid", 
				value = "${instance.owningConnection.id}"),
				@Binding(name = "dbid", 
				value = "${instance.id}")
			},
    		rel = "tables",
    		title = "tables",
    		style =  Style.RELATIVE_PATH
    	)
    })
    List<Link> links;

}
