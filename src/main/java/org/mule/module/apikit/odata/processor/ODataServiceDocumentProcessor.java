/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import java.util.List;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataMethodNotAllowedException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.formatter.ServiceDocumentPayloadFormatter;
import org.mule.module.apikit.spi.EventProcessor;

public class ODataServiceDocumentProcessor extends ODataRequestProcessor {

	public ODataServiceDocumentProcessor(OdataContext odataContext) {
		super(odataContext);
	}

	public ODataPayload process(HttpRequestAttributes attributes, EventProcessor eventProcessor, List<Format> formats) throws Exception {
		if ("GET".equalsIgnoreCase(super.oDataContext.getMethod())) {
			String url = getCompleteUrl(attributes);

			ODataPayload result = new ODataPayload();
			result.setFormatter(new ServiceDocumentPayloadFormatter(getMetadataManager(), url));
			return result;
		} else {
			throw new ODataMethodNotAllowedException("GET");
		}
	}
}
