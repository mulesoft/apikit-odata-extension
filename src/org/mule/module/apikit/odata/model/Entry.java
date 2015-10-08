/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.model;

import java.util.HashMap;
import java.util.Map;

public class Entry
{
	private Map<String, Object> properties = new HashMap<String, Object>();

	public void addProperty(String name, Object value)
	{
		getProperties().put(name, value);
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, Object> properties)
	{
		this.properties = properties;
	}

	public Object getProperty(String name)
	{
		return properties.get(name);
	}
	
}
