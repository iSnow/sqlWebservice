package de.isnow.sqlws;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.sql.Connection;
import java.util.*;

import javax.persistence.*;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import de.isnow.sqlws.db.WsPersistenceUnitInfo;
import de.isnow.sqlws.model.WsConnection;
import de.isnow.sqlws.resources.ObjectMapperContextResolver;
import io.dropwizard.assets.AssetsBundle;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;

import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jpa.HibernatePersistenceProvider;
import schemacrawler.schemacrawler.*;

@Slf4j
public class SqlWsApplication extends Application<SqlWsConfiguration> {
	private BundleInitializer initializer;

	/*@Getter
	@Setter
	private static Connection conn;*/

	// for external configuration, remove the second arg
	public static void main(final String[] args) throws Exception {
		log.debug("Application start up");
		new SqlWsApplication().run("server", "configuration.yml");
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
		// this path is also configured as assetsRoot in index.js for the NodeJS-based Javascript build tools
		bootstrap.addBundle(new AssetsBundle("/assets/sqlWebserv/dist", "/", "index.html"));
	}

	
	/**
	 * Extendible run() function. If the setInitializer() method was used
	 * to set a BundleInitializer, call it here.

	 * Parses command-line arguments and runs the application. Call this method from a public static void main entry point in your application.

	 * @Parameters:  arguments the command-line arguments
	 * @Throws: Exception - if something goes wrong
	 *
	 * @see io.dropwizard.Application#run(io.dropwizard.Configuration, io.dropwizard.setup.Environment)
	 */
	@Override
	public void run(final SqlWsConfiguration configuration,
			final Environment environment) {
		if (null != initializer)
			initializer.runConfig(configuration, environment);

		environment.jersey().packages("de.isnow.sqlws;de.isnow.sqlws.model;de.isnow.sqlws.resources;org.glassfish.jersey.linking");
		environment.jersey().register(DeclarativeLinkingFeature.class);
		environment.jersey().register(ObjectMapperContextResolver.class);
		environment.jersey().register(JacksonJaxbJsonProvider.class);
		environment.jersey().setUrlPattern("/api/v1/*");

		configureCors(environment);
	}

	private void configureCors(Environment environment) {
		final FilterRegistration.Dynamic cors =
				environment.servlets().addFilter("CORS", CrossOriginFilter.class);

		// Configure CORS parameters
		cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
		cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
		cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

		// Add URL mapping
		cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

		// DO NOT pass a preflight request to down-stream auth filters
		// unauthenticated preflight requests should be permitted by spec
		cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, Boolean.FALSE.toString());

	}

	public SqlWsApplication() {
		log.info("ResourceConfig Application init");
		init(); 
	}

	public void init()  {
		try {
			SchemaCrawlerOptions options = configureOptions();
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			ConnectionConfig config=null;
			try {
				ClassLoader classLoader = getClass().getClassLoader();
				InputStream in = classLoader.getResourceAsStream("sqlrestconf.yml");
				config = mapper.readValue(in, ConnectionConfig.class);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

			/*WsConnection conn = new WsConnection(
					config.databaseUrl,
					config.user,
					config.password,
					config.name,
					options);*/
			/*Map<String, String> persistenceMap = new HashMap<String, String>();
			persistenceMap.put("javax.persistence.jdbc.url", config.databaseUrl);
			persistenceMap.put("javax.persistence.jdbc.user", config.user);
			persistenceMap.put("javax.persistence.jdbc.password", config.password);
			persistenceMap.put("javax.persistence.jdbc.driver", config.jdbcDriverClass);*/

			Properties props = new Properties();
			props.put("javax.persistence.jdbc.url", config.databaseUrl);
			props.put("javax.persistence.jdbc.user", config.user);
			props.put("javax.persistence.jdbc.password", config.password);
			props.put("name", config.name);

			WsPersistenceUnitInfo persistenceUnitInfo = new WsPersistenceUnitInfo();
			persistenceUnitInfo.setProperties(props);

			HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
			EntityManagerFactory entityManagerFactory = hibernatePersistenceProvider
					.createContainerEntityManagerFactory(persistenceUnitInfo, Collections.EMPTY_MAP);


			new WsConnection(
					entityManagerFactory,
					config.databaseUrl,
					config.name,
					options);

			System.out.println("connected");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	SchemaCrawlerOptions configureOptions() {
		// Create the options
		final SchemaCrawlerOptions options = SchemaCrawlerOptionsBuilder.withMaximumSchemaInfoLevel();
		// 	withSchemaInfoLevel(SchemaInfoLevel schemaInfoLevel)
		// Set what details are required in the schema - this affects the
		// time taken to crawl the schema
        //options.setSchemaInfoLevel(SchemaCrawlerOptionsBuilder.withMaximumSchemaInfoLevel());
		//options.setRoutineInclusionRule(new ExcludeAll());
		//options.setSchemaInclusionRule(new RegularExpressionInclusionRule("PUBLIC.BOOKS"));
		return options;
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
