/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.error;

import org.mule.api.MuleException;
import org.mule.module.apikit.exception.*;
import org.mule.module.apikit.odata.exception.*;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;

import java.util.List;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.ODataFormatHandler;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.util.ExceptionUtils;

public class ODataErrorHandler {

	private static final String ATOM_ERROR_ENVELOPE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><m:error xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"><m:code /><m:message xml:lang=\"en-US\">%%ERRORMSG%%</m:message></m:error>";
	private static final String JSON_ERROR_ENVELOPE = "{\"odata.error\":{\"code\":\"\",\"message\":{\"lang\":\"en-US\",\"value\":\"%%ERRORMSG%%\"}}}";
	private static final String ERROR_MSG_PLACEHOLDER = "%%ERRORMSG%%";

	public static MuleEvent handle(MuleEvent event, Exception ex) {
		return handle(event, ex, null);
	}

	public static MuleEvent handle(MuleEvent event, Exception ex, List<Format> formats) {
		Exception exceptionToBeThrown;
		Throwable cause = ExceptionUtils.getRootCause(ex);
		exceptionToBeThrown = cause != null ? (Exception)cause : ex;
		if (exceptionToBeThrown instanceof MuleException) {
			// Exception thrown by APIkit
			exceptionToBeThrown = processMuleException((MuleException) exceptionToBeThrown);
		}
		
		if (isJsonFormat(formats, event)) {
			event.getMessage().setOutboundProperty("Content-Type", "application/json");
			event.getMessage().setPayload(JSON_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER, (exceptionToBeThrown.getMessage() != null) ? exceptionToBeThrown.getMessage().replace('"', '\'') : ""));
		} else {
			event.getMessage().setOutboundProperty("Content-Type", "application/xml");
			event.getMessage().setPayload(ATOM_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER, (exceptionToBeThrown.getMessage() != null) ? exceptionToBeThrown.getMessage() : exceptionToBeThrown.getClass().getName()));
		}

		
		if (exceptionToBeThrown instanceof ODataException) {
			event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY, ((ODataException) exceptionToBeThrown).getHttpStatus());
		} else {
			event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY, HttpConstants.SC_INTERNAL_SERVER_ERROR);
		}

		return event;
	}

	/**
	 * This method takes a MuleException thrown by APIKit and returns an odata exception
	 * @param ex
	 * @return
	 */
	private static ODataException processMuleException(MuleException ex){
		if (ex instanceof MethodNotAllowedException) {
			return new ODataMethodNotAllowedException();
		} else if (ex instanceof BadRequestException) {
			return new ODataBadRequestException();
		} else if (ex instanceof NotAcceptableException) {
			return new ODataNotAcceptableException();
		} else if (ex instanceof NotFoundException) {
			return new ODataNotFoundException(ex.getMessage());
		} else if (ex instanceof UnsupportedMediaTypeException) {
			return new ODataUnsupportedMediaTypeException(ex.getMessage());
		} else {
			return new ODataInternalServerErrorException(ex);
		}
	}

	private static boolean isJsonFormat(List<Format> formats, MuleEvent event) {
		try {
			if (formats == null) {
				formats = ODataFormatHandler.getFormats(event);
			}
			return formats.contains(Format.Json) && !formats.contains(Format.Atom);
		} catch (ODataException e) {
			// do nothing, resort to default atom value...
		}
		return false;
	}

}