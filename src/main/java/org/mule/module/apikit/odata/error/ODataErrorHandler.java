/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.error;

import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.odata.ODataFormatHandler;
import org.mule.module.apikit.odata.exception.ODataBadRequestException;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.exception.ODataInternalServerErrorException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.util.CoreEventUtils;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.util.ExceptionUtils;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;
import java.util.List;

public class ODataErrorHandler {

  private static final String ATOM_ERROR_ENVELOPE =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?><m:error xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"><m:code /><m:message xml:lang=\"en-US\">%%ERRORMSG%%</m:message></m:error>";
  private static final String JSON_ERROR_ENVELOPE =
      "{\"odata.error\":{\"code\":\"\",\"message\":{\"lang\":\"en-US\",\"value\":\"%%ERRORMSG%%\"}}}";
  private static final String ERROR_MSG_PLACEHOLDER = "%%ERRORMSG%%";

  public static CoreEvent handle(CoreEvent event, Exception ex) {
    return handle(event, ex, null);
  }

  public static CoreEvent handle(CoreEvent event, Exception ex, List<Format> formats) {
    Exception exceptionToBeThrown;
    Throwable cause = ExceptionUtils.getMessagingExceptionCause(ex);
    exceptionToBeThrown = cause != null ? (Exception) cause : ex;
    if (exceptionToBeThrown instanceof MuleException) {
      // Exception thrown by APIkit
      exceptionToBeThrown = processMuleException((MuleException) exceptionToBeThrown);
    }

    String payload = null;
    MediaType mediaType = null;
    if (isJsonFormat(formats, event)) {
      payload = JSON_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER,
          (exceptionToBeThrown.getMessage() != null)
              ? exceptionToBeThrown.getMessage().replace('"', '\'') : "");
      mediaType = MediaType.APPLICATION_JSON;
    } else {
      payload = ATOM_ERROR_ENVELOPE.replace(ERROR_MSG_PLACEHOLDER,
          (exceptionToBeThrown.getMessage() != null) ? exceptionToBeThrown.getMessage()
              : exceptionToBeThrown.getClass().getName());
      mediaType = MediaType.APPLICATION_XML;
    }
    Message message = Message.builder().value(payload).mediaType(mediaType).build();


    int httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode();
    if (exceptionToBeThrown instanceof ODataException) {
      httpStatus = ((ODataException) exceptionToBeThrown).getHttpStatus();
    }

    return CoreEvent.builder(event).message(message).addVariable("httpStatus", httpStatus).build();
  }

  /**
   * This method takes a MuleException thrown by APIKit and returns an odata exception
   * @param ex
   * @return
   */
  private static ODataException processMuleException(MuleException ex) {
    if (ex instanceof BadRequestException) {
      return new ODataBadRequestException(ex.getMessage(), ex);
    } else {
      return new ODataInternalServerErrorException(ex);
    }
  }

  private static boolean isJsonFormat(List<Format> formats, CoreEvent event) {
    try {
      if (formats == null) {
        formats = ODataFormatHandler.getFormats(CoreEventUtils.getHttpRequestAttributes(event));
      }
      return formats.contains(Format.Json) && !formats.contains(Format.Atom);
    } catch (ODataException e) {
      // do nothing, resort to default atom value...
    }
    return false;
  }

}
