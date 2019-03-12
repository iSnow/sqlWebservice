package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.isnow.sqlws.SqlWsApplication;
import de.isnow.sqlws.model.*;
import de.isnow.sqlws.model.config.RouterConfig;
import de.isnow.sqlws.model.config.SqlRestConfiguration;
import de.isnow.sqlws.model.layoutModel.*;
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
    public Map getTableConfig(
            @PathParam("tableid") String tableId,
            @QueryParam("columnsToShow") Set<String> columnsToShow
    )  {
        WsTable table = WsTable.get(tableId);
        if (null == table) {
            return null;
        }
        if ((null == columnsToShow) || (columnsToShow.isEmpty()))
            columnsToShow = table.getColumnsByName().keySet();
        Map<String,List<WsColumn>> colMap = table.getColumnsByType();
        List<LmObject> configs = new ArrayList<>();
        int checkBoxCounter = 0;
        int paragraphCounter = 0;
        int lobCounter = 0;
        for (String type : colMap.keySet()) {
            List<WsColumn> cols = colMap.get(type);
            for (WsColumn col : cols) {
                LmObject cfg;
                switch (type) {
                    case "bit":
                        cfg = new LmCheckBox();
                        cfg.setContainerId(checkBoxCounter++);
                        break;
                    case "character":
                        cfg = new LmParagraph();
                        cfg.setContainerId(paragraphCounter++);
                        break;
                    case "large_object":
                        cfg = new LmBox();
                        cfg.setContainerId(lobCounter++);
                        break;
                    case "integer":
                        cfg = new LmParagraph();
                        cfg.setContainerId(paragraphCounter++);
                        break;
                    case "real":
                        cfg = new LmParagraph();
                        cfg.setContainerId(paragraphCounter++);
                        break;
                    case "temporal":
                        cfg = new LmParagraph();
                        cfg.setContainerId(checkBoxCounter++);
                        break;
                    default:
                        cfg = new LmBox();
                        cfg.setContainerId(lobCounter++);
                }
                cfg.setColumnName(col.getName());
                cfg.setColumnId(col.getId());
                configs.add(cfg);
            }
        }
        List<LmObject> layouts = new ArrayList<>();
        List<LmObject> paragraphs = filterEntities (configs, "paragraph");
        addToBox(paragraphs, layouts);
        List<LmObject> checkboxes = filterEntities (configs, "checkbox");
        addToBox(checkboxes, layouts);
        List<LmObject> boxes = filterEntities (configs, "box");
        layouts.addAll(boxes);
        LmLayout wrapper = new LmLayout();
        wrapper.setContainerId(0);
        wrapper.addChildren(layouts);
        Map retVal = new TreeMap();
        retVal.put("id", tableId);
        retVal.put("data", wrapper);
        return retVal;
    }

    private static void addToBox(List<LmObject> paragraphs, List<LmObject> layouts) {
        int numParagraphs = paragraphs.size();
        int numParagraphBoxes = numParagraphs/10+1;
        for (int i = 0; i < numParagraphBoxes; i++) {
            LmObject cfg = new LmBox();
            cfg.setContainerId(i);
            cfg.setOrientation(LmObject.Orientation.VERTICAL.toString());
            int endIdx = (((i+1)*10) > numParagraphs) ? numParagraphs : ((i+1)*10);
            List<LmObject> sublist = paragraphs.subList(i*10, endIdx);
            for (LmObject lCfg : sublist) {
                cfg.getChildren().add(lCfg);
            }
            layouts.add(cfg);
        }
    }

    private static List<LmObject> filterEntities (List<LmObject> configs, String type) {
        List<LmObject> ents = configs
                .stream()
                .filter((cfg)-> cfg.getType().equals(type))
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
