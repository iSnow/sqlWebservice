package de.isnow.sqlws.util;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.oio.sqlrest.RESTRequestHandler;

public class XmlConfigUtil {

	public static Map<String, String> readConfig(File path)  {
		File conf = new File(path, "/WEB-INF/sqlrestconf.xml");
		if (!conf.exists()) {
			throw new RuntimeException("Configuration file /WEB-INF/sqlrestconf.xml is missing!");
		}

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			RESTRequestHandler handler = new RESTRequestHandler();
			parser.parse(conf, handler);
			Map<String, String> valuePairs = handler.getValuePairs();
			return valuePairs;
		} catch (Exception e) {
			System.err.println("Error initializing sqlwebservice:" + e);
			throw new RuntimeException(e);
		}
	}
}
