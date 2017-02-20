package de.isnow.sqlws.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import lombok.SneakyThrows;

public class HSQLSetup implements DbSetup {

	
	@SuppressWarnings("unchecked")
	@SneakyThrows
	@Override
	public void beforeStart(URI url, Object payload) {
		Map<String, Object> dict = (Map<String, Object>)payload;
		String jarPath = (String)dict.get("jarpath");
		ClassLoader classLoader = (ClassLoader)dict.get("classloader");
		writeStreamToFile(classLoader, jarPath, "exampledb.data");
		writeStreamToFile(classLoader, jarPath, "exampledb.properties");
		writeStreamToFile(classLoader, jarPath, "exampledb.script");
		
	}
	

	@SneakyThrows
	private void writeStreamToFile(ClassLoader classLoader, String jarPath, String inResourceName) {
		InputStream in = classLoader.getResourceAsStream (jarPath+inResourceName);
		if (null != in) {
		    byte[] buffer = new byte[in.available()];
		    in.read(buffer);
			File outFile = new File (System.getProperty("java.io.tmpdir")+ File.separator +inResourceName);
			FileOutputStream ops = new FileOutputStream(outFile);
			ops.write(buffer);
			ops.close();
			in.close();
		}
	}

}
