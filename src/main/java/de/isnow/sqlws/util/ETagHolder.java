package de.isnow.sqlws.util;


/**
 * @author JanderJo
 * @date 21.11.2014
 *
 */
public interface ETagHolder {

	public String getETag();
	
	public void cleareTag();
}
