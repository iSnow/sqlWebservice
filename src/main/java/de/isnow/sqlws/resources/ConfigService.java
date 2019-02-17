package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.isnow.sqlws.model.WsConnection;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigService {


    @Context
    ServletContext ctx;

    @GET
    @Path("/routes")
    public Response getRoot() throws JsonProcessingException {
        List<WsConnection> cons = new ArrayList<>(WsConnection.getAll());
        return Response.ok(cons).build();
    }
}
