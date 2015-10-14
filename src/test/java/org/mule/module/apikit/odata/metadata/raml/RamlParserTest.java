/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

/**
 * 
 * @author arielsegura
 */
public class RamlParserTest {

	RamlParser ramlParser;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private EntityDefinitionSet mockEntitySet;

	@Before
	public void setUp() throws Exception {
		ramlParser = new RamlParser();
		mockEntitySet = mockEntitySet();
	}

	private EntityDefinitionSet mockEntitySet() {
		EntityDefinitionSet newEntitySet = new EntityDefinitionSet();
		EntityDefinition entityDefinition;
		entityDefinition = new EntityDefinition("gateways", "gateways");
		entityDefinition.setHasPrimaryKey(true);
		
		entityDefinition.addProperty(new EntityDefinitionProperty("id", "Edm.Int32", false, true, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("name", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("description", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("status", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("published", "Edm.Boolean", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("draft", "Edm.Boolean", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("ch_domain", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("ch_full_domain", "Edm.String", false, false, null, null, null, null, null, null, null));
		
		newEntitySet.addEntity(entityDefinition);

		entityDefinition = new EntityDefinition("users", "users");
		entityDefinition.setHasPrimaryKey(true);
		entityDefinition.addProperty(new EntityDefinitionProperty("id", "Edm.Int32", false, true, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("first_name", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("last_name", "Edm.String", false, false, null, null, null, null, null, null, null));
		entityDefinition.addProperty(new EntityDefinitionProperty("email", "Edm.String", false, false, null, null, null, null, null, null, null));
		newEntitySet.addEntity(entityDefinition);

		return newEntitySet;
	}

	@Test
	public void incompleteSchemaTest() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFieldsException.class);
		thrown.expectMessage("Property \"nullable\" is missing in field \"draft\" in entity \"gateways\"");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/incomplete-schema.raml");
	}

	@Test
	public void withSomeTraitsTest() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFormatException.class);
		thrown.expectMessage("RAML is invalid. See log list.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/with-some-traits.raml");
	}

	@Test
	public void withoutTraitsTest() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFormatException.class);
		thrown.expectMessage("RAML is invalid. See log list.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/without-traits.raml");
	}

	@Test
	public void withoutSchemas() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFormatException.class);
		thrown.expectMessage("No schemas found.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/without-schemas.raml");
	}

	@Test
	public void withSchemasKey() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFieldsException.class);
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/with-schemas-key.raml");
	}

	@Test
	public void schemasMultipleKey() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		
		thrown.expect(GatewayMetadataFormatException.class);
		thrown.expectMessage("Duplicate key \"edm.remoteName\"");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/schemas-multiple-keys.raml");
	}

	@Test
	public void schemasWithoutProperties() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFieldsException.class);
		thrown.expectMessage("JSONObject[\"properties\"] not found.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/schema-without-properties.raml");
	}

	@Test
	public void invalidKeyValue() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFieldsException.class);
		thrown.expectMessage("Property \"key\" in field \"draft\" must be a boolean.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/invalid-key-value.raml");
	}

	@Test
	public void invalidNullableValue() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		thrown.expect(GatewayMetadataFieldsException.class);
		thrown.expectMessage("Property \"nullable\" in field \"draft\" must be a boolean.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/raml/invalid-nullable-value.raml");
	}

	@Test
	public void testPositive() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml("org/mule/module/apikit/odata/metadata/raml/datagateway-definition.raml");
		EntityDefinition entityDefinition = entitySet.toList().get(0);
		for (int i = 0; i < mockEntitySet.toList().get(0).getProperties().size(); i++) {
			EntityDefinitionProperty expectedProperty = mockEntitySet.toList().get(0).getProperties().get(i);
			Assert.assertEquals(expectedProperty, entityDefinition.getProperties().get(i));
		}
		Assert.assertEquals(mockEntitySet.toList().get(0), entityDefinition);
	}

}
