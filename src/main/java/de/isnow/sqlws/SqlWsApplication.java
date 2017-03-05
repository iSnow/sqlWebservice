package de.isnow.sqlws;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.glassfish.jersey.linking.DeclarativeLinkingFeature;

import de.isnow.sqlws.db.HSQLSetup;
import de.isnow.sqlws.resources.DatabaseService;
import de.isnow.sqlws.resources.RootService;
import de.isnow.sqlws.resources.TableService;
import de.isnow.sqlws.util.RestUtil;
import de.isnow.sqlws.util.XmlConfigUtil;

import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlWsApplication extends Application<SqlWsConfiguration> {
	private BundleInitializer initializer;

	// for external configuration, remove the second arg
	public static void main(final String[] args) throws Exception {
		log.debug("Application start up");
		new SqlWsApplication()
		.run(new String[]{"server", "configuration.yml"});
	}


	@Override
	public String getName() {
		return "sqlWebservice";
	}
	
	@Override
	/**
	 * Extendible initialize() function. If the setInitializer() method was used
	 * to set a BundleInitializer, call it here.
	 * @see io.dropwizard.Application#initialize(io.dropwizard.setup.Bootstrap)
	 */
	public void initialize(final Bootstrap<SqlWsConfiguration> bootstrap) {
		bootstrap.setConfigurationSourceProvider(
				new ResourceConfigurationSourceProvider());
		if (null != initializer)
			initializer.initialize (bootstrap);
		bootstrap.addBundle(new SwaggerBundle<SqlWsConfiguration>() {
			@Override
			protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(SqlWsConfiguration configuration) {
				return configuration.swaggerBundleConfiguration;
			}
		});
	}

	@Override
	
	/**
	 * Extendible run() function. If the setInitializer() method was used
	 * to set a BundleInitializer, call it here.

	 * Parses command-line arguments and runs the application. Call this method from a public static void main entry point in your application.

	 * @Parameters:  arguments the command-line arguments
	 * @Throws: Exception - if something goes wrong
	 *
	 * @see io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)
	 */
	public void run(final SqlWsConfiguration configuration,
			final Environment environment) {
		if (null != initializer)
			initializer.runConfig(configuration, environment);
		
		environment.jersey().register(new DatabaseService());
		environment.jersey().register(new RootService());
		environment.jersey().register(new TableService());
		environment.jersey().packages("de.isnow.sqlws;de.isnow.sqlws.model;de.isnow.sqlws.resources;org.glassfish.jersey.linking");
		environment.jersey().register(DeclarativeLinkingFeature.class);
	}

	public SqlWsApplication() {
		log.info("ResourceConfig Application init");
		init(); 
	}

	public void init()  {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream in = classLoader.getResourceAsStream ("sqlrestconf.xml");

			Map<String, String> valuePairs = XmlConfigUtil.readConfig(in);
			RestUtil.takeValuesFromConfig(valuePairs);

			Class.forName(valuePairs.get("jdbc-driver-class"));
			System.out.println("test:" + valuePairs);

			HSQLSetup setup = new HSQLSetup();
			Map<String, Object> kv = new HashMap<>();
			kv.put("classloader", classLoader);
			kv.put("jarpath", "data"+ File.separator);
			setup.beforeStart(new URI(valuePairs.get("database-url")), kv);

			DbConnectionStore.newConnection(
					//getDatabaseUrl(valuePairs, ctx), 
					getDatabaseUrl(valuePairs, null), 
					valuePairs.get("user"), 
					valuePairs.get("password"));
			System.out.println("connected");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks if the database URL equals "jdbc:hsqldb:exampledb". In this case
	 * get the real absolute path of the database. This is only used if the
	 * internal example DB should be used.
	 * 
	 * @param valuePairs  with the sqlrestconf name/value pairs
	 * @param ctx the servletcontext we are running in
	 * @return database URL
	 */
	private static String getDatabaseUrl(Map<String, String> valuePairs, ServletContext ctx) {
		String url = valuePairs.get("database-url");

		if ("jdbc:hsqldb:exampledb".equalsIgnoreCase(valuePairs.get("database-url"))) {
			url = "jdbc:hsqldb:" + System.getProperty("java.io.tmpdir") + "exampledb";
		}
		return url;
	}


	/**
	 * Set an initializer that gets called from initialize() to allow for 
	 * additional configuration
	 * @param initializer the initializer to set
	 */
	public SqlWsApplication setInitializer(BundleInitializer initializer) {
		this.initializer = initializer;
		return this;
	}


}
