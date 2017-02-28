/*
 * Copyright (c) 2003, Orientation in Objects GmbH, www.oio.de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of Orienation in Objects GmbH nor the names of its 
 *   contributors may be used to endorse or promote products derived from this 
 *   software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ${file_name} Created on ${date} by ${user}
 *  
 * ${todo}
 * 
 */

package de.isnow.sqlws.util;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author tbayer
 *
 */
public class XmlConfigHandler extends DefaultHandler {

	private StringBuffer text = new StringBuffer();
	private String elementName = "";
	private Map<String, String> valuePairs = new HashMap<>();

	/**
	 * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
		throws SAXException {		        
        text = new StringBuffer();
		elementName = qName;
	}
	

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {		
		for( int i = 0; i < length; i++) {
			text.append(ch[start + i]);
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
		if ( elementName.equals(qName)) {
			valuePairs.put( qName, text.toString());
		}
	}
	
	public Map<String, String> getValuePairs() {
		return valuePairs;
	}

}
