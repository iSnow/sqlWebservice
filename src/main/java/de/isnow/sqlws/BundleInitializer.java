package de.isnow.sqlws;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public interface BundleInitializer {
	public void initialize (Bootstrap<SqlWsConfiguration> bootstrap);

	public void runConfig (SqlWsConfiguration configuration,
		Environment environment);
}
