/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import org.json.JSONObject;
import org.json.XML;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
public class XmlBodyToJSonConverter {
	public static String convertXMLPayloadIfRequired(boolean bodyIsInXML, String payloadAsString) throws ODataInvalidFormatException {
		if (bodyIsInXML)
			payloadAsString = adaptBodyToJson(payloadAsString);

		if (!isValidJSON(payloadAsString))
			throw createInvalidFormatException();
		else
			return payloadAsString;
	}

	private static ODataInvalidFormatException createInvalidFormatException() {
		return new ODataInvalidFormatException("Invalid payload body format");
	}

	private static boolean isValidJSON(String payloadAsString) {
		try {
			new JSONObject(payloadAsString);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
		return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
		replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
		.replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
		.replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
	}

	private static String adaptBodyToJson(String body) throws ODataInvalidFormatException {
		try {
			JSONObject jsonObject = XML.toJSONObject(removeXmlStringNamespaceAndPreamble(body));
			JSONObject entry = (JSONObject) jsonObject.get("entry");
			JSONObject content = (JSONObject) entry.get("content");
			JSONObject properties = (JSONObject) content.get("properties");
			String result = properties.toString();
			return result;
		} catch (Exception e) {
			throw createInvalidFormatException();
		}
	}
}
