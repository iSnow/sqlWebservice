package de.isnow.sqlws.rest;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.DbConnectionStore;
import de.isnow.sqlws.model.DBConnection;
import de.isnow.sqlws.model.Database;
import de.isnow.sqlws.model.Table;

@Path("/table")
public class TableService {
	private ObjectMapper mapper;

	@GET
	@Path("/connection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTables(
    		@QueryParam("cid") Long connectionId,
    		@QueryParam("dbid") Long dbId
    		) throws JsonProcessingException {
		init();
		DBConnection conn = DbConnectionStore.getConnection(connectionId);
		if (null == conn) {
			return Response.ok().build();
		}
		Database database = conn.getDatabase(dbId);
		Collection<Table> tables = database.getTables();
		return Response.ok(mapper.writer().writeValueAsString(tables)).build();
    }
	
	private void init() {
		mapper = new ObjectMapper();
	}
}
