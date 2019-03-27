package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.util.DbUtil;
import de.isnow.sqlws.util.RestUtils;
import lombok.SneakyThrows;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jboss.logging.Param;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        Map<String, String> pks = getKeyValues(primaryKeys);
        //testPksMatch(table, pks);

        WsCatalog catalog = schema.getOwningCatalog();
        WsConnection conn = catalog.getOwningConnection();

        Map<String, Object> row = readTableRow(
              table, pks, columnsToShow, conn);

        List<Map> children = new ArrayList<>();
        if (depth > 0) {
            List<WsTable> cts = table.getChildTables();
            cts.forEach((t) -> {
                Set<WsTable.WsForeignKey> fkCols = t.getForeignKeys();
                fkCols.forEach((c) -> {
                    if (c.getParentTableKey().equals(table.getFullName())) {
                        c.getPrimaryForeignKeyRelationships().forEach((f) -> {
                            String pk = f.get("pk");
                            String fk = f.get("fk");

                            get pk value from 'pks'
                                    create another map like pks, set fk to value from pks
                                    readTableRows (could be many)
                        });
                    }
                    //pks.put(c.getPrimaryForeignKeyRelationships(), primaryKeys.get(c));
                });
                //Map<String, Object> cRow = readTableRow(
                  //      t, pks, lColumnsToShow, conn, depth);
                //children.add(cRow);
            });
        }
        Map<String, Object> response = RestUtils.createJsonWrapper(row);
        response.put("id", tableId);
        response.put("model", table.getColumns());
        response.put("children", children);
        return response;
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
    }

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
    }
}
