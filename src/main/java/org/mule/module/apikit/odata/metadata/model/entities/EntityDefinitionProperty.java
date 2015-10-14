/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.model.entities;

/**
 *
 * @author arielsegura
 */
public class EntityDefinitionProperty implements Comparable<EntityDefinitionProperty> {
	
	private String name;
	private String type;
	private boolean nullable;
	private boolean key;
	private String defaultValue;
	private String maxLength;
	private Boolean fixedLength;
	private String collation;
	private Boolean unicode;
	private String precision;
	private String scale;

	public EntityDefinitionProperty(String name, String type, boolean nullable, boolean key, String defaultValue, String maxLength, Boolean fixedLength, String collation, Boolean unicode, String precision, String scale) {
		this.name = name;
		this.type = type;
		this.nullable = nullable;
		this.key = key;
		this.defaultValue = defaultValue;
		this.maxLength = maxLength;
		this.fixedLength = fixedLength;
		this.collation = collation;
		this.unicode = unicode;
		this.precision = precision;
		this.scale = scale;
	}

	public String toString() {
		return toJsonString();
	}

	public String toJsonString() {
		StringBuilder ret = new StringBuilder("{");
		
		ret.append("\"name:\"" + this.name + "\",");
		ret.append("\"type:\"" + this.type + "\",");
		ret.append("\"nullable:\"" + this.nullable + "\",");
		ret.append("\"key:\"" + this.key + "\",");
		
		if (this.defaultValue != null) {
			ret.append("\"defaultValue:\"" + this.defaultValue + "\",");
		}
		if (this.maxLength != null) {
			ret.append("\"maxLength:\"" + this.maxLength + "\",");
		}
		if (this.fixedLength != null) {
			ret.append("\"fixedLength:\"" + this.fixedLength + "\",");
		}
		if (this.collation != null) {
			ret.append("\"collation:\"" + this.collation + "\",");
		}
		if (this.unicode != null) {
			ret.append("\"unicode:\"" + this.unicode + "\",");
		}
		if (this.precision != null) {
			ret.append("\"precision:\"" + this.precision + "\",");
		}
		if (this.scale != null) {
			ret.append("\"scale:\"" + this.scale + "\",");
		}

		ret.append("}");
		return ret.toString();
	}

	// getters
	public String getName() {
		return this.name;
	}
	public String getType() {
		return this.type;
	}
	public boolean isNullable() {
		return this.nullable;
	}
	public boolean isKey() {
		return this.key;
	}
	public String getDefaultValue() {
		return this.defaultValue;
	}
	public String getMaxLength() {
		return this.maxLength;
	}
	public Boolean isFixedLength() {
		return this.fixedLength;
	}
	public String getCollation() {
		return this.collation;
	}
	public Boolean isUnicode() {
		return this.unicode;
	}
	public String getPrecision() {
		return this.precision;
	}
	public String getScale() {
		return this.scale;
	}

	// setters
	public void setName(String value){
		this.name = value;
	}
	public void setType(String value){
		this.type = value;
	}
	public void setNullable(Boolean value){
		this.nullable = value;
	}
	public void setKey(Boolean value){
		this.key = value;
	}
	public void setDefaultValue(String value){
		this.defaultValue = value;
	}
	public void setMaxLength(String value){
		this.maxLength = value;
	}
	public void setFixedLength(Boolean value){
		this.fixedLength = value;
	}
	public void setCollation(String value){
		this.collation = value;
	}
	public void setUnicode(Boolean value){
		this.unicode = value;
	}
	public void setPrecision(String value){
		this.precision = value;
	}
	public void setScale(String value){
		this.scale = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (key ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
