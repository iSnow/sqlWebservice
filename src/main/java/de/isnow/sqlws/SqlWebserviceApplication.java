package de.isnow.sqlws;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import de.isnow.sqlws.util.RestUtil;
import de.isnow.sqlws.util.XmlConfigUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlWebserviceApplication extends ResourceConfig {

	@Context
	ServletContext ctx;
	

	public SqlWebserviceApplication(@Context ServletContext servletContext) {
		this.ctx = servletContext;
		log.info("ResourceConfig Application init");
		log.info(ctx.toString());
	    packages("de.isnow.sqlws;de.isnow.sqlws.model;de.isnow.sqlws.rest;org.glassfish.jersey.linking").register(DeclarativeLinkingFeature.class);
	       
		File path = new File(ctx.getRealPath("/"));
		ctx.setAttribute("warpath", path);
		init();
	}

	public void init()  {
		File path = (File)ctx.getAttribute("warpath");
		File conf = new File(path, "/WEB-INF/sqlrestconf.xml");
		if (!conf.exists()) {
			throw new RuntimeException("Configuration file /WEB-INF/sqlrestconf.xml is missing!");
		}

		Map<String, String> valuePairs = XmlConfigUtil.readConfig(path);
		RestUtil.takeValuesFromConfig(valuePairs);

		try {
			Class.forName(valuePairs.get("jdbc-driver-class"));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		System.out.println("test:" + valuePairs);
		
		DbConnectionStore.newConnection(
			getDatabaseUrl(valuePairs, ctx), 
			valuePairs.get("user"), 
			valuePairs.get("password"));
	}

	/**
	 * Checks if the database URL equals "jdbc:hsqldb:exampledb". In this case
	 * get the real absolute path of the database. This is only used if the
	 * internal example DB should be used.
	 * 
	 * @param valuePairs
	 *            with the sqlrestconf name/value pairs
	 * @param ctx
	 *            the servletcontext we are running in
	 * @return database URL
	 */
	private static String getDatabaseUrl(Map<String, String> valuePairs, ServletContext ctx) {
		String url = valuePairs.get("database-url");

		if ("jdbc:hsqldb:exampledb".equalsIgnoreCase(valuePairs.get("database-url"))) {
			url = "jdbc:hsqldb:" + ctx.getRealPath("/WEB-INF/data/") + File.separator + "exampledb";
		}
		return url;
	}
}
