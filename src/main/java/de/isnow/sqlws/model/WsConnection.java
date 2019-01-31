package de.isnow.sqlws.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.isnow.sqlws.db.WsPersistenceUnitInfo;
import de.isnow.sqlws.resources.ConnectionsModelService;
import lombok.SneakyThrows;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLink.Style;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import de.isnow.sqlws.resources.DatabaseModelService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jpa.HibernatePersistenceProvider;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.utility.SchemaCrawlerUtility;


/**
 * @author jjander
 *
 */
@Slf4j
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY, 
		getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@XmlRootElement
public class WsConnection extends WsObject {

	@Getter
	@JsonIgnore
	private WsCatalog catalog = null;

	@Getter
	String currentSchema;

	@Getter
	private String databaseUri;

	@Getter
	@JsonIgnore
	private Connection nativeConnection;

	@Getter
	@JsonIgnore
	private Set<WsSchema> schemas;

	@SneakyThrows
	public WsConnection(
			@NotNull String databaseUrlIn, 
			@NotNull String userIn, 
			@NotNull String passwordIn,
					 String name,
					 SchemaCrawlerOptions options) {
		this.name = name;
		this.fullName = name;
		this.databaseUri = databaseUrlIn;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", databaseUrlIn);
		props.put("javax.persistence.jdbc.user", userIn);
		props.put("javax.persistence.jdbc.password", passwordIn);
		props.put("name", name);

		WsPersistenceUnitInfo persistenceUnitInfo = new WsPersistenceUnitInfo();
		persistenceUnitInfo.setProperties(props);

		HibernatePersistenceProvider hibernatePersistenceProvider = new HibernatePersistenceProvider();
		hibernatePersistenceProvider
				.createContainerEntityManagerFactory(persistenceUnitInfo, Collections.EMPTY_MAP);
		nativeConnection = createConnection(databaseUrlIn, userIn, passwordIn);
		setUp(options);
	}

	@SneakyThrows
	public WsConnection(
			@NotNull EntityManagerFactory entityManagerFactory,
			@NotNull String databaseUrlIn,
			String name,
			SchemaCrawlerOptions options) {
		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction t = em.getTransaction();
		t.begin();
		nativeConnection = em.unwrap(SessionImpl.class).connection();
		t.commit();
		em.close();
		this.name = name;
		this.fullName = name;
		this.databaseUri = databaseUrlIn;
		setUp(options);
	}

	@JsonIgnore
	public static Collection<WsConnection> getAll() {
		return registries
				.get(WsConnection.class)
				.getAll()
				.stream()
				.map(o -> ((WsConnection)o))
				.collect(Collectors.toSet());
	}

	@JsonIgnore
	public static WsConnection get(@NotNull String id) {
		return (WsConnection)registries
				.get(WsConnection.class)
				.get(id);
	}

	@SneakyThrows
	private static Connection createConnection(
			@NotNull String databaseUrl, 
			@NotNull String user, 
			@NotNull String password) {
		return DriverManager.getConnection (databaseUrl, user, password);
	}

/*
	@SneakyThrows
	private static Connection createConnection(
			@NotNull EntityManagerFactory entityManagerFactory) {
		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction t = em.getTransaction();
		t.begin();
		Connection connection = em.unwrap(SessionImpl.class).connection();
		t.commit();
		em.close();
		return connection;
	}
*/

	@SneakyThrows
	@JsonIgnore
	public DatabaseMetaData getMetaData() {
		return nativeConnection.getMetaData();
	}

	@SneakyThrows
	@JsonInclude
	public String getDatabaseProductName() {
		DatabaseMetaData meta = nativeConnection.getMetaData();
		return meta.getDatabaseProductName()+" "+meta.getDatabaseProductVersion();
	}

	@SneakyThrows
	@JsonInclude
	public Map<String, String> getSchemaNames() {
		Map<String, String> lSchemas = new LinkedHashMap<>();
		for (WsSchema schema : schemas) {
			lSchemas.put(schema.getId(), schema.getName());
		}
		return lSchemas;
	}



	@SneakyThrows
	private void setUp(SchemaCrawlerOptions options) {
		Registry<WsObject> wsObjectRegistry = initRegistry(this.getClass());
		wsObjectRegistry.register(this);
		if (null == options)
			catalog = new WsCatalog(SchemaCrawlerUtility.getCatalog(nativeConnection, SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()), this);
		else
			catalog = new WsCatalog(SchemaCrawlerUtility.getCatalog(nativeConnection, options), this);
		schemas = catalog.getSchemas();
		currentSchema = nativeConnection.getSchema();
	}

	@InjectLinks({
			@InjectLink(
					resource= ConnectionsModelService.class,
					method="getConnectionDetails",
					bindings ={
							@Binding(name = "cid", value = "${instance.id}")
					},
					rel = "self",
					title="current Connection",
					type = "GET",
					style =  Style.RELATIVE_PATH
			),
			@InjectLink(
					resource= DatabaseModelService.class,
					method="getDbs",
					bindings ={
							@Binding(name = "cid", value = "${instance.id}")
					},
					rel = "catalog",
					title = "Catalogs for this Connection",
					type = "GET",
					style =  Style.RELATIVE_PATH
			)
	})
	List<Link> links;




}
