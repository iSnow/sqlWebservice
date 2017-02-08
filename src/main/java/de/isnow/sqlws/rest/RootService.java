package de.isnow.sqlws.rest;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.SqlWebserviceApplication;
import de.isnow.sqlws.model.DBConnection;

@Path("/dbinfo")
public class RootService {
	@Context
	ServletContext ctx;
	
	private ObjectMapper mapper;
	
	@GET
	@Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnections() throws JsonProcessingException {
		init();
		Collection<DBConnection> connections = SqlWebserviceApplication.activeConnections.values();
		Set<Long> conns = connections.stream().map(c -> c.getId()).collect(Collectors.toSet());
		return Response.ok(mapper.writer().writeValueAsString(conns)).build();
    }
	
	@GET
	@Path("/connection/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectionDetails(
    		@PathParam("id") Long id) throws JsonProcessingException {
		init();
		Collection<DBConnection> connections = SqlWebserviceApplication.activeConnections.values();
		DBConnection connection = connections
				.stream()
				.filter(c -> c.getId().equals(id))
				.collect(Collectors.toList())
				.get(0);
		Map<String, Object>retMap = new TreeMap<>();
		retMap.put("dbproduct", connection.getDatabaseProductName());
		retMap.put("schemas", connection.getSchemaNames());
		retMap.put("tables", connection.getTableNames());
		return Response.ok(mapper.writer().writeValueAsString(retMap)).build();
    }
	
	
	private void init() {
		mapper = new ObjectMapper();
	}
}
