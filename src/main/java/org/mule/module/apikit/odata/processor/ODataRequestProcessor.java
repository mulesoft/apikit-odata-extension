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

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.spi.EventProcessor;
import org.mule.runtime.core.api.event.CoreEvent;

public abstract class ODataRequestProcessor {

	protected OdataContext oDataContext;

	public ODataRequestProcessor(OdataContext oDataContext) {
		this.oDataContext = oDataContext;
	}

	public abstract ODataPayload process(CoreEvent event, EventProcessor eventProcessor, List<Format> formats) throws Exception;

	protected OdataMetadataManager getMetadataManager() {
		return oDataContext.getOdataMetadataManager();
	}

	protected String getProtocol(HttpRequestAttributes attributes) {
		String protocol = attributes.getScheme();

		// workaround to handle old http/https connector
		if (protocol == null) {
						String uri = attributes.getRequestUri();
			Matcher m = Pattern.compile("^(http|https|.+)://.*$").matcher(uri);
			if (m.matches()) {
				protocol = m.group(1);
			} else {
				protocol = "http";
			}
		}

		return protocol;
	}

	protected String getHost(HttpRequestAttributes attributes) {
		String host = attributes.getRemoteAddress();
		return host;
	}

	protected String getUrl(HttpRequestAttributes attributes) {
		String url = getProtocol(attributes) + "://" + getHost(attributes);
		return url;
	}

	protected String getCompleteUrl(HttpRequestAttributes attributes) {
		String path =attributes.getRequestPath();
		String url = getProtocol(attributes) + "://" + getHost(attributes) + path;
		return url;
	}
}
