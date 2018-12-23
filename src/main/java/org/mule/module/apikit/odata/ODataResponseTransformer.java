/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.api.MuleEvent;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount;

import java.util.List;
import java.util.Map;

import static org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount.ALL_PAGES;
import static org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount.NONE;

public class ODataResponseTransformer {
	public static MuleEvent transform(MuleEvent event, ODataPayload payload, List<Format> formats) throws Exception {
		if (payload.getContent() != null) {
			event.getMessage().setOutboundProperty("Content-Type", "text/plain");
			event.getMessage().setPayload(payload.getContent());
		} else {

			final boolean isJson = formats.contains(Format.Json) && !formats.contains(Format.Atom);

			final Integer entitiesCount = getEntitiesCount(event);

			String formatted = payload.getFormatter().format(isJson ? Format.Json : Format.Atom, entitiesCount);
			event.getMessage().setPayload(formatted);

			if (isJson) {
				event.getMessage().setOutboundProperty("Content-Type", "application/json");
			} else {
				if (payload.getFormatter().supportsAtom()) {
					event.getMessage().setOutboundProperty("Content-Type", "application/atom+xml");
				} else {
					event.getMessage().setOutboundProperty("Content-Type", "application/xml");
				}
			}
		}

		if (event.getMessage().getOutboundProperty("http.status") == null) {
			event.getMessage().setOutboundProperty("http.status", 200);
		}

		return event;
	}

	private static Integer getEntitiesCount(MuleEvent event) {
		Integer entitiesCount;
		final InlineCount inlineCount = getInlineCount(event);
		if (inlineCount == ALL_PAGES) {
			final Object inlineCountPopertyValue = event.getMessage().getOutboundProperty("odata.inlineCount");
			if (inlineCountPopertyValue instanceof String)
				entitiesCount = Integer.valueOf((String) inlineCountPopertyValue);
			else if (inlineCountPopertyValue instanceof Integer)
				entitiesCount = (Integer) inlineCountPopertyValue;
			else
				throw new RuntimeException("Unsupported value for 'odata.inlineCount' property: '" + inlineCountPopertyValue + "'");
		} else {
			entitiesCount = null;
		}
		return entitiesCount;
	}

	private static InlineCount getInlineCount(MuleEvent event) {
		final Map queryParamsMap = event.getMessage().getInboundProperty("http.query.params");
		final String inlineCountParameterValue = (String) queryParamsMap.get("inlinecount");

		if ("allpages".equalsIgnoreCase(inlineCountParameterValue)) return ALL_PAGES;

		return NONE;
	}

}
