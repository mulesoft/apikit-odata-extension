/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
