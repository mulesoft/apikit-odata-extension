/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import com.google.common.collect.ImmutableSet;
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableSet.copyOf;
import static org.odata4j.edm.EdmSimpleType.BOOLEAN;
import static org.odata4j.edm.EdmSimpleType.DECIMAL;
import static org.odata4j.edm.EdmSimpleType.DOUBLE;
import static org.odata4j.edm.EdmSimpleType.INT16;
import static org.odata4j.edm.EdmSimpleType.INT32;
import static org.odata4j.edm.EdmSimpleType.INT64;
import static org.odata4j.edm.EdmSimpleType.STRING;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
public class BodyToJsonConverter {
	private static final String XML_PREAMBLE_REGEX = "(<\\?[^<]*\\?>)?";
	private static final String XMLNS_DECLARATION_REGEX = "xmlns.*?(\"|\').*?(\"|\')";
	private static final String NAMESPACE_DECLARATION_REGEX = "xmlns:(\\w+)=.*?(\"|\').*?(\"|\')";
	private static final Pattern NAMESPACE_DECLARATION_PATTERN = Pattern.compile(NAMESPACE_DECLARATION_REGEX);

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

	private static Set<String> getNamespaces(String xmlString) {
		Set<String> namespaces = new HashSet<>();
		Matcher m = NAMESPACE_DECLARATION_PATTERN.matcher(xmlString);
		while (m.find()) {
			namespaces.add(m.group(1));
		}
		return namespaces;
	}

	private static String removeNamespaceFromKey(String key, Set<String> namespaces) {
		for (final String namespace : namespaces) {
			if (key.startsWith(namespace + ":")) return key.replaceFirst("^(" + namespace + ":)(\\w+)$", "$2");
		}

		return key;
	}

	private static JSONObject removeNamespaces(JSONObject jsonObject, Set<String> namespaces) {
		final ImmutableSet<String> keys = copyOf(jsonObject.keySet());
		for (final String key : keys) {

			Object value = jsonObject.get(key);
			if (value instanceof JSONObject) {
				value = removeNamespaces((JSONObject) value, namespaces);
			}

			jsonObject.remove(key); // remove old key
			jsonObject.put(removeNamespaceFromKey(key, namespaces), value); // add new key without namespaces
		}

		return jsonObject;
	}

	private static JSONObject adaptBodyToJson(String body) throws ODataInvalidFormatException, OdataMetadataEntityNotFoundException, OdataMetadataFieldsException, OdataMetadataFormatException, OdataMetadataResourceNotFound {
		try {
			final JSONObject jsonObject = removeNamespaces(XML.toJSONObject(body), getNamespaces(body));
			final JSONObject entry = jsonObject.getJSONObject("entry");
			final JSONObject content = entry.getJSONObject("content");
			final JSONObject properties = content.getJSONObject("properties");

			final ImmutableSet<String> keys = copyOf(properties.keySet());
			for (final String key : keys) {
				final Object value = properties.get(key);
				if (value instanceof JSONObject) {
					properties.put(key, getContent((JSONObject) value));
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
	private static Object getContent(JSONObject object) throws ODataInvalidFormatException {
		final String key = "content";

		if (isNull(object)) return null;

		try {
			final EdmSimpleType type = getEdmType(object);
			if (BOOLEAN.equals(type)) return object.getBoolean(key);
			if (DECIMAL.equals(type)) return new BigDecimal(object.getString(key));
			if (DOUBLE.equals(type)) return object.getDouble(key);
			if (INT64.equals(type)) return object.getLong(key);
			if (INT16.equals(type) || INT32.equals(type)) return object.getInt(key);

			return object.getString(key);
		} catch (final JSONException e) {
			throw new ODataInvalidFormatException("Invalid format.");
		}
	}

	private static boolean isNull(JSONObject object) {
		try {
			return object.getBoolean("null");
		} catch (final JSONException e) {
			return false;
		}
	}

	private static EdmSimpleType getEdmType(JSONObject object) {
		try {
			return EDMTypeConverter.convert(object.getString("type"));
		} catch (final JSONException e) {
			return STRING;
		}
	}
}