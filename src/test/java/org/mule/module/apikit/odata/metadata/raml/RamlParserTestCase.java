/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

/**
 * 
 * @author arielsegura
 */
public class RamlParserTestCase {

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
	public void incompleteSchemaTest() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFieldsException.class);
		thrown.expectMessage("Property \"nullable\" is missing in field \"draft\" in entity \"gateways\"");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/incomplete-schema.raml");
	}

	@Ignore // this test no longer makes sense since the trait is in the library
	@Test
	public void withSomeTraitsTest() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFormatException.class);
		thrown.expectMessage("RAML is invalid. See log list.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/with-some-traits.raml");
	}

	@Ignore // this test no longer makes sense since the trait is in the library
	@Test
	public void withoutTraitsTest() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFormatException.class);
		thrown.expectMessage("RAML is invalid. See log list.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/without-traits.raml");
	}

	@Test
	public void withoutSchemas() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFormatException.class);
		thrown.expectMessage("Type not supported. gateways");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/without-schemas.raml");
	}

	@Test
	public void withSchemasKey() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFieldsException.class);
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/with-schemas-key.raml");
	}

	@Test
	public void schemasMultipleKey() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/schemas-multiple-keys.raml");
		EntityDefinition entityDefinition = entitySet.toList().get(0);
		for (int i = 0; i < mockEntitySet.toList().get(0).getProperties().size(); i++) {
			EntityDefinitionProperty expectedProperty = mockEntitySet.toList().get(0).getProperties().get(i);
			Assert.assertEquals(expectedProperty, entityDefinition.getProperties().get(i));
		}
		Assert.assertEquals(mockEntitySet.toList().get(0), entityDefinition);
	}

	@Test
	public void schemasWithoutProperties() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		thrown.expect(OdataMetadataFormatException.class);
		thrown.expectMessage("No schemas found.");
		ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/schema-without-properties.raml");
	}

	@Test
	public void testPositive() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml("src/test/resources/org/mule/module/apikit/odata/metadata/datagateway-definition.raml");
		EntityDefinition entityDefinition = entitySet.toList().get(0);
		for (int i = 0; i < mockEntitySet.toList().get(0).getProperties().size(); i++) {
			EntityDefinitionProperty expectedProperty = mockEntitySet.toList().get(0).getProperties().get(i);
			Assert.assertEquals(expectedProperty, entityDefinition.getProperties().get(i));
		}
		Assert.assertEquals(mockEntitySet.toList().get(0), entityDefinition);
	}

}