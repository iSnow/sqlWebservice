package de.isnow.sqlws.resources;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.model.WsCatalog;
import de.isnow.sqlws.model.WsConnection;
import de.isnow.sqlws.model.WsSchema;

@Path("/model/catalog")
public class DatabaseModelService {
	

	@GET
	@Path("/{catid}")
    @Produces(MediaType.APPLICATION_JSON)
    public WsCatalog getDb(
    		@PathParam("catid") String catId
    		) throws JsonProcessingException {
		WsCatalog wsCatalog = WsCatalog.get(catId);
		
		return wsCatalog;
    }

	@GET
	@Path("/connection/{cid}")
    @Produces(MediaType.APPLICATION_JSON)
    public WsCatalog getDbs(
    		@PathParam("cid") String connectionId
    		) throws JsonProcessingException {
		WsConnection conn = WsConnection.get(connectionId);
		if (null == conn) {
			return null;
		}
		WsCatalog db = conn.getCatalog();
		return db;
    }

	@GET
	@Path("/{catid}/schemas")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<WsSchema> getDbSchemas(
			@PathParam("catid") String catId
	) throws JsonProcessingException {

		WsCatalog wsCatalog = WsCatalog.get(catId);

		return wsCatalog.getSchemas();
	}
}
