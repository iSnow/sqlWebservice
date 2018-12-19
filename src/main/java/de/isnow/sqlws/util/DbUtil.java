package de.isnow.sqlws.util;

import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;
import lombok.SneakyThrows;

import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DbUtil {

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
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        String cols = String.join(", ", columnsToShow);
        sb.append(cols);
        sb.append(" FROM ");
        sb.append(table.getFullName());
        if (null != columnFilters) {
            for (WsColumn col : columnFilters.keySet()) {
                String clause = null;
                switch (col.getDataType()) {
                    case "character":
                        clause = " WHERE LOWER(" + col.getName() + ") like ? ESCAPE '!'";
                        break;
                    case "integer":
                        clause = " WHERE " + col.getName() + " = ?";
                        break;
                    case "real":
                        clause = " WHERE " + col.getName() + " = ?";
                        break;
                    default:
                        clause = " WHERE " + col.getName() + " = ?";
                        break;
                }
                sb.append(clause);
            }
        }
        if (null != numRows)
            //sb.append(" fetch first "+numRows + " rows only");
            sb.append(" limit "+numRows+ " offset "+firstRow);

        PreparedStatement p = conn.prepareStatement(sb.toString());
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
                    default:
                        p.setString(cnt++, columnFilters.get(col));
                        break;
                }
            }
        }
        System.out.println(p.toString());
        return p;
    }
}