/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.mule.module.apikit.model.exception.EntityModelParsingException;
import org.mule.module.apikit.model.exception.InvalidModelException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EntityModelParser {

	static final String typesPattern = "^types:\\s*$";
	static final Pattern elementPattern = Pattern.compile("^\\s{2}(\\w+):\\s*$");
	static final Pattern remotePattern = Pattern.compile("^\\s{4}\\(odata.remote\\):\\s*(\\w+)\\s*$");
	static final String propertiesPattern = "^\\s{4}properties:\\s*$";
	static final Pattern fieldPattern = Pattern.compile("^\\s{6}(\\w+):\\s*$");
	static final Pattern typePropertyPattern = Pattern.compile("^\\s{8}type:\\s*(\\w+)\\s*$");
	static final Pattern keyPropertyPattern = Pattern.compile("^\\s{8}\\(odata.key\\):\\s*(\\w+)\\s*$");
	static final Pattern nullablePropertyPattern = Pattern.compile("^\\s{8}\\(odata.nullable\\):\\s*(\\w+)\\s*$");

	/**
	 * Parses the entities out of the RAML file and looks for required fields
	 * @param pathToModel
	 * @return
	 * @throws IOException
	 * @throws EntityModelParsingException
	 */
	public static List<Entity> getEntities(String pathToModel) throws IOException, EntityModelParsingException {

		try {
			AMFWrapper amfWrapper =  new AMFWrapper(pathToModel);
			
			return getEntities(amfWrapper.getSchemas());
		} catch (Exception e) {
			throw new EntityModelParsingException(e.getMessage());
		}
	}

	private static List<Entity> getEntities(EntityDefinitionSet entityDefinitionSet) throws InvalidModelException {
		List<Entity> entities = new ArrayList<Entity>();
		boolean typesFound = false;

		Entity entity;
		Property property;
		for (EntityDefinition entityDefinition : entityDefinitionSet.toList()) {
			typesFound = true;
			entity = new Entity(entityDefinition.getName());
			entity.setRemote(entityDefinition.getRemoteEntity());
			entity.setPropertiesFound(true);
			for(EntityDefinitionProperty entityDefinitionProperty : entityDefinition.getProperties()){
				property = new Property(entityDefinitionProperty.getName());
				property.setKey(String.valueOf(entityDefinitionProperty.isKey()));
				property.setNullable(String.valueOf(entityDefinitionProperty.isNullable()));
				property.setType(entityDefinitionProperty.getType());
				entity.addProperty(property);
			}
			entities.add(entity);
		}

		if (!typesFound) {
			throw new InvalidModelException("no types definition where found, please check the model");
		}

		return entities;
	}

	public static String getSchemaTypeFromEdmType(String edmType) {
		String schemaType = "string";
		switch (edmType) {
			case "Edm.Boolean":
				schemaType = "boolean";
				break;
			case "Edm.Decimal":
			case "Edm.Double":
			case "Edm.Single":
				schemaType = "number";
				break;
			case "Edm.Int16":
			case "Edm.Int32":
			case "Edm.Int64":
			case "Edm.SByte":
				schemaType = "integer";
				break;
			case "Edm.Guid":
			case "Edm.Binary":
			case "Edm.DateTime":
			case "Edm.String":
			case "Edm.Time":
			case "Edm.DateTimeOffset":
				schemaType = "string";
				break;
		}
		return schemaType;
	}

}