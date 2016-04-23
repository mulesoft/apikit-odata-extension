/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.model.exception.EntityModelParsingException;
import org.mule.module.apikit.model.exception.InvalidModelException;

public class EntityModelParser {

<<<<<<< HEAD
	static final String typesPattern = "^types:\\s*$";
	static final Pattern elementPattern = Pattern.compile("^\\s{2}(\\w+):\\s*$");
	static final Pattern remotePattern = Pattern.compile("^\\s{4}\\(odata.remote\\):\\s*(\\w+)\\s*$");
	static final String propertiesPattern = "^\\s{4}properties:\\s*$";
	static final Pattern fieldPattern = Pattern.compile("^\\s{6}(\\w+):\\s*$");
	static final Pattern typePropertyPattern = Pattern.compile("^\\s{8}type:\\s*(\\w+)\\s*$");
	static final Pattern keyPropertyPattern = Pattern.compile("^\\s{8}\\(odata.key\\):\\s*(\\w+)\\s*$");
	static final Pattern nullablePropertyPattern = Pattern.compile("^\\s{8}\\(odata.nullable\\):\\s*(\\w+)\\s*$");
	 
=======
	private static final String[] FIELD_PROPERTIES = { "name", "type", "nullable", "key", "defaultValue", "maxLength", "fixedLength", "collation", "unicode", "precision", "scale" };
	private static final String DEFAULT_JSON_SCHEMA = "model-schema.json";

	public EntityModelParser() {

	}

	public ProcessingReport validateJson(JSONObject obj) throws JsonProcessingException, IOException, ProcessingException {
		// Validate json data against json schema
		ObjectMapper m = new ObjectMapper();
		JsonNode fstabSchema = m.readTree(getClass().getClassLoader().getResource(DEFAULT_JSON_SCHEMA));

		JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

		JsonSchema schema = factory.getJsonSchema(fstabSchema);

		JsonNode good = JsonLoader.fromString(obj.toString());

		return schema.validate(good);
	}

	public List<Map<String, Object>> getEntities(JSONObject obj) throws IOException, ProcessingException, EntityModelParsingException {

		ProcessingReport report;
		report = validateJson(obj);

		if (!report.isSuccess()) {
			String msg = ValidationErrorsHandler.handle(report);
			throw new EntityModelParsingException(msg);
		}

		List<Map<String, Object>> entitySet = new ArrayList<Map<String, Object>>();

		JSONArray schemas = obj.getJSONArray("entities");
		for (int i = 0; i < schemas.length(); i++) {
			JSONObject entityJson = (JSONObject) ((JSONObject) schemas.get(i)).get("entity");
			String entityName = entityJson.getString("name");
			String remoteName = entityJson.getString("remoteName");

			Map<String, Object> entity = new HashMap<String, Object>();
			entity.put("name", entityName);
			entity.put("remoteName", remoteName);
			entity.put("json", generateJsonSchema(entityJson));
			Map<String, Object> parsedProperties = parseEntityProperties(entityJson.getJSONArray("properties"));
			entity.put("properties", parsedProperties.get("properties"));
			entity.put("keys", parsedProperties.get("keys"));
			entitySet.add(entity);

		}

		return entitySet;
	}

	private JSONObject generateJsonSchema(JSONObject entityJson) {
		JSONObject jsonSchema = new JSONObject();

		jsonSchema.put("properties", generateJsonSchemaProperties(entityJson.getJSONArray("properties")));
		jsonSchema.put("edm.name", entityJson.getString("name"));
		jsonSchema.put("edm.remoteName", entityJson.getString("remoteName"));
		jsonSchema.put("$schema", "http://json-schema.org/draft-04/schema#");
		jsonSchema.put("type", "object");
		jsonSchema.put("required", generateJsonSchemaRequiredProperties(entityJson.getJSONArray("properties")));
		jsonSchema.put("additionalProperties", false);

		return jsonSchema;
	}

	private JSONArray generateJsonSchemaRequiredProperties(JSONArray properties) {
		JSONArray required = new JSONArray();

		for (int i = 0; i < properties.length(); i++) {
			String name = (String) properties.getJSONObject(i).getJSONObject("field").get("name");
			Boolean isNullable = (Boolean)properties.getJSONObject(i).getJSONObject("field").get("nullable");
			if (isNullable) {
				continue;
			}
			required.put(name);
		}

		return required;
	}

	private JSONObject generateJsonSchemaProperties(JSONArray jsonArray) {
		JSONObject jsonProperties = new JSONObject();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonProperty = jsonArray.getJSONObject(i).getJSONObject("field");
			JSONObject jsonStructure = new JSONObject();

			for (String prop : FIELD_PROPERTIES) {
				try {
					jsonStructure.put("edm." + prop, jsonProperty.get(prop));
				} catch (Exception e) {
					// ignore missing property
				}
			}

			// infer json schema type from edm.type
			String type = (String) jsonProperty.get("type");

			jsonStructure.put("type", getSchemaTypeFromEdmType(type));

			jsonProperties.put(jsonProperty.getString("name"), jsonStructure);
		}

		return jsonProperties;
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

>>>>>>> b915db8f6156b67ceff017cd7673790d7067d914
	/**
	 * Parses the entities out of the RAML file and looks for required fields
	 * @param inputStream
	 * @return
	 * @throws IOException
	 * @throws EntityModelParsingException
	 */
	public List<Entity> getEntities(InputStream inputStream) throws IOException, EntityModelParsingException {

		try { 
  		String[] lines = getFileLines(inputStream);
  		return getEntities(lines);
		} catch (Exception e) {
			throw new EntityModelParsingException(e.getMessage());
		}
	}

	private List<Entity> getEntities(String[] lines) throws InvalidModelException {
		List<Entity> entities = new ArrayList<Entity>();
		boolean typesFound = false;

		Entity entity = new Entity(null);
		Property property = new Property(null);
		for (String line : lines) {
			
			// Looks for the "types:" keyword
			if (line.matches(typesPattern)) {
				typesFound = true;
			}
			
			// If "types" was found, looks for other possible matches to build the entities
			if (typesFound) {
				
				// Matches element name
				Matcher matcher = elementPattern.matcher(line);
				if (matcher.find()) {
					if (entity.getName() == null) {
						entity = new Entity(matcher.group(1));
					} else {
						if (entity.isValid()) {
							entities.add(entity);
							entity = new Entity(matcher.group(1));
						}
					}
				}

				matcher = remotePattern.matcher(line);
				if (matcher.find()) {
					entity.setRemote(matcher.group(1));
				}
				
				// Matches the properties keyword
				if (line.matches(propertiesPattern)) {
					entity.setPropertiesFound(true);
				}
				matcher = fieldPattern.matcher(line);
				if (matcher.find()) {
					if (property.getName() == null) {
						property = new Property(matcher.group(1));
					} else {
						if (property.isValid()) {
							entity.addProperty(property);
							property = new Property(matcher.group(1));
						}
					}
				}
				
				// Matches the required property attributes
				matcher = typePropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setType(matcher.group(1));
				}
				
				matcher = keyPropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setKey(matcher.group(1));
				}
				
				matcher = nullablePropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setNullable(matcher.group(1));
				}
			}	
		}
		
		if (!typesFound) {
			throw new InvalidModelException("no types definition where found, please check the model");
		}
		
		
		// if there is any entity left...
		if (property.isValid()) {
			entity.addProperty(property);
		}
		
		if (entity.isValid()) {
			entities.add(entity);
		}
		
		return entities;
	}	
	
	private String[] getFileLines(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		String text = writer.toString();
		String[] lines = text.split("\\n");
		return lines;
	}
	
}