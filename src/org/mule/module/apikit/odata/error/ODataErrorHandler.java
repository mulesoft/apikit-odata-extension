/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.error;



import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.ODataFormatHandler;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;

public class ODataErrorHandler {

	private static final String ATOM_ERROR_ENVELOPE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><m:error xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"><m:code /><m:message xml:lang=\"en-US\">%%ERRORMSG%%</m:message></m:error>";
	private static final String JSON_ERROR_ENVELOPE = "{\"odata.error\":{\"code\":\"\",\"message\":{\"lang\":\"en-US\",\"value\":\"%%ERRORMSG%%\"}}}";
	private static final String ERROR_MSG_PLACEHOLDER = "%%ERRORMSG%%";
	
	public static MuleEvent handle(MuleEvent event, Exception ex) {
		return handle(event, ex, null);
	}
	
	public static MuleEvent handle(MuleEvent event, Exception ex, Format format) {
		if (isJsonFormat(format, event)) {
			event.getMessage().setOutboundProperty("Content-Type", "application/json");
			event.getMessage().setPayload(JSON_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER, ex.getMessage().replace('"', '\'')));		
		} else {
			event.getMessage().setOutboundProperty("Content-Type", "application/xml");
			event.getMessage().setPayload(ATOM_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER, ex.getMessage()));
		}
		
		if (ex instanceof ODataException) {
			event.getMessage().setOutboundProperty("http.status", ((ODataException) ex).getHttpStatus());
		} else {
			event.getMessage().setOutboundProperty("http.status", 500);
		}
		
		return event;
	}

	private static boolean isJsonFormat(Format format, MuleEvent event) {		
		try {
			if (format != null) {
				return Format.Json.equals(format);
			} else {
				format = ODataFormatHandler.getFormat(event);
				return Format.Json.equals(format);
			}
		} catch (ODataException e) {
			// do nothing, resort to default atom value...
		}	
		return false; 
	}
	
}