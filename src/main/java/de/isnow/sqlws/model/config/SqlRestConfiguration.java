package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SqlRestConfiguration {

    @JsonProperty("connectionConfig")
    ConnectionConfig connectionConfig;

    @JsonProperty("internalStoreConfig")
    ConnectionConfig internalStoreConfig;

    Map<String, Object> application;

    Map<String, Object> files;
 }
