package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.isnow.sqlws.model.WsConnection;
import de.isnow.sqlws.model.WsCatalog;
import de.isnow.sqlws.model.WsSchema;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/model/schema")
public class SchemaModelService {
	private ObjectMapper mapper;
	

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
    public WsSchema getSchema(
    		@PathParam("id") String schemaid
    		) throws JsonProcessingException {
		init();
		WsSchema wss = WsSchema.get(schemaid);

		return wss;
    }

	@GET
	@Path("/connection/{cid}")
	@Produces(MediaType.APPLICATION_JSON)
    public Set<WsSchema> getSchemasForConnection(
    		@PathParam("cid") String connectionId
    		) throws JsonProcessingException {
		init();
		WsConnection conn = WsConnection.get(connectionId);
		if (null == conn) {
			return null;
		}
		WsCatalog db = conn.getCatalog();
		return db.getSchemas();
    }

	@GET
	@Path("/catalog/{catid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<WsSchema> getDbSchemas(
			@PathParam("catid") String catId
	) throws JsonProcessingException {
		init();
		WsCatalog wsCatalog = WsCatalog.get(catId);
		if (wsCatalog == null)
			return null;
		return wsCatalog.getSchemas();
	}
	
	private void init() {
		mapper = new ObjectMapper();
	}
}
