/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.module.apikit.odata.exception.ODataInvalidFlowResponseException;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.model.Entry;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.Builder;
import org.odata4j.edm.EdmSchema;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.Responses;

public class Helper {

	private static final OdataMetadataManager gwMetadataManager = new OdataMetadataManager();

	public static EntitiesResponse convertEntriesToOEntries(List<Entry> outputEntries, String entitySetName, EntityDefinitionSet metadata1) {

		EdmEntitySet ees = Helper.createMetadata(metadata1).getEdmEntitySet(entitySetName);
		List<OEntity> entities = new ArrayList<OEntity>();
		List<OProperty<?>> properties = new ArrayList<OProperty<?>>();

		for (Entry outputEntry : outputEntries) {
			Map<String, Object> keys = new HashMap<String, Object>();

			properties = new ArrayList<OProperty<?>>();

			for (EdmProperty edmProperty : ees.getType().getProperties().toList()) {
				String propertyName = edmProperty.getName();
				Object propertyValue = outputEntry.getProperties().get(edmProperty.getName());
				// This means the request had a 'select' filter and it probably has
				// fewer fields than entity definition
				// that's why property value is null
				if (propertyValue != null) {

					OProperty oProperty = EDMTypeConverter.getOProperty(propertyName, propertyValue, edmProperty.getType());

					properties.add(oProperty);

					if (ees.getType().getKeys().contains(oProperty.getName())) {
						keys.put(oProperty.getName(), oProperty.getValue());
					}
				}

			}

			entities.add(OEntities.create(ees, OEntityKey.create(keys), properties, null));

		}

		return Responses.entities(entities, ees, null, null);
	}

	public static EdmDataServices createMetadata(EntityDefinitionSet metadata) {
		try {
			String namespace = "odata2.namespace";

			List<EdmEntityType.Builder> entityTypes = new ArrayList<EdmEntityType.Builder>();
			List<EdmEntitySet.Builder> entitySets = new ArrayList<EdmEntitySet.Builder>();

			for (EntityDefinition entityMetadata : metadata.toList()) {

				List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();

				List<String> keys = new ArrayList<String>();

				for (EntityDefinitionProperty propertyMetadata : entityMetadata.getProperties()) {
					Builder builder = EdmProperty.newBuilder(propertyMetadata.getName()).setType(EDMTypeConverter.convert(propertyMetadata.getType()));
					
					builder.setNullable(propertyMetadata.isNullable());
					
					if (propertyMetadata.getDefaultValue() != null) {
					  builder.setDefaultValue(propertyMetadata.getDefaultValue());
					}
					if (propertyMetadata.getMaxLength() != null) {
					  builder.setMaxLength(Integer.parseInt(propertyMetadata.getMaxLength()));
					}
					if (propertyMetadata.getCollation() != null) {
					  builder.setCollation(propertyMetadata.getCollation());
					}
					if (propertyMetadata.isUnicode() != null) {
					  builder.setUnicode(propertyMetadata.isUnicode());
					}
					if (propertyMetadata.getPrecision() != null) {
					  builder.setPrecision(Integer.parseInt(propertyMetadata.getPrecision()));
					}
					if (propertyMetadata.getScale() != null) {
					  builder.setScale(Integer.parseInt(propertyMetadata.getScale()));
					}
					
					properties.add(builder);
					
					if (propertyMetadata.isKey()) {
						keys.add(propertyMetadata.getName());
					}
				}

				String entityName = entityMetadata.getName();
				EdmEntityType.Builder type = EdmEntityType.newBuilder().setNamespace(namespace).setName(entityName).addKeys(keys).addProperties(properties);
				entityTypes.add(type);

				entitySets.add(EdmEntitySet.newBuilder().setName(entityName).setEntityType(type));
			}

			EdmEntityContainer.Builder container = EdmEntityContainer.newBuilder().setName("ODataEntityContainer").setIsDefault(true).addEntitySets(entitySets);
			EdmSchema.Builder containerSchema = EdmSchema.newBuilder().setNamespace("odata2.namespace").addEntityContainers(container).addEntityTypes(entityTypes);

			return EdmDataServices.newBuilder().addSchemas(containerSchema).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getPayload(URL url) {
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		StringBuilder result = new StringBuilder();
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public static Map<String, String> queryToMap(String query) {
		Map<String, String> queryMap = new HashMap<String, String>();

		if (query != null && query != "") {
			String[] queries = query.split("&");
			for (String q : queries) {
				String[] parts = q.split("=");
				queryMap.put(parts[0], parts[1]);
			}
		}

		return queryMap;
	}

	public static List<Entry> transformJsonToEntryList(String payload) throws ODataInvalidFlowResponseException {
		try {
			List<Entry> entities = new ArrayList<Entry>();

			JSONObject response = new JSONObject(payload);
			JSONArray objects = response.getJSONArray("entries");

			for (int i = 0; i < objects.length(); i++) {
				JSONObject j = objects.optJSONObject(i);
				Iterator<?> it = j.keys();
				Entry e = new Entry();
				while (it.hasNext()) {
					String n = it.next().toString();
					e.addProperty(n, j.get(n));
				}
				entities.add(e);
			}

			return entities;
		} catch (Exception e) {
			throw new ODataInvalidFlowResponseException("Flow response is not a valid json");
		}
	}

}
