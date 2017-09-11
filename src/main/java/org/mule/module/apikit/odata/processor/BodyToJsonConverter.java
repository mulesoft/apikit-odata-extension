/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.mule.module.apikit.odata.exception.ODataBadRequestException;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.util.EDMTypeConverter;
import org.odata4j.edm.EdmSimpleType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.odata4j.edm.EdmSimpleType.*;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
public class BodyToJsonConverter {
	public static String convertPayload(String entity, boolean isXMLFormat, String payloadAsString) throws ODataInvalidFormatException, ODataBadRequestException, OdataMetadataEntityNotFoundException, OdataMetadataFieldsException, OdataMetadataFormatException, OdataMetadataResourceNotFound {
		if (isXMLFormat){
			return adaptBodyToJson(payloadAsString).toString();
		} else {
			if(!isValidJson(payloadAsString)){
				throw new ODataInvalidFormatException("Invalid format.");
			}
			return payloadAsString;
		}
	}

	private static boolean isValidJson(String payload) {
		try{
			new JSONObject(payload);
			return true;
		} catch(JSONException ex){
			return false;
		}
	}

	public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
		for (String np : getNamespaces(xmlString)) {
			xmlString = removeNamespace(xmlString, np);
		}

		return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
				replaceAll("xmlns.*?(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */

	}

	private static Set<String> getNamespaces(String xmlString) {
		Set<String> namespaces = new HashSet<>();
		Matcher m = Pattern.compile("xmlns:(\\w+)=.*?(\"|\').*?(\"|\')").matcher(xmlString);
		while (m.find()) {
			namespaces.add(m.group(1));
		}
		return namespaces;
	}

	private static String removeNamespace(String xmlString, String namespace) {
		final String openingTagRegex = "(<[^<>]*)(" + namespace + ":)([^<>]+>)";
		final String closingTagRegex = "(</[^<>]*)(" + namespace + ":)([^<>]+>)";

		xmlString = xmlString.replaceAll(openingTagRegex, "$1$3") /* remove opening tag prefix */
				.replaceAll(closingTagRegex, "$1$3"); /* remove closing tags prefix */

		final Matcher m = Pattern.compile(openingTagRegex + "|" + closingTagRegex).matcher(xmlString);
		if (m.find()) {
			return removeNamespace(xmlString, namespace);
		} else {
			return xmlString;
		}
	}

	private static JSONObject adaptBodyToJson(String body) throws ODataInvalidFormatException, OdataMetadataEntityNotFoundException, OdataMetadataFieldsException, OdataMetadataFormatException, OdataMetadataResourceNotFound {
		try {
			JSONObject jsonObject = XML.toJSONObject(removeXmlStringNamespaceAndPreamble(body));
			JSONObject entry = jsonObject.getJSONObject("entry");
			JSONObject content = entry.getJSONObject("content");
			JSONObject properties = content.getJSONObject("properties");
			Iterator<String> keyIterator = properties.keys();

			while(keyIterator.hasNext()){
				String key = keyIterator.next();
				final Object value = properties.get(key);
				if (value instanceof JSONObject) {
					JSONObject object = properties.getJSONObject(key);
					final EdmSimpleType type = getEdmType(object);
					properties.put(key, getContent(object, type));
				} else {
					properties.put(key, value.toString());
				}
			}
			return properties;
		} catch (JSONException e) {
			throw new ODataInvalidFormatException("Invalid format.");
		}
	}

	@Nullable
	private static Object getContent(JSONObject object, EdmSimpleType type) throws ODataInvalidFormatException {
		final String key = "content";
		try {
			if (BOOLEAN.equals(type)) return object.getBoolean(key);
			if (DECIMAL.equals(type)) return new BigDecimal(object.getString(key));
			if (DOUBLE.equals(type)) return object.getDouble(key);
			if (INT64.equals(type)) return object.getLong(key);
			if (INT16.equals(type) || INT32.equals(type)) {
				return object.getInt(key);
			} else {
				return object.getString(key);
			}
		} catch (final JSONException e) {
			boolean isNull = object.getBoolean("null");
			if (isNull) return null;
		}

		// if object is not null and content is not present throw an error
		throw new ODataInvalidFormatException("Invalid format.");
	}

	private static EdmSimpleType getEdmType(JSONObject object) {
		try {
			return EDMTypeConverter.convert(object.getString("type"));
		} catch (final JSONException e) {
			return STRING;
		}
	}
}