/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.GatewayMetadataManager;
import org.mule.module.apikit.odata.util.Helper;
import org.mule.module.apikit.odata.util.UriInfoImpl;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;

public class ServiceDocumentPayloadFormatter implements ODataPayloadFormatter
{
	private final String url;
	private GatewayMetadataManager gatewayMetadataManager;

	public ServiceDocumentPayloadFormatter(GatewayMetadataManager gatewayMetadataManager, String url)
	{
		this.gatewayMetadataManager = gatewayMetadataManager;
		this.url = url;
	}

	public String format(Format format) throws Exception
	{
		if (Format.Default.equals(format)) {
			format = Format.Atom;
		}
		FormatWriter<EdmDataServices> fw = FormatWriterFactory.getFormatWriter(EdmDataServices.class, Arrays.asList(MediaType.valueOf(MediaType.WILDCARD)), format.name(), null);
		EdmDataServices ees = Helper.createMetadata(gatewayMetadataManager.getEntitySet());
		UriInfo uriInfo = new UriInfoImpl(url);
		Writer w = new StringWriter();
		fw.write(uriInfo, w, ees);

		return w.toString();
	}
}