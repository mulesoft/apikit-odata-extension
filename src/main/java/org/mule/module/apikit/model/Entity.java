package org.mule.module.apikit.model;

import org.atteo.evo.inflector.English;

public class Entity {

	private String name;
		
	public Entity (String name) {
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

}
