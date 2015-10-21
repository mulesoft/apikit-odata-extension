/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.AbstractRouter;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;

public abstract class ODataRequestProcessor {

	protected OdataContext oDataContext;

	public ODataRequestProcessor(OdataContext oDataContext) {
		this.oDataContext = oDataContext;
	}

	public abstract ODataPayload process(MuleEvent event, AbstractRouter router, List<Format> formats) throws Exception;

	protected OdataMetadataManager getMetadataManager() {
		return oDataContext.getOdataMetadataManager();
	}

	protected String getProtocol(MuleEvent event) {

		String protocol = event.getMessage().getInboundProperty("http.scheme");

		// workaround to handle old http/https connector
		if (protocol == null) {
			String uri = event.getMessage().getInboundProperty("http.context.uri");
			Matcher m = Pattern.compile("^(http|https|.+)://.*$").matcher(uri);
			if (m.matches()) {
				protocol = m.group(1);
			} else {
				protocol = "http";
			}
		}

		return protocol;
	}

	protected String getHost(MuleEvent event) {
		String host = event.getMessage().getInboundProperty("host");
		return host;
	}

	protected String getUrl(MuleEvent event) {
		String url = getProtocol(event) + "://" + getHost(event);
		return url;
	}

	protected String getCompleteUrl(MuleEvent event) {
		String path = event.getMessage().getInboundProperty("http.request.path");
		String url = getProtocol(event) + "://" + getHost(event) + path;
		return url;
	}
}
