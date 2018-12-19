package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.util.DbUtil;
import de.isnow.sqlws.util.RestUtils;
import lombok.SneakyThrows;

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
        System.out.println(filters);
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
        else if (columnsToShow.size() == 1) {
            // this is the case when @QueryParam("columnsToShow") contains
            // an empty set - show all columns in that case
            if (columnsToShow.iterator().next().isEmpty()) {
                columnsToShow = table.getColumnsByName().keySet();
            }
        }

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
        Map<String, Object> response = RestUtils.createJsonWrapper(retVal);
        response.put("id", tableId);
        response.put("model", table.getColumns());
        return response;
    }
}