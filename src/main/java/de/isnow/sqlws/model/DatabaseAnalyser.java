package de.isnow.sqlws.model;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jjander
 *
 */
@Slf4j
public class DatabaseAnalyser {
	private DBConnection connection;

	public DatabaseAnalyser(DBConnection conn) {
		connection = conn;
	}

	/**
	 * Method getDatabaseInfo.
	 */
	public Database createDatabaseInfo() {
		Database database = new Database();
		connection.addDatabase(database);
		for (String tableName : connection.getTableNames()) {
			log.debug("Analyser TableName: " + tableName);
			String pkColumnName = DatabaseUtil.getPrimaryKeyColumnName(tableName, connection);
			Set<Column> cols = new TreeSet<>(DatabaseUtil.getColumnsMetaData(tableName, connection));

			Table table = new Table();
			table.setOwningConnection(connection);
			table.setPkColumnName(pkColumnName);
			table.setPkColumnType(DatabaseUtil
					.getColumnType(tableName, pkColumnName, connection));
			table.setTableName(tableName);
			table.setColumns(cols);
			table.setRelations(retrieveRelations(tableName));

			database.add(table);
		}

		return database;
	}


	private Set<Relation> retrieveRelations(String tableName) {
		try {
			Set<Relation> relations = new LinkedHashSet<>();
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getImportedKeys(null, null, tableName);

			while (resultSet.next()) {
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					relations.add(new Relation(resultSet.getString("PKTABLE_NAME"),
							resultSet.getString("PKCOLUMN_NAME"), resultSet.getString("FKTABLE_NAME"),
							resultSet.getString("FKCOLUMN_NAME"), resultSet.getString("FK_NAME")));

				}
			}
			return relations;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
