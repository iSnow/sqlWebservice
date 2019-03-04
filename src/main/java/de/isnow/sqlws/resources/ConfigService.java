package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.isnow.sqlws.SqlWsApplication;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.model.config.RouterConfig;
import de.isnow.sqlws.model.config.SqlRestConfiguration;
import lombok.SneakyThrows;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigService {

    @Context
    ServletContext ctx;

    @GET
    @Path("/routes")
    public Response getRoot() {
        RouterConfig cfg = getRouterConfig(
                SqlWsApplication.getSqlRestConfig(),
                getClass().getClassLoader());
        return Response.ok(cfg).build();
    }


    @GET
    @Path("/table/{tableid}")
    @Produces(MediaType.APPLICATION_JSON)
    @SneakyThrows
    public List<LayoutConfig> getTableConfig(
            @PathParam("tableid") String tableId,
            @QueryParam("columnsToShow") Set<String> columnsToShow
    ) throws JsonProcessingException {
        WsTable table = WsTable.get(tableId);
        if (null == table) {
            return null;
        }
        if ((null == columnsToShow) || (columnsToShow.isEmpty()))
            columnsToShow = table.getColumnsByName().keySet();
        Map<String,List<WsColumn>> colMap = table.getColumnsByType();
        List<ColumnLayoutConfig> configs = new ArrayList<>();
        int checkBoxCounter = 0;
        int paragraphCounter = 0;
        int lobCounter = 0;
        for (String type : colMap.keySet()) {
            List<WsColumn> cols = colMap.get(type);
            for (WsColumn col : cols) {
                ColumnLayoutConfig cfg = new ColumnLayoutConfig();
                cfg.setType(type);
                cfg.setColumnName(col.getName());
                cfg.setColumnId(col.getId());
                switch (type) {
                    case "bit":
                        cfg.setContainerType("checkbox");
                        cfg.setCointainerId("checkbox" + checkBoxCounter++);
                        break;
                    case "character":
                        cfg.setContainerType("paragraph");
                        cfg.setCointainerId("paragraph" + paragraphCounter++);
                        break;
                    case "large_object":
                        cfg.setContainerType("box");
                        cfg.setCointainerId("box" + lobCounter++);
                        break;
                    case "integer":
                        cfg.setContainerType("paragraph");
                        cfg.setCointainerId("paragraph" + paragraphCounter++);
                        break;
                    case "real":
                        cfg.setContainerType("paragraph");
                        cfg.setCointainerId("paragraph" + paragraphCounter++);
                        break;
                    case "temporal":
                        cfg.setContainerType("paragraph");
                        cfg.setCointainerId("paragraph" + checkBoxCounter++);
                        break;
                }
                configs.add(cfg);
            }
        }
        List<LayoutConfig> layouts = new ArrayList<>();
        List<ColumnLayoutConfig> paragraphs = filterEntities (configs, "paragraph");
        addToBox(paragraphs, layouts);
        List<ColumnLayoutConfig> checkboxes = filterEntities (configs, "checkbox");
        addToBox(checkboxes, layouts);
        List<ColumnLayoutConfig> boxes = filterEntities (configs, "box");
        layouts.addAll(boxes);

        return layouts;
    }

    private static void addToBox(List<ColumnLayoutConfig> paragraphs, List<LayoutConfig> layouts) {
        int numParagraphs = paragraphs.size();
        int numParagraphBoxes = numParagraphs/10+1;
        for (int i = 0; i < numParagraphBoxes; i++) {
            LayoutConfig cfg = new LayoutConfig();
            cfg.setContainerType("box");
            cfg.setCointainerId("box"+i);
            int endIdx = (((i+1)*10) > numParagraphs) ? numParagraphs : ((i+1)*10);
            List<ColumnLayoutConfig> sublist = paragraphs.subList(i*10, endIdx);
            for (ColumnLayoutConfig lCfg : sublist) {
                cfg.getChildren().add(lCfg);
            }
            layouts.add(cfg);
        }
    }

    private static List<ColumnLayoutConfig> filterEntities (List<ColumnLayoutConfig> configs, String type) {
        List<ColumnLayoutConfig> ents = configs
                .stream()
                .filter((cfg)->{return cfg.getContainerType().equals(type);})
                .collect(Collectors.toList());
        return ents;
    }


    @SneakyThrows
    private static RouterConfig getRouterConfig(SqlRestConfiguration sqlRestConfig, ClassLoader classLoader) {
        InputStream in;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String routerConfigPath = (String)sqlRestConfig.getApplication().get("routeConfig");
        File f = new File(routerConfigPath);
        if (f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = classLoader.getResourceAsStream("router-config.yml");
        }
        RouterConfig cfg = mapper.readValue(in, RouterConfig.class);
        return cfg;
    }
}
