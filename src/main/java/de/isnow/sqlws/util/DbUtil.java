package de.isnow.sqlws.util;

import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;
import lombok.SneakyThrows;

import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

public class DbUtil {


    @SneakyThrows
    public static PreparedStatement createSingleReadQuery(
            WsTable table,
            Map<WsColumn, String> primaryKeys,
            Collection<String> columnsToShow,
            Connection conn) {
        if (null == columnsToShow)
            columnsToShow = table.getColumnsByName().keySet();
        String tableHeadSelect= createTableHeadSelect(table, columnsToShow);
        StringBuilder sb = new StringBuilder();
        //List<WsColumn> pkCols = table.getPrimaryKeyColumns();
        List<String> pkClauses = new ArrayList<>();
        for (WsColumn col : primaryKeys.keySet()) {
            String val = primaryKeys.get(col);
            if (null != val) {
                pkClauses.add(col.getName()+" = "+ val);
            }
        }
        sb.append(" WHERE ");
        sb.append(String.join(" AND ", pkClauses));
        String q = tableHeadSelect+sb.toString();
        PreparedStatement p = conn.prepareStatement(q);
        return p;
    }

    @SneakyThrows
    public static PreparedStatement createChildTableReadQuery(
            WsTable childTable,
            Map<WsColumn, WsColumn> primarySecondaryKeyRelations,
            Map<String, String> pkVals,
            Collection<String> columnsToShow,
            Connection conn) {
        if (null == columnsToShow)
            columnsToShow = childTable.getColumnsByName().keySet();
        String tableHeadSelect= createTableHeadSelect(childTable, columnsToShow);
        StringBuilder sb = new StringBuilder();
        List<String> pkClauses = new ArrayList<>();
        for (WsColumn col : primarySecondaryKeyRelations.keySet()) {
            WsColumn fkCol = primarySecondaryKeyRelations.get(col);
            if (null != fkCol) {
                pkClauses.add(fkCol.getFullName()+" = "+ pkVals.get(col.getName()));
            }
        }
        sb.append(" WHERE ");
        sb.append(String.join(" AND ", pkClauses));
        String q = tableHeadSelect+sb.toString();
        PreparedStatement p = conn.prepareStatement(q);
        return p;
    }

    @SneakyThrows
    public static PreparedStatement createLimitedReadQuery(
            WsTable table,
            Long firstRow,
            Long numRows,
            Collection<String> columnsToShow,
            Connection conn) {
        return createLimitedReadQuery(
                table, firstRow, numRows,
                columnsToShow,null, conn);
    }

    @SneakyThrows
    public static PreparedStatement createLimitedReadQuery(
            WsTable table,
            Long firstRow,
            Long numRows,
            Collection<String> columnsToShow,
            Map<WsColumn, String> columnFilters,
            Connection conn) {
        if (null == columnsToShow)
            columnsToShow = table.getColumnsByName().keySet();

        String tableHeadSelect= createTableHeadSelect(table, columnsToShow);
        StringBuilder sb = new StringBuilder();
        if (null != columnFilters) {
            List<String> clauses = new ArrayList<>();
            for (WsColumn col : columnFilters.keySet()) {
                String clause = null;
                switch (col.getDataType()) {
                    case "character":
                        clause = " LOWER(" + col.getName() + ") like ? ESCAPE '!'";
                        break;
                    case "integer":
                        clause = col.getName() + " = ?";
                        break;
                    case "real":
                        clause = col.getName() + " = ?";
                        break;
                    case "bit":
                        clause = col.getName() + " = ?";
                        break;
                    default:
                        clause = col.getName() + " = ?";
                        break;
                }
                clauses.add(clause);
            }
            if (clauses.size() != 0) {
                sb.append(" WHERE " + clauses.stream().collect(Collectors.joining(" AND ")));
            }
        }
        if (null != numRows)
            //sb.append(" fetch first "+numRows + " rows only");
            sb.append(" limit "+numRows+ " offset "+firstRow);
        String q = tableHeadSelect+sb.toString();
        PreparedStatement p = conn.prepareStatement(q);
        int cnt = 1;

        if (null != columnFilters) {
            for (WsColumn col : columnFilters.keySet()) {
                String qStr = columnFilters.get(col);
                switch (col.getDataType()) {
                    case "character":
                        qStr = qStr.toLowerCase()
                                .replace("!", "!!")
                                .replace("%", "!%")
                                .replace("_", "!_")
                                .replace("[", "![");
                        p.setString(cnt++, "%" + qStr + "%");
                        break;
                    case "integer":
                        Long qNum = Long.parseLong(qStr);
                        p.setLong(cnt++, qNum);
                        break;
                    case "real":
                        Double qDouble = Double.parseDouble(qStr);
                        p.setDouble(cnt++, qDouble);
                        break;
                    case "bit":
                        Boolean qBoolean = ((null != qStr));
                        p.setBoolean(cnt++, qBoolean);
                        break;
                    default:
                        p.setString(cnt++, columnFilters.get(col));
                        break;
                }
            }
        }
        return p;
    }


    private static String createTableHeadSelect(
            WsTable table,
            Collection<String> columnsToShow) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        String cols = String.join(", ", columnsToShow);
        sb.append(cols);
        sb.append(" FROM ");
        sb.append(table.getFullName());
        return sb.toString();
    }
}
