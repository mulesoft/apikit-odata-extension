/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.List;

import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount;
import org.mule.module.apikit.odata.model.Entry;

import static org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.InlineCount.NONE;

public class ODataPayload {
	private String content;
	private List<Entry> entries;
	private InlineCount inlineCount = NONE;
	private ODataPayloadFormatter formatter;
	private int status=200;

	public int getStatus() {
		return status;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public ODataPayloadFormatter getFormatter() {
		return formatter;
	}

	public ODataPayload(String content) {
		this.content = content;
	}

	public ODataPayload(String content, int status) {
		this.content = content;
		this.status = status;
	}

	public ODataPayload(List<Entry> entries) {
		this.entries = entries;
	}

	public ODataPayload(List<Entry> entries, int status) {
		this.entries = entries;
		this.status = status;
	}

	public ODataPayload() {
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<Entry> getEntities() {
		return entries;
	}

	public void setEntities(List<Entry> entities) {
		this.entries = entities;
	}

	public void setFormatter(ODataPayloadFormatter oDataPayloadFormatter) {
		this.formatter = oDataPayloadFormatter;
	}

	public void setInlineCount(InlineCount inlineCount) {
		this.inlineCount = inlineCount;
	}

	public InlineCount getInlineCount() {
		return inlineCount;
	}

}
