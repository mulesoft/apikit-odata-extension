/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.formatter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.util.Helper;
import org.mule.module.apikit.odata.util.UriInfoImpl;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;

public class ServiceDocumentPayloadFormatter extends ODataPayloadFormatter {
	private final String url;
	private OdataMetadataManager odataMetadataManager;

	static { 
		System.setProperty("javax.ws.rs.ext.RuntimeDelegate","org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl"); // Workaround for issue while loading class javax.ws.rs.ext.RuntimeDelegate embedded in odata4j
	}																									  	  // https://stackoverflow.com/questions/30316829/classnotfoundexception-org-glassfish-jersey-internal-runtimedelegateimpl-cannot
	

	public ServiceDocumentPayloadFormatter(OdataMetadataManager odataMetadataManager, String url) {
		this.odataMetadataManager = odataMetadataManager;
		this.url = url;
		this.setSupportsAtom(false);
	}

	public String format(Format format) throws Exception {
		if (Format.Default.equals(format)) {
			format = Format.Atom;
		}
		FormatWriter<EdmDataServices> fw = FormatWriterFactory.getFormatWriter(EdmDataServices.class, Arrays.asList(MediaType.valueOf(MediaType.WILDCARD)),
				format.name(), null);
		EdmDataServices ees = Helper.createMetadata(odataMetadataManager.getEntitySet());
		UriInfo uriInfo = new UriInfoImpl(url);
		Writer w = new StringWriter();
		fw.write(uriInfo, w, ees);

		return w.toString();
	}
}