package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hibernate.internal.SessionImpl;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqlRestConfigurationConnection {

    @Getter
    private static SqlRestConfigurationConnection instance;

    @Getter
    private Connection nativeConnection;

    @SneakyThrows
    public SqlRestConfigurationConnection(@NotNull ConnectionConfig connectionConfig) {
        nativeConnection = createConnection(
                connectionConfig.getDatabaseUrl(),
                connectionConfig.getUser(),
                connectionConfig.getPassword());
        instance = this;
    }
    @SneakyThrows
    private static Connection createConnection(
            @NotNull String databaseUrl,
            @NotNull String user,
            @NotNull String password) {
        return DriverManager.getConnection (databaseUrl, user, password);
    }
}
