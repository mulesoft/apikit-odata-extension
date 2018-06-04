/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import java.util.List;

import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataMethodNotAllowedException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.formatter.ODataPayloadMetadataFormatter;
import org.mule.module.apikit.spi.EventProcessor;
import org.mule.runtime.core.api.event.CoreEvent;

public class ODataMetadataProcessor extends ODataRequestProcessor {
	public ODataMetadataProcessor(OdataContext odataContext) {
		super(odataContext);
	}

	public ODataPayload process(CoreEvent event, EventProcessor eventProcessor, List<Format> formats) throws Exception
	{
		if("GET".equalsIgnoreCase(super.oDataContext.getMethod())){
			ODataPayload oDataPayload = new ODataPayload();
			oDataPayload.setFormatter(new ODataPayloadMetadataFormatter(getMetadataManager()));
			return oDataPayload;
		} else{
			throw new ODataMethodNotAllowedException("GET");
		}

	}
}
