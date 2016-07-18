/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;
import org.mule.module.apikit.odata.exception.ODataInvalidUriException;
import org.mule.module.apikit.odata.exception.ODataUnsupportedMediaTypeException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;

public class ODataFormatHandler {

	private static String[] xmlMimeTypes = { "application/atom+xml", "application/atomsvc+xml", "application/xml" };
	private static String[] jsonMimeTypes = { "application/json" };
	private static String[] plainMimeTypes = { "text/plain" };
	private static String[] defaultMimeTypes = { "*/*" };
	private static String[] xmlFormatTypes = { "atom", "xml" };
	private static String[] jsonFormatTypes = { "json" };

	/**
	 * Defines the right format based on the querystring and the accept header If
	 * the $format query option is present in a request URI it takes precedence
	 * over the value(s) specified in the Accept request header.
	 * 
	 * @param event
	 * @return
	 * @throws ODataInvalidUriException
	 * @throws ODataInvalidFormatException
	 */
	public static List<Format> getFormats(MuleEvent event) throws ODataException {
		
		List<Format> formats = new ArrayList<Format>();
		
		String acceptHeader = getAcceptHeader(event);
		String formatQueryParam = getFormatQueryParam(event);

		if (formatQueryParam != null) {
			if (Arrays.asList(xmlFormatTypes).contains(formatQueryParam)) {
				formats.add(Format.Atom);
				return formats;
			} else if (Arrays.asList(jsonFormatTypes).contains(formatQueryParam)) {
				formats.add(Format.Json);
				return formats;
			} else {
				throw new ODataUnsupportedMediaTypeException("Unsupported media type requested.");
			}
		}

		if (acceptHeader != null) {
			
			boolean unsupportedMediaType = true;
			
			if (containsAnyOf(acceptHeader, xmlMimeTypes)) {
				formats.add(Format.Atom);
				unsupportedMediaType = false;
			}
			
			if (containsAnyOf(acceptHeader, jsonMimeTypes)) {
				formats.add(Format.Json);
				unsupportedMediaType = false;
			}
			
			if (containsAnyOf(acceptHeader, plainMimeTypes)) {
				formats.add(Format.Plain);
				unsupportedMediaType = false;
			}
			
			if (containsAnyOf(acceptHeader, defaultMimeTypes)) {
				formats.add(Format.Default);
				unsupportedMediaType = false;
			}
			
			if (unsupportedMediaType) {
				throw new ODataUnsupportedMediaTypeException("Unsupported media type requested.");
			}
			
			return formats;
		}
		
		// if none specified, return default format
		formats.add(Format.Default);
		return formats;
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
		if (acceptHeader == null) {
			acceptHeader = defaultMimeTypes[0];
		}
		return acceptHeader.toLowerCase();
	}

	/**
	 * Looks for the $format param in the query string and returns its value if
	 * present
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