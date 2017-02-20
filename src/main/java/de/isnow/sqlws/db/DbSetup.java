package de.isnow.sqlws.db;

import java.net.URI;

public interface DbSetup {

	public void beforeStart(URI url, Object payload);
}
