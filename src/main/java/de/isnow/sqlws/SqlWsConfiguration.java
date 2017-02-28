package de.isnow.sqlws;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class SqlWsConfiguration extends Configuration {

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

	public SqlWsConfiguration() {
		System.out.println("in SqlWsConfiguration c'tor");
		//swaggerBundleConfiguration = new SwaggerBundleConfiguration();
	}
	
}
