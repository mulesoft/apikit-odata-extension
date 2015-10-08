/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.net.URLDecoder;
import java.util.Arrays;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;
import org.mule.module.apikit.odata.exception.ODataInvalidUriException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;

public class ODataFormatHandler
{

	private static String[] xmlMimeTypes = {"application/atom+xml", "application/atomsvc+xml", "application/xml"};
	private static String[] jsonMimeTypes = {"application/json"};
	private static String[] plainMimeTypes = {"text/plain"};
	private static String[] defaultMimeTypes = {"*/*"};
	private static String[] xmlFormatTypes = {"atom", "xml"};
	private static String[] jsonFormatTypes = {"json"};

	/**
	 * Defines the right format based on the querystring and the accept header
	 * If the $format query option is present in a request URI it takes precedence 
	 * over the value(s) specified in the Accept request header.
	 * 
	 * @param event
	 * @return
	 * @throws ODataInvalidUriException
	 * @throws ODataInvalidFormatException 
	 */
	public static Format getFormat(MuleEvent event) throws ODataException {
		String acceptHeader = getAcceptHeader(event);
		String formatQueryParam = getFormatQueryParam(event);
		
		if (formatQueryParam != null) {
			if (Arrays.asList(xmlFormatTypes).contains(formatQueryParam)) {
				return Format.Atom;
			} else if (Arrays.asList(jsonFormatTypes).contains(formatQueryParam)) {
				return Format.Json;
			} else {
				throw new ODataInvalidFormatException("Unsupported media type requested.");
			}
		}
		
		if (acceptHeader != null) {
			if (containsAnyOf(acceptHeader, xmlMimeTypes)) {
				return Format.Atom;
			} else if (containsAnyOf(acceptHeader, jsonMimeTypes)) {
				return Format.Json;
			} else if (containsAnyOf(acceptHeader, plainMimeTypes)) { 
				return Format.Plain;
			} else if (containsAnyOf(acceptHeader, defaultMimeTypes)) { 
				return Format.Default;	
			} else {
				throw new ODataInvalidFormatException("Unsupported media type requested.");
			}
		}
		
		// default response if none of the above is specified
		return Format.Default;	
	}
	
	private static boolean containsAnyOf(String value, String[] array) {
		for (String key : array) {
			if (value.contains(key)) {
				return true;
			}			
		}
		return false;
	}
	
	private static String getAcceptHeader(MuleEvent event) {
		String acceptHeader = event.getMessage().getInboundProperty("accept");
		if(acceptHeader == null){
			acceptHeader = defaultMimeTypes[0];
		}
		return acceptHeader.toLowerCase();
	}
	
	/**
	 * Looks for the $format param in the query string and returns its value if present
	 * 
	 * @param event
	 * @return
	 */
	private static String getFormatQueryParam(MuleEvent event) {
		String formatQueryParam = null;
		String queryString = URLDecoder.decode((String) event.getMessage().getInboundProperty("http.query.string"));
		if (queryString != null && queryString.contains("$format=")) {
			String[] query = queryString.split("&");
			for (String pair : query) {
				if (pair.contains("$format=")) {
					return pair.replace("$format=", "").toLowerCase();
				}
			}
		}
		return formatQueryParam;
	}
}