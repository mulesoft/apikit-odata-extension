/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
