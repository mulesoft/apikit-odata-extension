/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.event.CoreEvent;

import static org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount.ALL_PAGES;
import static org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount.NONE;

public class ODataPayload<T> {
	private T value;
	private ODataPayloadFormatter formatter;
	private int status=200;
	private CoreEvent muleEvent;

	public ODataPayload(CoreEvent event) {
		this.muleEvent = event;
	}

	public ODataPayload(CoreEvent event, T value, int status) {
		this(event);
		this.value = value;
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public CoreEvent getMuleEvent() {
		return muleEvent;
	}

	public void setMuleEvent(CoreEvent muleEvent) {
		this.muleEvent = muleEvent;
	}

	public ODataPayloadFormatter getFormatter() {
		return formatter;
	}

	public void setFormatter(ODataPayloadFormatter oDataPayloadFormatter) {
		this.formatter = oDataPayloadFormatter;
	}

	public Integer getInlineCount() {
		final InlineCount inlineCountParam = getInlineCountParam(muleEvent);

		if (inlineCountParam == ALL_PAGES) {
			final TypedValue inlineCount = muleEvent.getVariables().get("inlineCount");
			if (inlineCount != null) {
				final Object inlineCountValue = inlineCount.getValue();

				if (inlineCountValue instanceof Integer)
					return (Integer) inlineCountValue;

				if (inlineCountValue instanceof String)
					return Integer.valueOf((String) inlineCountValue);

				throw new RuntimeException("Unsupported value for 'inlineCount' variable: '" + inlineCountValue + "'");
			}
		}

		return null;
	}

	private static InlineCount getInlineCountParam(CoreEvent event) {
		HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
		final MultiMap<String, String> queryParams = attributes.getQueryParams();

		final String inlineCountParameterValue = queryParams.get("inlinecount");

		if ("allpages".equalsIgnoreCase(inlineCountParameterValue)) return ALL_PAGES;

		return NONE;
	}
}
