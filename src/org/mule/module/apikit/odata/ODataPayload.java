/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.List;

import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter;
import org.mule.module.apikit.odata.model.Entry;

public class ODataPayload
{
	private String content;
	List<Entry> entries;
	private ODataPayloadFormatter formatter;

	public ODataPayloadFormatter getFormatter()
	{
		return formatter;
	}

	public ODataPayload(String content)
	{
		this.content = content;
	}

	public ODataPayload(List<Entry> entries)
	{
		this.entries = entries;
	}

	public ODataPayload()
	{
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public List<Entry> getEntities()
	{
		return entries;
	}

	public void setEntities(List<Entry> entities)
	{
		this.entries = entities;
	}

	public void setFormatter(ODataPayloadFormatter oDataPayloadFormatter)
	{
		this.formatter = oDataPayloadFormatter;
	}

}
