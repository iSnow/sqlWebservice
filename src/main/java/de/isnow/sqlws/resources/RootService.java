package de.isnow.sqlws.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.DbConnectionStore;
import de.isnow.sqlws.model.DBConnection;

@Path("/")
public class RootService {
	@Context
	ServletContext ctx;
	
	private ObjectMapper mapper;
	
	@GET
	@Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections2() throws JsonProcessingException {
		init();
		List<DBConnection> cons = new ArrayList<DBConnection>(DbConnectionStore.getConnections());
		return Response.ok(cons).build();
    }
	
	@GET
	@Path("/2")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() throws JsonProcessingException {
		init();
		List<DBConnection> cons = new ArrayList<DBConnection>(DbConnectionStore.getConnections());
		return Response.ok(mapper.writer().writeValueAsString(cons)).build();
    }
	
	
	@GET
	@Path("/3/")
	@Produces("application/xml")
    public Collection getConnections3() throws JsonProcessingException {
		init();
		List<DBConnection> cons = new ArrayList<DBConnection>(DbConnectionStore.getConnections());
		return cons;
    }
	
	@GET
	@Path("/connection/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectionDetails(
    		@PathParam("id") Long id) throws JsonProcessingException {
		init();
		DBConnection connection = DbConnectionStore.getConnection(id);
		Map<String, Object>retMap = new TreeMap<>();
		retMap.put("dbproduct", connection.getDatabaseProductName());
		retMap.put("schemas", connection.getSchemaNames());
		retMap.put("tables", connection.getTableNames());
		return Response.ok(mapper.writer().writeValueAsString(retMap)).build();
    }
	

	@POST
	@Path("/connection")
    @Produces(MediaType.APPLICATION_JSON)
    public Response openConnection(
    		@QueryParam("url") String url,
    		@QueryParam("user") String username,
    		@QueryParam("password") String password,
    		@Context UriInfo uriInfo) throws JsonProcessingException {
		init();
		DBConnection connection = DbConnectionStore.newConnection(url, username, password);
		long connectionId = connection.getId();
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(connectionId));
        return Response.created(builder.build()).build();
    }
	
	
	private void init() {
		mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
	}
}
