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
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;

import java.util.Iterator;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
public class BodyToJsonConverter {
	public static String convertPayload(String entity, boolean isXMLFormat, String payloadAsString) throws ODataInvalidFormatException, ODataBadRequestException, OdataMetadataEntityNotFoundException, OdataMetadataFieldsException, OdataMetadataFormatException, OdataMetadataResourceNotFound {
		if (isXMLFormat){
			return adaptBodyToJson(entity, payloadAsString).toString();
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
		return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
		replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
		.replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
		.replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
	}

	private static JSONObject adaptBodyToJson(String entity, String body) throws ODataInvalidFormatException, OdataMetadataEntityNotFoundException, OdataMetadataFieldsException, OdataMetadataFormatException, OdataMetadataResourceNotFound {
		try {
			JSONObject jsonObject = XML.toJSONObject(removeXmlStringNamespaceAndPreamble(body));
			JSONObject entry = jsonObject.getJSONObject("entry");
			JSONObject content = entry.getJSONObject("content");
			JSONObject properties = content.getJSONObject("properties");
			Iterator<String> keyIterator = properties.keys();

			OdataMetadataManager odataMetadataManager = new OdataMetadataManager();

			while(keyIterator.hasNext()){
				String key = keyIterator.next();
				try {
					JSONObject object = properties.getJSONObject(key);
					properties.put(key, object.get("content"));
				} catch (JSONException ex){
					// not a json object? it's ok
				}

				// now check against the property's type
				if(entity != null){
					EntityDefinitionProperty propertyDefinition = odataMetadataManager.getEntityByName(entity).findPropertyDefinition(key);
					Object value = properties.get(key);
					if(propertyDefinition.getType().contains("String") && !value.toString().startsWith("\"")){
						properties.put(key, "\""+value.toString()+"\"");
					}
				}
			}
			return properties;
		} catch (JSONException e) {
			throw new ODataInvalidFormatException("Invalid format.");
		}
	}
}
