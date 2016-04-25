/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.raml.interfaces.model.IRaml;

/**
 * 
 * @author arielsegura
 */
public class RamlParser {
	
	private static final String NAMESPACE = "edm";
	
	private static final String REMOTE_NAME = "remoteName";
	private static final String NAME_PROPERTY = "name";
	private static final String TYPE_PROPERTY = "type";
	private static final String NULLABLE_PROPERTY = "nullable";
	private static final String KEY_PROPERTY = "key";
	private static final String READ_ONLY_PROPERTY = "readOnly";
	private static final String DEFAULT_VALUE_PROPERTY = "defaultValue";
	private static final String MAX_LENGTH_PROPERTY = "maxLength";
	private static final String FIXED_LENGTH_PROPERTY = "fixedLength";
	private static final String COLLATION_PROPERTY = "collation";
	private static final String UNICODE_PROPERTY = "unicode";
	private static final String PRECISION_PROPERTY = "precision";
	private static final String SCALE_PROPERTY = "scale";

	public EntityDefinitionSet getEntitiesFromRaml(String path) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {
		RamlParserUtils.validateRaml(path);
		return getEntitiesFromRaml(RamlParserUtils.getRaml(path));
	}

	public EntityDefinitionSet getEntitiesFromRaml(IRaml raml) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {
		Logger.getLogger(getClass()).info("Loading entities from RAML...");
		Long initTime = System.nanoTime();
		Long part = System.nanoTime();

		EntityDefinitionSet entitySet = new EntityDefinitionSet();

		List<Map<String, String>> schemas = raml.getSchemas();

		if (schemas.isEmpty()) {
			throw new OdataMetadataFormatException("No schemas found. ");
		}
		part = System.nanoTime();
		Logger.getLogger(getClass()).info("Parsing schemas reached in " + (part - initTime) / 1000000);

		for (int i = 0; i < schemas.size(); i++) {
			Map<String, String> schema = schemas.get(i);
			if (schema.keySet().size() != 1) {
				throw new OdataMetadataFormatException("A schema must contain only one key and it has " + schema.keySet().size());
			}
			String entityName = (String) schema.keySet().toArray()[0];

			part = System.nanoTime();
			JSONObject jsonSchema = null;
			try {
				jsonSchema = new JSONObject(schema.get(entityName));
			} catch (JSONException ex) {
				throw new OdataMetadataFormatException(ex.getMessage());
			}
			Logger.getLogger(getClass()).info("Schema to JSON in " + (System.nanoTime() - part) / 1000000 + " ms!");

			String remoteName = getStringFromJson(jsonSchema, NAMESPACE + "." + REMOTE_NAME);
			notNull("\"remoteName\" not found in entity \"" + entityName + "\"", remoteName);

			EntityDefinition entity = new EntityDefinition(entityName, remoteName);

			JSONObject properties = getJsonObjectFromJson(jsonSchema, "properties");
			if (properties == null) {
				throw new OdataMetadataResourceNotFound("Properties not found in entity " + entityName + ".");
			}
			part = System.nanoTime();
			entity.setProperties(parseEntityProperties(properties, entity));
			// TODO See if it's possible to avoid this for
			for (EntityDefinitionProperty property : entity.getProperties()) {
				if (property.isKey()) {
					entity.setHasPrimaryKey(true);
					break;
				}
			}
			Logger.getLogger(getClass()).info("Properties parsed in " + (System.nanoTime() - part) / 1000000 + " ms!");
			entitySet.addEntity(entity);
		}
		Long end = System.nanoTime();
		Long total = (end - initTime) / 1000000;
		Logger.getLogger(getClass()).info("Done in " + total + " ms!");

		return entitySet;
	}

	private String getStringFromJson(JSONObject json, String objectName) throws OdataMetadataFieldsException {
		try {
			return json.getString(objectName);
		} catch (JSONException ex) {
			throw new OdataMetadataFieldsException(ex.getMessage());
		}
	}

	private Object getFieldFromJson(JSONObject json, String objectName) throws OdataMetadataFieldsException {
		try {
			return json.get(objectName);
		} catch (JSONException ex) {
			throw new OdataMetadataFieldsException(ex.getMessage());
		}
	}

	private int getIntegerFromJson(JSONObject json, String objectName) throws OdataMetadataFieldsException {
		try {
			return json.getInt(objectName);
		} catch (JSONException ex) {
			throw new OdataMetadataFieldsException(ex.getMessage());
		}
	}

	private Boolean getBooleanFromJson(JSONObject json, String objectName) throws OdataMetadataFieldsException {
		try {
			return json.getBoolean(objectName);
		} catch (JSONException ex) {
			throw new OdataMetadataFieldsException(ex.getMessage());
		}
	}

	private JSONObject getJsonObjectFromJson(JSONObject json, String objectName) throws OdataMetadataFieldsException {
		try {
			return json.getJSONObject(objectName);
		} catch (JSONException ex) {
			throw new OdataMetadataFieldsException(ex.getMessage());
		}
	}

	private List<EntityDefinitionProperty> parseEntityProperties(JSONObject properties, EntityDefinition entity) throws OdataMetadataFieldsException {
		List<EntityDefinitionProperty> entityProperties = new ArrayList<EntityDefinitionProperty>();
		if (properties != null) {
			Iterator<String> keyIterator = properties.keys();
			while (keyIterator.hasNext()) {
				String item = keyIterator.next();
				String field = item;
				JSONObject property = getJsonObjectFromJson(properties, field);
				Logger.getLogger(getClass()).debug("Parsing \n" + property.toString());

				String name = getString(property, NAME_PROPERTY);
				String type = getString(property, TYPE_PROPERTY);
				Boolean nullable = getBoolean(property, NULLABLE_PROPERTY, field);
				Boolean key = getBoolean(property, KEY_PROPERTY, field);
				String defaultValue = getString(property, DEFAULT_VALUE_PROPERTY);
				String maxLength = getString(property, MAX_LENGTH_PROPERTY);
				Boolean fixedLength = getBoolean(property, FIXED_LENGTH_PROPERTY, field);
				String collation = getString(property, COLLATION_PROPERTY);
				Boolean unicode = getBoolean(property, UNICODE_PROPERTY, field);
				String precision = getString(property, PRECISION_PROPERTY);
				String scale = getString(property, SCALE_PROPERTY);

				notNull("Property \"name\" is missing in field \"" + field + "\" in entity \"" + entity.getName() + "\"", name);
				notNull("Property \"type\" is missing in field \"" + field + "\" in entity \"" + entity.getName() + "\"", type);
				notNull("Property \"key\" is missing in field \"" + field + "\" in entity \"" + entity.getName() + "\"", key);
				notNull("Property \"nullable\" is missing in field \"" + field + "\" in entity \"" + entity.getName() + "\"", nullable);

				EntityDefinitionProperty newEntityProperty = new EntityDefinitionProperty(name, type, nullable, key, defaultValue, maxLength, fixedLength, collation, unicode, precision, scale);
				entityProperties.add(newEntityProperty);

			}
		}
		Collections.sort(entityProperties);
		return entityProperties;
	}
	
	private void notNull(String message, Object actual) throws OdataMetadataFieldsException {
		if (actual == null) {
			throw new OdataMetadataFieldsException(message);
		}
	}
	
	private String getString(JSONObject property, String propertyName) {
		try { 
			return String.valueOf(property.get(NAMESPACE + "." + propertyName));
		} catch (JSONException e){
			return null;
		}
	}
	
	private Boolean getBoolean(JSONObject property, String propertyName, String field) throws OdataMetadataFieldsException {
		String str = getString(property, propertyName);
		if (str == null) {
			return null;
		} else if (str.equals("true") || str.equals("false")) {
			return Boolean.valueOf(str);
		}
		throw new OdataMetadataFieldsException("Property \"" + propertyName + "\" in field \"" + field + "\" must be a boolean.");
	}

}
