package de.isnow.sqlws.resources;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.core.JsonProcessingException;

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
