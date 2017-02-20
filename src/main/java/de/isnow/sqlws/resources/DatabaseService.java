package de.isnow.sqlws.resources;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.isnow.sqlws.DbConnectionStore;
import de.isnow.sqlws.model.DBConnection;
import de.isnow.sqlws.model.Database;

@Path("/db")
public class DatabaseService {
	private ObjectMapper mapper;
	

	@GET
	@Path("/{dbid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Database getDb(
    		@PathParam("dbid") Long dbId
    		) throws JsonProcessingException {
		init();
		Database database = null;
		for (DBConnection conn : DbConnectionStore.getConnections()) {
			database = conn.getDatabase(dbId);
			if (null != database)
				break;
		}
		
		return database;
    }

	@GET
	@Path("/connection")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Database> getDbs(
    		@QueryParam("cid") Long connectionId
    		) throws JsonProcessingException {
		init();
		DBConnection conn = DbConnectionStore.getConnection(connectionId);
		if (null == conn) {
			return new ArrayList<Database>();
		}
		Collection<Database> dbs = conn.getDatabases();
		return dbs;
    }
	
	private void init() {
		mapper = new ObjectMapper();
	}
}
