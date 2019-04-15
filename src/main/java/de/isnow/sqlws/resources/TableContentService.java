package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.model.viewModel.VmColumn;
import de.isnow.sqlws.model.viewModel.VmForeignKey;
import de.isnow.sqlws.model.viewModel.VmObject;
import de.isnow.sqlws.model.viewModel.VmTable;
import de.isnow.sqlws.util.DbUtil;
import de.isnow.sqlws.util.RestUtils;
import lombok.SneakyThrows;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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

        final Map<String, String> pks = getKeyValues(primaryKeys, table);
        testPksMatch(table, pks);

        VmTable tableToReturn = VmTable.fromWsTable(table, columnsToShow, depth, true);
        transformTable(table, tableToReturn, pks, conn);

        Map<String, Object> response = RestUtils.createJsonWrapper(new Object[]{tableToReturn});
        response.put("id", tableId);
        return response;
    }


    @POST
    @Path("schema/{schemaid}/table/{tableid}/pk/{pks: .*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public Map updateRow(
            @PathParam("schemaid") String schemaId,
            @PathParam("tableid") String tableId,
            @PathParam("pks") List<PathSegment> primaryKeys,
            @FormDataParam("data") String formData) {
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
        ObjectMapper mapper = new ObjectMapper();
        VmTable vtable = mapper.readValue(formData, VmTable.class);

        Map<String, Object> response = RestUtils.createJsonWrapper(new Object[]{vtable});
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

        tableToReturn.setRowValues(row);
        tableToReturn.getChildren().forEach((c) -> {
            String fName = c.getFullName();
            List<WsTable> children = table.getChildTables()
                .stream()
                .filter((ct) -> {return ct.getFullName().equals(fName);})
                .collect(Collectors.toList());
            if (children.size() > 0) {
                c.setForeignKeys(tableToReturn);
                transformTable(children.get(0), c, c.getChildKeyValues(), conn);
            }
        });
    }


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

    private static Map<String, String> getKeyValues(List<PathSegment> pathSegments, WsTable table) {
        Map<String, String> kvs = new LinkedHashMap<>();
        Map<String, String> retVal = new LinkedHashMap<>();
        String key = null;
        for (PathSegment seg : pathSegments) {
            if (null == key)
                key = seg.getPath();
            else {
                kvs.put(key, seg.getPath());
                key = null;
            }
        }
        kvs.keySet().forEach((kv) -> {
            WsColumn col = table.getColumnByName(kv);
            if (null != col) {
                retVal.put(col.getFullName(), kvs.get(kv));
            }
        });
        return retVal;
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
            pkCols.put(table.getColumnByFullName(k), pks.get(k));
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

}
