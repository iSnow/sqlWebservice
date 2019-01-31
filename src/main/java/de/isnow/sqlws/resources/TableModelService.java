package de.isnow.sqlws.resources;

import java.util.Collection;
import java.util.HashSet;
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
import de.isnow.sqlws.model.viewModel.VmColumn;
import de.isnow.sqlws.model.viewModel.VmTable;
import de.isnow.sqlws.util.RestUtils;

@Produces(MediaType.APPLICATION_JSON)
@Path("/model/table")
public class TableModelService {

	@GET
	@Path("/{id}")
	public Map getTable(
			@PathParam("id") String tableId) {

		WsTable wst = WsTable.get(tableId);

		VmTable vmt = new VmTable();
		Set<VmColumn> cols = new HashSet<>();
		int cnt = 0;
		if (null != wst.getColumns()) {
			for (WsColumn c : wst.getColumns()) {
				VmColumn col = new VmColumn();
				col.setName(c.getName());
				col.setFullName(c.getFullName());
				//if ((c.isPrimaryKey()) || (c.isForeignKey())) {
				if (c.isForeignKey()) {
					col.setVisible(false);
				} else {
					col.setPosition(cnt++);
				}
				cols.add(col);
			}
		};
		vmt.setColumns(cols);

		Map<String, Object> response = RestUtils.createJsonWrapper(wst);
		response.put("id", tableId);
		response.put("model", vmt);
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
