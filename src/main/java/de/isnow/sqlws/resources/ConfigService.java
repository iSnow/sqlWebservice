package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.isnow.sqlws.SqlWsApplication;
import de.isnow.sqlws.model.WsConnection;
import de.isnow.sqlws.model.config.RouterConfig;
import de.isnow.sqlws.model.config.SqlRestConfiguration;
import lombok.SneakyThrows;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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


    @SneakyThrows
    private static RouterConfig getRouterConfig(SqlRestConfiguration sqlRestConfig, ClassLoader classLoader) {
        InputStream in;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String routerConfigPath = (String)sqlRestConfig.getApplication().get("routeConfig");
        File f = new File(routerConfigPath);
        if (f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = classLoader.getResourceAsStream("router-config-small.yml");
        }
        RouterConfig cfg = mapper.readValue(in, RouterConfig.class);
        return cfg;
    }
}
