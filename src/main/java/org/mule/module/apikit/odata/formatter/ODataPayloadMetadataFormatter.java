/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.io.Writer;

import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.util.Helper;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.EdmxFormatWriter;

public class ODataPayloadMetadataFormatter implements ODataPayloadFormatter {
	private OdataMetadataManager odataMetadataManager;

	public ODataPayloadMetadataFormatter(OdataMetadataManager odataMetadataManager) {
		this.odataMetadataManager = odataMetadataManager;
	}

	public String format(Format format) throws Exception {
		Writer w = new StringWriter();
		EdmDataServices ees = Helper.createMetadata(odataMetadataManager.getEntitySet());
		EdmxFormatWriter.write(ees, w);
		return w.toString();
	}
}