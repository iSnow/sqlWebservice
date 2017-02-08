/*
 * Copyright (c) 2003, Orientation in Objects GmbH, www.oio.de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of Orienation in Objects GmbH nor the names of its 
 *   contributors may be used to endorse or promote products derived from this 
 *   software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ${file_name} Created on ${date} by ${user}
 *  
 * ${todo}
 * 
 */

package de.isnow.sqlws.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * @author tbayer
 *
 */
@Slf4j
public class DatabaseUtil {

	

	/**
	 * Method getTable.
	 * @param tableName
	 */
	public static Collection<String> getPrimaryKeyValuesFromTable(String tableName, DBConnection connection) {
		ArrayList<String> ids = new ArrayList<>();

		try {
			Connection conn = connection.getConnection();
			ResultSet resultSet = conn.prepareStatement(getPrimaryKeyValuesSelectStatement(tableName)).executeQuery();
			String pkColumnName = getPrimaryKeyColumnName(tableName, connection);
			while (resultSet.next()) {
				ids.add(resultSet.getString(pkColumnName));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ids;

	}

	private static String getPrimaryKeyValuesSelectStatement(String tableName) {
		return "select * from " + tableName;
	}


	public static String getSQLParameterSubstring(String value, int type) {

		if (isNumericType(type)) {
			return value;
		} else {
			return "'" + value + "'";
		}
	}

	public static boolean isNumericType(int type) {
		return Types.BIGINT == type
			|| Types.DECIMAL == type
			|| Types.INTEGER == type
			|| Types.NUMERIC == type
			|| Types.SMALLINT == type
			|| Types.TINYINT == type;
	}

	public static String getPrimaryKeyColumnName(String tableName, DBConnection connection) {

		try {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getPrimaryKeys(null, null, tableName);
			resultSet.next();
			return resultSet.getString("COLUMN_NAME");
		} catch (SQLException e) {
			throw new IllegalArgumentException("Wrong tablename : " + tableName);
		}
	}

	/**
	 * Method getColumnType.
	 * @param tableName
	 * @param string
	 * @return String
	 */
	public static int getColumnType(String tableName, String columnName, DBConnection connection) {
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName);

			resultSet.next();

			return resultSet.getInt("DATA_TYPE");
		} catch (SQLException e) {
			throw new IllegalArgumentException(
				"Wrong tablename or columnName: " + tableName + ", " + columnName);
		}

	}

	/**
	 * Method deleteRow.
	 * @param string
	 * @param string1
	 */
	public static void deleteRow(String tableName, String primaryKey, DBConnection connection) throws SQLException, NoRowsAffectedException {
		Connection conn = connection.getConnection();
		PreparedStatement statement =
			conn.prepareStatement(getRowDeleteStatement(tableName, primaryKey, connection));

		if(!statement.execute()) {
			if(statement.getUpdateCount()==0) {
				throw new NoRowsAffectedException();
			}
		}
	}

	

	/**
	 * Method getRowDeleteStatement.
	 * @param tableName
	 * @param primaryKey
	 * @return String
	 */
	private static String getRowDeleteStatement(String tableName, String primaryKey, DBConnection connection) {
		return "delete from "
			+ tableName
			+ " where "
			+ getPrimaryKeyColumnName(tableName, connection)
			+ " = "
			+ getSQLParameterSubstring(
				primaryKey,
				DatabaseUtil.getColumnType(tableName, getPrimaryKeyColumnName(tableName, connection), connection));
	}
	/**
	 * Method getColumnsMetaData.
	 * @param tableName
	 * @return Collection
	 */
	public static Collection<Column> getColumnsMetaData(String tableName, DBConnection connection) {
		ArrayList<Column> columns = new ArrayList<>();

		try {
			DatabaseMetaData dbMetaData = connection.getMetaData();
			ResultSet rs = dbMetaData.getColumns( null, null, tableName, null);

			while( rs.next()) {				
				columns.add( new Column( rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE")));				
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return columns;		
		
	}

}
