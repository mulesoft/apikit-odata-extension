/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.metadata.model.entities;

/**
 * 
 * @author arielsegura
 */
public class EntityDefinitionProperty implements Comparable<EntityDefinitionProperty> {

	private String name;
	private String fieldName;
	private String sample;
	private String type;
	private boolean nullable;
	private int length;
	private boolean key;
	
	

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public EntityDefinitionProperty(String name, String fieldName, String sample, String type, boolean nullable, int length, boolean key) {
		this.name = (name != null ? name : "");
		this.fieldName = fieldName;
		this.sample = (sample != null ? sample : "");
		this.type = (type != null ? type : "");
		this.nullable = nullable;
		this.length = length;
		this.key = key;
	}

	public String toString() {
		return toJsonString();
	}

	public String toJsonString() {
		StringBuilder ret = new StringBuilder("{");
		ret.append("\"name\":\"" + this.name + "\",");
		ret.append("\"sample\":\"" + this.sample + "\",");
		ret.append("\"type\":\"" + this.type + "\",");
		ret.append("\"length\":\"" + this.length + "\",");
		ret.append("\"fieldName\":\"" + this.fieldName + "\",");
		ret.append("\"key\":\"" + this.key + "\",");
		ret.append("\"nullable\":\"" + this.nullable + "\"");
		ret.append("}");
		return ret.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public String getSample() {
		return sample;
	}

	public void setSample(String sample) {
		this.sample = sample;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (key ? 1231 : 1237);
		result = prime * result + length;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (nullable ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityDefinitionProperty other = (EntityDefinitionProperty) obj;
		if (key != other.key)
			return false;
		if (length != other.length)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nullable != other.nullable)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(EntityDefinitionProperty o) {
		return this.name.compareToIgnoreCase(o.name);
	}

}
