/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

/**
 * 
 * @author arielsegura
 */
public class RamlParser {

	private static final String FIELD_NAME_PROPERTY_TEXT = "fieldName";
	private static final String SAMPLE_PROPERTY_TEXT = "sample";
	private static final String TYPE_PROPERTY_TEXT = "type";
	private static final String NULLABLE_PROPERTY_TEXT = "nullable";
	private static final String LENGTH_PROPERTY_TEXT = "maxLength";
	private static final String KEY_PROPERTY_TEXT = "key";
		

	public EntityDefinitionSet getEntitiesFromRaml(String path)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		RamlParserUtils.validateRaml(path);
		return getEntitiesFromRaml(new RamlDocumentBuilder().build(path));
	}

	public EntityDefinitionSet getEntitiesFromRaml(InputStream inputStream)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		RamlParserUtils.validateRaml(inputStream);
		return getEntitiesFromRaml(new RamlDocumentBuilder().build(inputStream));
	}

	public EntityDefinitionSet getEntitiesFromRaml(Raml raml)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		Logger.getLogger(getClass()).info("Loading entities from RAML...");
		Long initTime = System.nanoTime();
		Long part = System.nanoTime();

		EntityDefinitionSet entitySet = new EntityDefinitionSet();

		List<Map<String, String>> schemas = raml.getSchemas();
		
		if(schemas.isEmpty()){
			throw new GatewayMetadataFormatException(
					"No schemas found. ");
		}
		part = System.nanoTime();
		Logger.getLogger(getClass()).info("Parsing schemas reached in "+(part-initTime)/1000000);
		
		for (int i = 0; i < schemas.size(); i++) {
			Map<String, String> schema = schemas.get(i);
			if (schema.keySet().size() != 1) {
				throw new GatewayMetadataFormatException(
						"A schema must contain only one key and it has "+schema.keySet().size());
			}
			String entityName = (String) schema.keySet().toArray()[0];

			part = System.nanoTime();
			JSONObject jsonSchema = null;
			try {
				jsonSchema = new JSONObject(schema.get(entityName));
			} catch (JSONException ex) {
				throw new GatewayMetadataFormatException(ex.getMessage());
			}
			Logger.getLogger(getClass()).info("Schema to JSON in "+(System.nanoTime()-part)/1000000+" ms!");

			String remoteName = getStringFromJson(jsonSchema, "remoteName");
			checkFieldNotNull("Remote Name", remoteName);

			EntityDefinition entity = new EntityDefinition(entityName,
					remoteName);

			JSONObject properties = getJsonObjectFromJson(jsonSchema,
					"properties");
			if (properties == null) {
				throw new GatewayMetadataResourceNotFound(
						"Properties not found in entity " + entityName + ".");
			}
			part = System.nanoTime();
			entity.setProperties(parseEntityProperties(properties));
			//TODO See if it's possible to avoid this for
			for (EntityDefinitionProperty property : entity.getProperties()) {
				if (property.isKey()) {
					entity.setHasPrimaryKey(true);
					break;
				}
			}
			Logger.getLogger(getClass()).info("Properties parsed in "+(System.nanoTime()-part)/1000000+" ms!");
			entitySet.addEntity(entity);
		}
		Long end = System.nanoTime();
		Long total = (end - initTime)/1000000;
		Logger.getLogger(getClass()).info("Done in "+total+" ms!");

		return entitySet;
	}

	private String getStringFromJson(JSONObject json, String objectName)
			throws GatewayMetadataFieldsException {
		try {
			return json.getString(objectName);
		} catch (JSONException ex) {
			throw new GatewayMetadataFieldsException(ex.getMessage());
		}
	}

	private Object getFieldFromJson(JSONObject json, String objectName)
			throws GatewayMetadataFieldsException {
		try {
			return json.get(objectName);
		} catch (JSONException ex) {
			throw new GatewayMetadataFieldsException(ex.getMessage());
		}
	}
	
	private int getIntegerFromJson(JSONObject json, String objectName)
			throws GatewayMetadataFieldsException {
		try {
			return json.getInt(objectName);
		} catch (JSONException ex) {
			throw new GatewayMetadataFieldsException(ex.getMessage());
		}
	}
	
	private Boolean getBooleanFromJson(JSONObject json, String objectName)
			throws GatewayMetadataFieldsException {
		try {
			return json.getBoolean(objectName);
		} catch (JSONException ex) {
			throw new GatewayMetadataFieldsException(ex.getMessage());
		}
	}


	private JSONObject getJsonObjectFromJson(JSONObject json, String objectName)
			throws GatewayMetadataFieldsException {
		try {
			return json.getJSONObject(objectName);
		} catch (JSONException ex) {
			throw new GatewayMetadataFieldsException(ex.getMessage());
		}
	}

	
	private List<EntityDefinitionProperty> parseEntityProperties(
			JSONObject properties) throws GatewayMetadataFieldsException {
		List<EntityDefinitionProperty> entityProperties = new ArrayList<EntityDefinitionProperty>();
		if (properties != null) {
			Iterator<String> keyIterator = properties.keys();
			while (keyIterator.hasNext()) {
				String item = keyIterator.next();
				String propertyName = item;
				JSONObject property = getJsonObjectFromJson(properties, propertyName);
				Logger.getLogger(getClass()).debug("Parsing \n"+property.toString());
				
				String sample = String.valueOf(getFieldFromJson(property, SAMPLE_PROPERTY_TEXT));
				checkFieldNotNull("Sample", sample);
				
				String fieldName = String.valueOf(getFieldFromJson(property, FIELD_NAME_PROPERTY_TEXT));
				checkFieldNotNull("Field name", fieldName);
				
				String type = String.valueOf(getFieldFromJson(property, TYPE_PROPERTY_TEXT));
				checkFieldNotNull("Type", type);
				
				Boolean nullable = getBooleanFromJson(property, NULLABLE_PROPERTY_TEXT);
				checkFieldNotNull("Nullable", nullable);
				
				Integer length = getIntegerFromJson(property, LENGTH_PROPERTY_TEXT);
				checkFieldNotNull("Length", length);
				
				Boolean key = false;
				if (property.has(KEY_PROPERTY_TEXT)) {
					key = getBooleanFromJson(property, KEY_PROPERTY_TEXT);
					checkFieldNotNull("Key", key);
				}
				EntityDefinitionProperty newEntityProperty = new EntityDefinitionProperty(
						propertyName, fieldName, sample, type, nullable, length,
						key);
				entityProperties.add(newEntityProperty);
			}
		}
		Collections.sort(entityProperties);
		return entityProperties;
	}
	
	private void checkFieldNotNull(String expected, Object actual)
			throws GatewayMetadataFieldsException {
		if (actual == null) {
			throw new GatewayMetadataFieldsException(expected + " not found.");
		}
	}

}
