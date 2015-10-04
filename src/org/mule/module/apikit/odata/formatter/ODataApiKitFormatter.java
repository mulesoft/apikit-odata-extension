/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mule.module.apikit.odata.metadata.GatewayMetadataManager;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.model.Entry;
import org.mule.module.apikit.odata.util.Helper;
import org.mule.module.apikit.odata.util.UriInfoImpl;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.EntitiesResponse;

public class ODataApiKitFormatter implements ODataPayloadFormatter
{
	private List<Entry> entries;
	private String entityName;
	private String url;
	private GatewayMetadataManager gatewayMetadataManager;

	public ODataApiKitFormatter(GatewayMetadataManager gatewayMetadataManager, List<Entry> entities, String entityName, String url)
	{
		this.gatewayMetadataManager = gatewayMetadataManager;
		this.entries = entities;
		this.entityName = entityName;
		this.url = url;
	}

	public String format(Format format) throws Exception
	{
		if (Format.Default.equals(format)) {
			format = Format.Atom;
		}
		FormatWriter<EntitiesResponse> fw = FormatWriterFactory.getFormatWriter(EntitiesResponse.class, Arrays.asList(MediaType.valueOf(MediaType.WILDCARD)), format.name(), null);

		EntityDefinitionSet entitySet = gatewayMetadataManager.getEntitySet();
		EntitiesResponse entitiesResponse = Helper.convertEntriesToOEntries(entries, entityName, entitySet);

		StringWriter sw = new StringWriter();
		UriInfo uriInfo = new UriInfoImpl(url);
		fw.write(uriInfo, sw, entitiesResponse);
		return sw.toString();
	}
}