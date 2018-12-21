/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.model.Entry;
import org.mule.module.apikit.odata.util.Helper;
import org.mule.module.apikit.odata.util.UriInfoImpl;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.EntitiesResponse;

public class ODataApiKitFormatter extends ODataPayloadFormatter {
	private List<Entry> entries;
	private String entityName;
	private String url;
	private OdataMetadataManager odataMetadataManager;

	public ODataApiKitFormatter(OdataMetadataManager odataMetadataManager, List<Entry> entities, String entityName, String url) {
		this.odataMetadataManager = odataMetadataManager;
		this.entries = entities;
		this.entityName = entityName;
		this.url = url;
		this.setSupportsAtom(true);
	}

	public String format(Format format, InlineCount inlineCount) throws Exception {
		if (Format.Default.equals(format)) {
			format = Format.Atom;
		}
		FormatWriter<EntitiesResponse> fw = FormatWriterFactory.getFormatWriter(EntitiesResponse.class, Arrays.asList(MediaType.valueOf(MediaType.WILDCARD)),
				format.name(), null);

		EntityDefinitionSet entitySet = odataMetadataManager.getEntitySet();
		EntitiesResponse entitiesResponse = Helper.convertEntriesToOEntries(entries, entityName, entitySet, inlineCount);

		StringWriter sw = new StringWriter();
		UriInfo uriInfo = new UriInfoImpl(url);
		fw.write(uriInfo, sw, entitiesResponse);
		return sw.toString();
	}
}