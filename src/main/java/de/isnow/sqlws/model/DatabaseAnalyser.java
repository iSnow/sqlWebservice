package de.isnow.sqlws.model;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jjander
 *
 */
@Slf4j
public class DatabaseAnalyser {
	private DBConnection connection;
	
	public DatabaseAnalyser (DBConnection conn) {
		connection = conn;
	}

	
	/**
	 * Method getDatabaseInfo.
	 */
	public DatabaseInfo getDatabaseInfo() {
		DatabaseInfo databaseInfo = new DatabaseInfo();
		for (String tableName : connection.getTableNames()) {
			
			log.debug("Analyser TableName: " + tableName);
			TableInfo tableInfo = new TableInfo(tableName);

			retrievePrimaryKey(tableName, tableInfo);
			retrieveRelations(tableName, tableInfo);
			
			retrieveColumns(tableName, tableInfo);
			
			Column column = tableInfo.getColumn( tableInfo.getPkColumnName());
			column.setPrimaryKey( true);
			
			databaseInfo.add(tableInfo);
		}

		return databaseInfo;
	}

	public void retrieveColumns(String tableName, TableInfo tableInfo) {
		Collection<Column> columns = DatabaseUtil.getColumnsMetaData(tableName, connection);
		tableInfo.add(columns);
	}

	public void retrievePrimaryKey(String tableName, TableInfo tableInfo) {
		tableInfo.setPkColumnName(DatabaseUtil.getPrimaryKeyColumnName(tableName, connection));

		tableInfo.setPkColumnType(
			DatabaseUtil.getColumnType(tableName, tableInfo.getPkColumnName(), connection));
	}

	private void retrieveRelations(String tableName, TableInfo tableInfo) {
		try {

			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getImportedKeys(null, null, tableName);

			while (resultSet.next()) {
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					tableInfo.add(
						new Relation(
							resultSet.getString("PKTABLE_NAME"),
							resultSet.getString("PKCOLUMN_NAME"),							
							resultSet.getString("FKTABLE_NAME"),
							resultSet.getString("FKCOLUMN_NAME"),
							resultSet.getString("FK_NAME")));

				}
			}

		} catch (SQLException e) {
			System.err.println(e);
			throw new IllegalArgumentException("Wrong tableName : " + tableName);
		}
	}

}
