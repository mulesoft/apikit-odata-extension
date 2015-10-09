/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

public class GatewayMetadataTestCase {

	GatewayMetadataManager metadataManager;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private EntityDefinitionSet mockEntitySet;

	@Before
	public void setUp() throws Exception {
		metadataManager = new GatewayMetadataManager();
		mockEntitySet = mockEntitySet();
	}

	@Test
	public void retrieveEntityPositive() throws GatewayMetadataEntityNotFoundException, GatewayMetadataFieldsException, GatewayMetadataResourceNotFound,
			GatewayMetadataFormatException {
		metadataManager.refreshMetadata("org/mule/module/apikit/odata/metadata/raml/datagateway-definition.raml", true);
		Assert.assertEquals(metadataManager.getEntityByName("gateways"), mockEntitySet.toList().get(0));
	}

	@Test
	public void retrieveEntityNegative() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, GatewayMetadataFormatException,
			GatewayMetadataEntityNotFoundException {
		thrown.expect(GatewayMetadataEntityNotFoundException.class);
		thrown.expectMessage("Entity bla not found.");
		metadataManager.refreshMetadata("org/mule/module/apikit/odata/metadata/raml/datagateway-definition.raml", true);
		metadataManager.getEntityByName("bla");
	}

	private EntityDefinitionSet mockEntitySet() {
		EntityDefinitionSet newEntitySet = new EntityDefinitionSet();
		EntityDefinition entityDefinition;

		entityDefinition = new EntityDefinition("gateways", "gateways");
		entityDefinition.setHasPrimaryKey(true);
		// String name, String sample, String type, boolean nullable, int
		// length,
		// String description, boolean key
		entityDefinition.addProperty(new EntityDefinitionProperty("id", "id", "12", "integer", false, 4, true));
		entityDefinition.addProperty(new EntityDefinitionProperty("name", "name", "Ariel", "string", true, 45, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("description", "description", "Ariel", "string", true, 45, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("status", "status", "Ariel", "string", true, 255, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("published", "published", "Ariel", "boolean", true, 5, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("draft", "draft", "Ariel", "boolean", true, 5, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("ch_domain", "ch_domain", "Ariel", "string", true, 5, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("ch_full_domain", "ch_full_domain", "Ariel", "string", true, 5, false));
		newEntitySet.addEntity(entityDefinition);

		entityDefinition = new EntityDefinition("users", "users");
		entityDefinition.setHasPrimaryKey(true);
		entityDefinition.addProperty(new EntityDefinitionProperty("id", "id", "1", "integer", false, 4, true));
		entityDefinition.addProperty(new EntityDefinitionProperty("first_name", "first_name", "Marty", "string", true, 45, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("last_name", "last_name", "Mc Fly", "string", true, 45, false));
		entityDefinition.addProperty(new EntityDefinitionProperty("email", "email", "Mc Fly", "string", true, 45, false));
		newEntitySet.addEntity(entityDefinition);

		return newEntitySet;
	}

}
