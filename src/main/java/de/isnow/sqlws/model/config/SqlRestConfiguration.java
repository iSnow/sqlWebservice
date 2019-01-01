package de.isnow.sqlws.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SqlRestConfiguration {

    @JsonProperty("connectionConfig")
    ConnectionConfig connectionConfig;

    @JsonProperty("internalStoreConfig")
    ConnectionConfig internalStoreConfig;
}
