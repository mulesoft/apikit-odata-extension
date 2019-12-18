/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.util.FileUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RamlParserTestCase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private RamlParser ramlParser;
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

    entityDefinition.addProperty(new EntityDefinitionProperty("id", "Edm.Int32", false, true, null,
        null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("name", "Edm.String", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("description", "Edm.String", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("status", "Edm.String", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("published", "Edm.Boolean", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("draft", "Edm.Boolean", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("ch_domain", "Edm.String", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("ch_full_domain", "Edm.String", false,
        false, null, null, null, null, null, null, null));

    newEntitySet.addEntity(entityDefinition);

    entityDefinition = new EntityDefinition("users", "users");
    entityDefinition.setHasPrimaryKey(true);
    entityDefinition.addProperty(new EntityDefinitionProperty("id", "Edm.Int32", false, true, null,
        null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("first_name", "Edm.String", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("last_name", "Edm.String", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("email", "Edm.String", false, false,
        null, null, null, null, null, null, null));
    newEntitySet.addEntity(entityDefinition);

    entityDefinition = new EntityDefinition("odataTypes", "odataTypes");
    entityDefinition.setHasPrimaryKey(true);
    entityDefinition.addProperty(new EntityDefinitionProperty("edmString", "Edm.String", false,
        true, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmBoolean", "Edm.Boolean", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmDouble", "Edm.Double", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmSingle", "Edm.Single", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmBinary", "Edm.Binary", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmDateTime", "Edm.DateTime", false,
        false, null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmInt32", "Edm.Int32", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmInt64", "Edm.Int64", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmInt16", "Edm.Int16", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmByte", "Edm.Byte", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmDecimal", "Edm.Decimal", false,
        false, null, null, null, null, null, "3", "3"));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmGuid", "Edm.Guid", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmTime", "Edm.Time", false, false,
        null, null, null, null, null, null, null));
    entityDefinition.addProperty(new EntityDefinitionProperty("edmDateTimeOffset",
        "Edm.DateTimeOffset", false, false, null, null, null, null, null, null, null));
    newEntitySet.addEntity(entityDefinition);

    return newEntitySet;
  }

  @Test
  public void incompleteSchemaTest()
      throws OdataMetadataFieldsException, OdataMetadataFormatException {
    thrown.expect(OdataMetadataFieldsException.class);
    thrown.expectMessage(
        "Property \"nullable\" is missing in field \"draft\" in entity \"gateways\"");
    ramlParser.getEntitiesFromRaml(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/model-incomplete-schema.raml"));
  }

  @Test
  public void withoutSchemas() throws OdataMetadataFieldsException, OdataMetadataFormatException {
    thrown.expect(OdataMetadataFormatException.class);
    thrown.expectMessage("Type not supported. gateways");
    ramlParser.getEntitiesFromRaml(
        FileUtils.getAbsolutePath("org/mule/module/apikit/odata/metadata/without-schemas.raml"));
  }

  @Test
  public void withSchemasKey() throws OdataMetadataFieldsException, OdataMetadataFormatException {
    thrown.expect(OdataMetadataFieldsException.class);
    ramlParser.getEntitiesFromRaml(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/model-with-schemas-key.raml"));
  }

  @Test
  public void schemasMultipleKey()
      throws OdataMetadataFieldsException, OdataMetadataFormatException {
    EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/model-multiple-keys.raml"));
    EntityDefinition entityDefinition = entitySet.toList().get(0);
    assertThat(entityDefinition, is(getEntityByName("gateways")));
  }

  @Test
  public void schemasWithoutProperties()
      throws OdataMetadataFieldsException, OdataMetadataFormatException {
    thrown.expect(OdataMetadataFormatException.class);
    thrown.expectMessage("No schemas found.");
    ramlParser.getEntitiesFromRaml(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/model-without-properties.raml"));
  }

  @Test
  public void testPositive() throws OdataMetadataFieldsException, OdataMetadataFormatException {
    EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml(
        FileUtils.getAbsolutePath("org/mule/module/apikit/odata/metadata/odata.raml"));
    EntityDefinition entityDefinition = entitySet.toList().get(0);
    assertThat(entityDefinition, is(getEntityByName("gateways")));
  }

  @Test
  public void allowedTypes() throws OdataMetadataFieldsException, OdataMetadataFormatException {
    EntityDefinitionSet entitySet = ramlParser.getEntitiesFromRaml(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/model-allowed-types.raml"));
    EntityDefinition entityDefinition = entitySet.toList().get(0);
    assertThat(entityDefinition, is(getEntityByName("odataTypes")));
  }

  private EntityDefinition getEntityByName(String name) {
    for (EntityDefinition entityDefinition : mockEntitySet.toList()) {
      if (entityDefinition.getName().equals(name))
        return entityDefinition;
    }

    throw new RuntimeException("Entity not found in mock");
  }

}
