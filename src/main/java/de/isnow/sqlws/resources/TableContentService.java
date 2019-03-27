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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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

    @POST
    @Path("schema/{schemaid}/table/{tableid}/row/{pk}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public Map getContentsOfRow(
            @PathParam("schemaid") String schemaId,
            @PathParam("tableid") String tableId,
            @FormDataParam("pks") Map<String, String> primaryKeys,
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
        final Set<String> lColumnsToShow =
                ((null == columnsToShow) || (columnsToShow.isEmpty())) ?
                  columnsToShow = table.getColumnsByName().keySet()
                : columnsToShow;
        Map<WsColumn, String> pks = new HashMap<>();
        //TODO generalize for compound PKs. How to encode in URL, or move to POST?
        /*List<WsColumn> pkCols = table.getPrimaryKeyColumns();
        pkCols.forEach((c) -> {
            pks.put(c, primaryKeys.get(c));
        });*/
        Map<String, Object> row = readTableRow(
              table, pks, columnsToShow, conn, depth);
        List<Map> children = new ArrayList<>();
        if (depth > 0) {
            table.getChildTables().forEach((t) -> {
                Set<WsTable.WsForeignKey> fkCols = table.getForeignKeys();
                fkCols.forEach((c) -> {
                    c.getPrimaryForeignKeyRelationships().forEach((f) -> {
                        System.out.println(f);
                                            });
                    //pks.put(c.getPrimaryForeignKeyRelationships(), primaryKeys.get(c));
                });
                Map<String, Object> cRow = readTableRow(
                        t, pks, lColumnsToShow, conn, depth);
                children.add(cRow);
            });
        }
        Map<String, Object> response = RestUtils.createJsonWrapper(row);
        response.put("id", tableId);
        response.put("model", table.getColumns());
        response.put("children", children);
        return response;
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
