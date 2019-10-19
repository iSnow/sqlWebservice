package de.isnow.sqlws.resources;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import de.isnow.sqlws.model.WsTable;
import de.isnow.sqlws.model.WsSchema;
import de.isnow.sqlws.model.viewModel.VmRecord;
import de.isnow.sqlws.util.RestUtils;

@Produces(MediaType.APPLICATION_JSON)
@Path("/model/table")
public class TableModelService {

	@GET
	@Path("/{id}")
	public Map getTable(
			@PathParam("id") String tableId) {

		WsTable wst = WsTable.get(tableId);
		VmRecord vmt = VmRecord.fromWsTable(wst, null, 1,true);

		Map<String, Object> response = RestUtils.createJsonWrapper(vmt);
		response.put("id", tableId);
		return response;
	}

	@GET
	@Path("/schema/{schemaid}")
	public Collection<WsTable> getTables(
			@PathParam("schemaid") String tableId) {

		WsSchema schema = WsSchema.get(tableId);
		if (null == schema) {
			return null;
		}
		return schema.getTables();
	}

	@GET
	@Path("/filters/{tableid}")
	public Map<String, Map<String,String>> getTableFilters(
			@PathParam("tableid") String tableId) {

		WsTable wst = WsTable.get(tableId);
		if ((null == wst) || (null == wst.getColumns()))
			return null;
		return wst.getColumnFilters();
	}
}
