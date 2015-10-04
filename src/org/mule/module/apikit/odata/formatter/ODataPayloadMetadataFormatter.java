/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.io.Writer;

import org.mule.module.apikit.odata.metadata.GatewayMetadataManager;
import org.mule.module.apikit.odata.util.Helper;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatWriter;

public class ODataPayloadMetadataFormatter implements ODataPayloadFormatter
{
	private GatewayMetadataManager gatewayMetadataManager;

	public ODataPayloadMetadataFormatter(GatewayMetadataManager gatewayMetadataManager)
	{
		this.gatewayMetadataManager = gatewayMetadataManager;
	}

	public String format(Format format) throws Exception
	{
		Writer w = new StringWriter();
		EdmDataServices ees = Helper.createMetadata(gatewayMetadataManager.getEntitySet());
		EdmxFormatWriter.write(ees, w);
		return w.toString();
	}
}