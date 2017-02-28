package de.isnow.sqlws.util;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XmlConfigUtil {

	public static Map<String, String> readConfig(File conf)  {
		if (!conf.exists()) {
			throw new RuntimeException("Configuration file /WEB-INF/sqlrestconf.xml is missing!");
		}

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			XmlConfigHandler handler = new XmlConfigHandler();
			parser.parse(conf, handler);
			Map<String, String> valuePairs = handler.getValuePairs();
			return valuePairs;
		} catch (Exception e) {
			System.err.println("Error initializing sqlwebservice:" + e);
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, String> readConfig(InputStream conf)  {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			XmlConfigHandler handler = new XmlConfigHandler();
			parser.parse(conf, handler);
			Map<String, String> valuePairs = handler.getValuePairs();
			return valuePairs;
		} catch (Exception e) {
			System.err.println("Error initializing sqlwebservice:" + e);
			throw new RuntimeException(e);
		}
	}
}
