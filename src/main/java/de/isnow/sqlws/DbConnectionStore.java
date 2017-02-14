package de.isnow.sqlws;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import de.isnow.sqlws.model.DBConnection;

public class DbConnectionStore {
	private static Map<String, DBConnection> connectionsByUrl = new HashMap<>();
	private static Map<Long, DBConnection> connectionsById = new HashMap<>();
	
	public static DBConnection newConnection(
			@NotNull String dbUrl, 
			@NotNull String user, 
			@NotNull String password) {
		DBConnection conn = new DBConnection(dbUrl, user, password);
		return registerConnection(conn);
	}

	public static DBConnection registerConnection(@NotNull DBConnection conn) {
		connectionsByUrl.put(conn.getDatabaseUrl(), conn);
		connectionsById.put(conn.getId(), conn);
		return conn;
	}
	
	public static DBConnection getConnection(@NotNull Long id) {
		return connectionsById.get(id);
	}

	public static DBConnection getConnection(@NotNull String url) {
		return connectionsByUrl.get(url);
	}
	
	public static Collection<DBConnection> getConnections() {
		return connectionsById.values();
	}

}
