/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.util.ArrayList;
import java.util.List;

import org.atteo.evo.inflector.English;
import org.mule.module.apikit.model.exception.InvalidModelException;

public class Entity {

	private String name;
	private String remote;
	private boolean hasProperties;
	private List<Property> properties = new ArrayList<Property>();
		
	public Entity (String name) throws InvalidModelException {
		setName(name);
	}
	
	private void setName(String name) throws InvalidModelException {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getElementName() {
		return name;
	}
	
	public String getCollectionName() {
		return English.plural(name);
	}
	
	public String getIdElementName() {
		return (name.toLowerCase().endsWith("id") ? name + "_id" : name + "Id");
	}

	public void setPropertiesFound(boolean value) {
		this.hasProperties = value;
	}
	
	public void addProperty(Property property) {
		this.properties.add(property);
	}
	
	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) throws InvalidModelException {
		this.remote = remote;
	}
	
	public boolean isValid() throws InvalidModelException {
		if (!isValid(name)) throw new InvalidModelException("there are entities with empty names, please fix the model");	
		if (!hasProperties) throw new InvalidModelException("the entity named '" + this.name + "' has no properties defined");
		if (!isValid(remote)) throw new InvalidModelException("the entity named '" + this.name + "' is missing the remote field definition");
		
		return true;
	}
	
	private boolean isValid(String value) {
		if (value==null || value.isEmpty()) return false;
		return true;
	}
}
