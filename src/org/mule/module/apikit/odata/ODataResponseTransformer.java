/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;

public class ODataResponseTransformer {
	public static MuleEvent transform(MuleEvent event, ODataPayload payload, Format format) throws Exception {
		if (payload.getContent() != null) {
			event.getMessage().setOutboundProperty("Content-Type", "text/plain");
			event.getMessage().setPayload(payload.getContent());
		} else {
			String formatted = payload.getFormatter().format(format);
			event.getMessage().setPayload(formatted);
			if (format == Format.Json) {
				event.getMessage().setOutboundProperty("Content-Type", "application/json"); 
			} else {
				event.getMessage().setOutboundProperty("Content-Type", "application/xml");
			}
		}

		if (event.getMessage().getOutboundProperty("http.status") == null) {
			event.getMessage().setOutboundProperty("http.status", 200);
		}

		return event;
	}
	
}
