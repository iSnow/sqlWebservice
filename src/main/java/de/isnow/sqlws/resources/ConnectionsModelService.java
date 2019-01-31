package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.isnow.sqlws.model.WsConnection;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;

@Path("/model/connection")
@Produces(MediaType.APPLICATION_JSON)
public class ConnectionsModelService {
	@Context
	ServletContext ctx;

	@GET
	@Path("/")
    public Response getRoot() throws JsonProcessingException {
		List<WsConnection> cons = new ArrayList<>(WsConnection.getAll());
		return Response.ok(cons).build();
    }
	
	@GET
	@Path("connection/{id}")
    public WsConnection getConnectionDetails(
    		@PathParam("id") String id) throws JsonProcessingException {
		WsConnection connection = WsConnection.get(id);
		return connection;
    }
	

	@POST
	@Path("/connection")
    public Response openConnection(
    		@QueryParam("url") String url,
    		@QueryParam("user") String username,
    		@QueryParam("password") String password,
			@QueryParam("name") String name,
    		@Context UriInfo uriInfo) throws JsonProcessingException {
		WsConnection connection = new WsConnection(url, username, password, name,null);
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(connection.getId());
        return Response.created(builder.build()).build();
    }
}
