package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsSchema;
import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/model/column")
public class ColumnModelService {

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public WsColumn getColumn(
			@PathParam("id") String columnid
	) throws JsonProcessingException {
		WsColumn wsc = WsColumn.get(columnid);

		return wsc;
	}

	@GET
	@Path("/table/{tableid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<WsColumn> getColumns(
    		@PathParam("tableid") String schemaId
    		) throws JsonProcessingException {
		WsTable table = WsTable.get(schemaId);
		if (null == table) {
			return null;
		}
		Collection<WsColumn> columns = table.getColumns();
		return columns;
    }

	
}
