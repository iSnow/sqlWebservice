package de.isnow.sqlws.resources;

import java.util.ArrayList;
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
import javax.ws.rs.core.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.model.WsConnection;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootService {
	@Context
	ServletContext ctx;

	@GET
	@Path("/")
    public Response getRoot() throws JsonProcessingException {
		Map<String, String> urls = new TreeMap<>();
		urls.put("model", "/model/");
		return Response.ok(urls).build();
    }
}
