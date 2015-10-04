/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.processor;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.AbstractRouter;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.formatter.ODataPayloadMetadataFormatter;
import org.mule.module.apikit.odata.metadata.GatewayMetadataManager;

public class ODataMetadataProcessor extends ODataRequestProcessor
{
	public ODataMetadataProcessor(GatewayMetadataManager metadataManager)
	{
		super(metadataManager);
	}

	public ODataPayload process(MuleEvent event, AbstractRouter router, Format format) throws Exception
	{
		ODataPayload oDataPayload = new ODataPayload();
		oDataPayload.setFormatter(new ODataPayloadMetadataFormatter(getMetadataManager()));
		return oDataPayload;
	}
}
