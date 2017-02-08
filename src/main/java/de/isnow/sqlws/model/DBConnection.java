package de.isnow.sqlws.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * @author jjander
 *
 */
@Slf4j
public class DBConnection {

	@Getter
	private Long id;

	private String databaseUrl, user, password;
	
	@Getter
	private Connection connection;

	public DBConnection (
			@NotNull String databaseUrlIn, 
			@NotNull String userIn, 
			@NotNull String passwordIn) {
		 this.databaseUrl = databaseUrlIn;
		 this.user = userIn;
		 this.password = passwordIn;
		 connection = getDbConnection();
		 id = new Double(Math.random()*1000000).longValue() + System.currentTimeMillis()*1000000;
	}
	
	private Connection getDbConnection() {
		try {
			return DriverManager.getConnection( databaseUrl, user, password);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DatabaseMetaData getMetaData() {
		try {
			return connection.getMetaData();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getDatabaseProductName() {
		try {
			DatabaseMetaData meta = connection.getMetaData();
			return meta.getDatabaseProductName()+" "+meta.getDatabaseProductVersion();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public ArrayList<String> getSchemaNames() {
		ArrayList<String> schemas = new ArrayList<>();
		try {
			ResultSet resultSet = getMetaData().getTables(null, null, null, new String[] { "TABLE" });
			log.debug("GetSchemaNames !");

			while (resultSet.next()) {
				log.debug("GetSchemaNames ! inside");
				schemas.add(resultSet.getString("TABLE_NAME"));
			}
			return schemas;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the names of all tables in the DB schema
	 * by querying the DB metadata
	 * @return Collection of strings
	 */
	public Collection<String> getTableNames() {
		ArrayList<String> tables = new ArrayList<>();
		try {
			log.debug("GetTableNames ! Start");
			ResultSet resultSet = getMetaData().getTables(null, null, null, new String[] { "TABLE" });
			log.debug("GetTableNames !");

			while (resultSet.next()) {
				log.debug("GetTableNames ! inside");
				tables.add(resultSet.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return tables;
	}
	
	/**
	 * A Catalog is a logical database in a dbms. The method returns a list of catalognames.
	 * 
	 * 
	 * @return Collection containing Strings with the names of the database Catalogs.
	 */
	public Collection<String> getCatalogs() {
		ArrayList<String> catalogs = new ArrayList<>();

		try {
			ResultSet resultSet = getMetaData().getCatalogs();
			while (resultSet.next()) {
				catalogs.add(resultSet.getString("TABLE_CAT"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return catalogs;
	}
	
	/**
	 * Method getTableDetails.
	 * @param string
	 * @param i
	 */
	public ArrayList<Row> getTableDetails(String tableName, String id) {
		ArrayList<Row> ids = new ArrayList<>();

		try {
			PreparedStatement statement =
				connection.prepareStatement(getTableDetailsStatement(tableName, id));

			ResultSet resultSet = statement.executeQuery();
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			int columnCount = resultSet.getMetaData().getColumnCount();

			while (resultSet.next()) {
				Row row = new Row();
				for (int i = 1; i <= columnCount; i++) {
					row.add(new Column(rsMetaData.getColumnName(i), resultSet.getString(i)));
				}
				ids.add(row);
			}
		} catch (SQLException e) {
			throw new RuntimeException (e);
		}

		return ids;
	}
	

	public String getTableDetailsStatement(String tableName, String id) {
		StringBuffer queryString = new StringBuffer();
		queryString.append("select * from ");
		queryString.append(tableName);
		queryString.append(" where ");
		queryString.append(DatabaseUtil.getPrimaryKeyColumnName(tableName, this));
		queryString.append(" = ");

		int type = DatabaseUtil.getColumnType(tableName, DatabaseUtil.getPrimaryKeyColumnName(tableName, this), this);
		queryString.append(DatabaseUtil.getSQLParameterSubstring(id, type));

		return queryString.toString();
	}
}
