package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.model.viewModel.VmColumn;
import de.isnow.sqlws.model.viewModel.VmForeignKey;
import de.isnow.sqlws.model.viewModel.VmObject;
import de.isnow.sqlws.model.viewModel.VmTable;
import de.isnow.sqlws.util.DbUtil;
import de.isnow.sqlws.util.RestUtils;
import lombok.SneakyThrows;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Path("/data")
public class TableContentService {

    @GET
    @Path("/table/{tableid}")
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public Map getContents(
            @PathParam("tableid") String tableId,
            @QueryParam ("startrecord") @DefaultValue("0")   Long startRecord,
            @QueryParam ("maxrecords")  @DefaultValue("100") Long maxRecords,
            @QueryParam("columnsToShow") Set<String> columnsToShow,
            @QueryParam("filters") Set<String> filters
    ) throws JsonProcessingException {
        Map<WsColumn, String> colFilters = new HashMap<>();
        WsTable table = WsTable.get(tableId);
        if (null == table) {
            return null;
        }
        filters.forEach((f) -> {
            String parts[] = f.split(":");
            if (parts.length > 1) {
                WsColumn col = WsColumn.get(parts[0].replace("filter-", ""));
                colFilters.put(col, parts[1]);
            }
        });
        WsSchema schema = table.getOwningSchema();
        WsCatalog catalog = schema.getOwningCatalog();
        WsConnection conn = catalog.getOwningConnection();
        if ((null == columnsToShow) || (columnsToShow.isEmpty()))
            columnsToShow = table.getColumnsByName().keySet();

        PreparedStatement p = DbUtil.createLimitedReadQuery(
                table,
                startRecord,
                maxRecords,
                columnsToShow,
                colFilters,
                conn.getNativeConnection());
        ResultSet rs = p.executeQuery();
        int cnt = 0;
        List retVal = new ArrayList();
        if (null == maxRecords) {
            while (rs.next())  {
                List row = new ArrayList();
                retVal.add(row);
                for (String name : columnsToShow) {
                    row.add(rs.getObject(name));
                }
            }
        } else {
            while ((rs.next() && (cnt < maxRecords)))  {
                List row = new ArrayList();
                retVal.add(row);
                for (String name : columnsToShow) {
                    row.add(rs.getObject(name));
                }
                cnt++;
            }
        }
        Map<String, Object> response = RestUtils.createJsonWrapperForCollection(retVal);
        response.put("id", tableId);
        response.put("model", table.getColumns());
        return response;
    }

    @GET
    @Path("schema/{schemaid}/table/{tableid}/pk/{pks: .*}")
    //@Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public Map getContentsOfRow(
            @PathParam("schemaid") String schemaId,
            @PathParam("tableid") String tableId,
            @PathParam("pks") List<PathSegment> primaryKeys,
            @QueryParam("columnsToShow") Set<String> columnsToShow,
            @QueryParam("depth") int depth) {
        WsSchema schema = WsSchema.get(schemaId);
        if (null == schema)
            return null;
        WsTable table = WsTable.get(tableId);
        if (null == table) {
            return null;
        }
        WsSchema schema2 = table.getOwningSchema();
        if (!schema2.equals(schema))
            return null;

        WsCatalog catalog = schema.getOwningCatalog();
        WsConnection conn = catalog.getOwningConnection();

        final Map<String, String> pks = getKeyValues(primaryKeys);
        //testPksMatch(table, pks);

        VmTable tableToReturn = VmTable.fromWsTable(table, columnsToShow, depth, true);
        transformTable(table,tableToReturn,pks,conn);
        if (depth > 0) {
            List<WsTable> cts = table.getChildTables();
            Set<VmForeignKey> newFks = new HashSet<>();
            cts.forEach((t) -> {
                //VmTable childTable = VmTable.fromWsTable(t, null, depth-1, true);

                VmTable childTable = tableToReturn.getChildTableByFullName(t.getFullName());
                VmForeignKey fk = tableToReturn.getMatchingFKs(childTable);
                fk.getPrimaryForeignKeyRelationships().forEach((m) -> {
                    VmColumn c = tableToReturn.getColumnByFullName((String)m.get("pk"));
                    if (null != c)
                        m.put("value", c.getValue());
                });
                childTable.addForeignKey(fk);
                newFks.add(fk);
            });
            tableToReturn.setForeignKeys(newFks);
        }
        Map<String, Object> response = RestUtils.createJsonWrapper(new Object[]{tableToReturn});
        response.put("id", tableId);
        return response;
    }

    private static void transformTable(
            WsTable table,
            VmTable tableToReturn,
            Map<String, String> pks,
            WsConnection conn) {
        Set<String> colNames = tableToReturn
                .getColumns()
                .stream()
                .map(VmObject::getName).collect(Collectors.toSet());
        final Set<WsColumn> lColumnsToShow = table.getColumnsToShow(colNames);


        final Map<String, Object> row = readTableRow(
                table, pks, lColumnsToShow, conn);

        tableToReturn.getColumns().forEach((c) -> c.setValue(row.get(c.getName())));
    }

    private static void transformSubTable(
            WsTable table,
            VmTable tableToReturn,
            Map<String, String> pks,
            WsConnection conn) {
        Set<String> colNames = tableToReturn
                .getColumns()
                .stream()
                .map(VmObject::getName).collect(Collectors.toSet());
        final Set<WsColumn> lColumnsToShow = table.getColumnsToShow(colNames);


        final Map<String, Object> row = readTableRow(
                table, pks, lColumnsToShow, conn);

        tableToReturn.getColumns().forEach((c) -> c.setValue(row.get(c.getName())));
    }

    /*private static Set<VmColumn> transformTable(
            WsTable table,
            Set<String> columnsToShow,
            Map<String, String> pks,
            WsConnection conn) {
        final Set<WsColumn> lColumnsToShow = getColumnsToShow(table, columnsToShow);
        final Set<VmColumn> lColumnsToReturn = lColumnsToShow
                .stream()
                .map(VmColumn::fromWsColumn)
                .collect(Collectors.toSet());

        final Map<String, Object> row = readTableRow(
                table, pks, lColumnsToShow, conn);

        lColumnsToReturn.forEach((c) -> c.setValue(row.get(c.getName())));
        return lColumnsToReturn;
    }

    private static Set<WsColumn> getColumnsToShow(WsTable table, Set<String> columnsToShow) {
        Set<String> lColumnsToShow =
                ((null == columnsToShow) || (columnsToShow.isEmpty())) ?
                        table.getColumnsByName().keySet()
                        : columnsToShow;
        return lColumnsToShow.stream().map((name) ->
            table.getColumnByName(name))
                .collect(Collectors.toSet());
    }*/

    private void testPksMatch(WsTable table, Map<String, String> pks) {
        List<String> pkColNames = table
                .getPrimaryKeyColumns()
                .stream()
                .map(WsObject::getFullName)
                .collect(Collectors.toList());
        List<String> inPkNames = new ArrayList<>(pks.keySet());
        if (!pkColNames.equals(inPkNames)) {
            throw new IllegalArgumentException("list of primary keys mismatches table primary keys");
        }
    }

    private static Map<String, String> getKeyValues(List<PathSegment> pathSegments) {
        Map<String, String> kvs = new LinkedHashMap<>();
        String key = null;
        for (PathSegment seg : pathSegments) {
            if (null == key)
                key = seg.getPath();
            else {
                kvs.put(key, seg.getPath());
                key = null;
            }
        }
        return kvs;
    }

    @SneakyThrows
    private static List<Map<String, Object>> readDependentTable (
            WsTable parentTable,
            WsTable childTable,
            Map<String, String> pkVals,
            Set<String> columnsToShow,
            WsConnection conn) {
        List<Map<String, Object>> retVal = new ArrayList<>();
        final Set<String> lColumnsToShow =
                ((null == columnsToShow) || (columnsToShow.isEmpty())) ?
                        childTable.getColumnsByName().keySet()
                        : columnsToShow;

        final Map<WsColumn, WsColumn> childPks = new HashMap<>();
        Set<WsTable.WsForeignKey> fkCols = childTable.getForeignKeys();
        fkCols.forEach((c) -> {
            if (c.getParentTableKey().equals(parentTable.getFullName())) {
                c.getPrimaryForeignKeyRelationships().forEach((f) -> {
                    WsColumn pfkCol = parentTable.getColumnByFullName(f.getPkColumnName());
                    WsColumn ffkCol = childTable.getColumnByFullName(f.getFkColumnName());
                    childPks.put(pfkCol, ffkCol);
                    try {
                        PreparedStatement s = DbUtil.createChildTableReadQuery(
                                childTable, childPks, pkVals,
                                null, conn.getNativeConnection());
                        ResultSet rs = s.executeQuery();
                        while (rs.next()) {
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (String name : lColumnsToShow) {
                                row.put(name, rs.getObject(name));
                            }
                            retVal.add(row);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                });
            }
        });

        return retVal;
    }

    /*@SneakyThrows
    private static Map<String, Object> readDependentTableRow(
            WsTable table,
            Map<String, String> fks,
            Set<String> columnsToShow,
            WsConnection conn) {
        final Set<String> lColumnsToShow =
                ((null == columnsToShow) || (columnsToShow.isEmpty())) ?
                        table.getColumnsByName().keySet()
                        : columnsToShow;

        Map<String, WsColumn> colsDict = table.getColumnsByFullName();
        Map<WsColumn, String> keyCols = new HashMap<>();
        fks.keySet().stream().forEach((k) -> {
            keyCols.put(colsDict.get(k), fks.get(k));
        });
        PreparedStatement p = DbUtil.createSingleReadQuery(
                table,
                keyCols,
                lColumnsToShow,
                conn.getNativeConnection());

        ResultSet rs = p.executeQuery();
        Map<String, Object> row = new LinkedHashMap<>();
        if (rs.next()) {
            for (String name : lColumnsToShow) {
                row.put(name, rs.getObject(name));
            }
        }
        return row;
    }*/

    /*@SneakyThrows
    private static Map<String, Object> readTableRow(
            WsTable table,
            Map<String, String> pks,
            Set<String> columnsToShow,
            WsConnection conn) {
        final Set<String> lColumnsToShow =
                ((null == columnsToShow) || (columnsToShow.isEmpty())) ?
                        table.getColumnsByName().keySet()
                        : columnsToShow;

        Map<String, WsColumn> colsDict = table.getColumnsByName();
        Map<WsColumn, String> pkCols = new HashMap<>();
        pks.keySet().stream().forEach((k) -> {
            pkCols.put(colsDict.get(k), pks.get(k));
        });
        PreparedStatement p = DbUtil.createSingleReadQuery(
                table,
                pkCols,
                lColumnsToShow,
                conn.getNativeConnection());

        ResultSet rs = p.executeQuery();
        Map<String, Object> row = new LinkedHashMap<>();
        if (rs.next()) {
            for (String name : lColumnsToShow) {
                row.put(name, rs.getObject(name));
            }
        }
        return row;
    }*/

    @SneakyThrows
    private static Map<String, Object> readTableRow (
            WsTable table,
            Map<String, String> pks,
            Set<WsColumn> columnsToShow,
            WsConnection conn) {
        Set<String> lColumnsToShow = null;
        if ((null == columnsToShow) || (columnsToShow.isEmpty())) {
            lColumnsToShow = table.getColumnsByName().keySet();
        } else {
            lColumnsToShow = columnsToShow
                    .stream()
                    .map(WsObject::getName)
                    .collect(Collectors.toSet());
        }
        Map<WsColumn, String> pkCols = new HashMap<>();
        pks.keySet().forEach((k) -> {
            pkCols.put(table.getColumnByName(k), pks.get(k));
        });
        PreparedStatement p = DbUtil.createSingleReadQuery(
                table,
                pkCols,
                lColumnsToShow,
                conn.getNativeConnection());

        ResultSet rs = p.executeQuery();
        Map<String, Object> row = new LinkedHashMap<>();
        if (rs.next()) {
            for (String name : lColumnsToShow) {
                row.put(name, rs.getObject(name));
            }
        }
        return row;
    }
/*
    @SneakyThrows
    private static Map<String, Object> readTableRow(
            WsTable table,
            Map<WsColumn, String> pks,
            Set<String> columnsToShow,
            WsConnection conn,
            int depth) {

        PreparedStatement p = DbUtil.createSingleReadQuery(
                table,
                pks,
                columnsToShow,
                conn.getNativeConnection());

        ResultSet rs = p.executeQuery();
        Map<String, Object> row = new LinkedHashMap<>();
        if (rs.next()) {
            for (String name : columnsToShow) {
                row.put(name, rs.getObject(name));
            }
        }
        return row;
    }*/
}
