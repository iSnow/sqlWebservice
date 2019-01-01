package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConnectionConfig {

    @JsonProperty("database")
    String name;

    @JsonProperty("jdbc-driver-class")
    String jdbcDriverClass;

    @JsonProperty("database-url")
    String databaseUrl;

    @JsonProperty("user")
    String user;

    @JsonProperty("password")
    String password;
}
