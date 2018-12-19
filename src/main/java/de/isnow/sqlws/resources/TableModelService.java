package de.isnow.sqlws.resources;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;
import de.isnow.sqlws.model.WsSchema;

@Produces(MediaType.APPLICATION_JSON)
@Path("/model/table")
	public class TableModelService {

	@GET
	@Path("/{id}")
	public WsTable getTable(
			@PathParam("id") String tableId
	) throws JsonProcessingException {
		WsTable wst = WsTable.get(tableId);

		return wst;
	}

	@GET
	@Path("/schema/{schemaid}")
    public Collection<WsTable> getTables(
    		@PathParam("schemaid") String tableId
    		) throws JsonProcessingException {
		WsSchema schema = WsSchema.get(tableId);
		if (null == schema) {
			return null;
		}
		Collection<WsTable> wsTables = schema.getTables();
		return wsTables;
    }



	@GET
	@Path("/filters/{tableid}")
	public Map<String, Map<String,String>> getTableFilters(
			@PathParam("tableid") String tableId
	) throws JsonProcessingException {
		WsTable wst = WsTable.get(tableId);
		if ((null == wst) || (null == wst.getColumns()))
			return null;
		return wst.getColumnFilters();
	}



/*	@GET
	@Path("/{tableid}/columns")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WsColumn> getColumns(
			@PathParam("tableid") Long tableId
	) throws JsonProcessingException {
		init();
		WsTable table = WsTable.get(tableId);
		if (null == table) {
			return null;
		}
		Collection<WsColumn> cols = table.getColumns();
		return cols;
	}*/
}
