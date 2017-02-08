package de.isnow.sqlws.dao;

import java.util.Collection;

import de.isnow.sqlws.model.DBConnection;
import de.isnow.sqlws.model.DatabaseAnalyser;
import de.isnow.sqlws.model.DatabaseInfo;
import de.isnow.sqlws.model.TableInfo;

public class DatabaseInfoDAO {
	private DBConnection conn;
	private DatabaseAnalyser analyzer;
	
	public DatabaseInfoDAO(DBConnection connection) {
		conn = connection;
		analyzer = new DatabaseAnalyser(connection);
	}

	public Collection<String> getTableNames() {
		return conn.getTableNames();
	}
	
	public Collection<TableInfo> getTables() {
		return analyzer.getDatabaseInfo().getTableInfos();
	}
	
	public DatabaseInfo getDatabaseInfo() {
		return analyzer.getDatabaseInfo();
	}
}
