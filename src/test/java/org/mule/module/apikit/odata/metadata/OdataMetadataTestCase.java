/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.util.FileUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OdataMetadataTestCase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private EntityDefinitionSet mockEntitySet;

  @Before
  public void setUp() {
    mockEntitySet = mockEntitySet();
  }

  @Test
  public void retrieveEntityPositive() throws OdataMetadataEntityNotFoundException,
      OdataMetadataFieldsException, OdataMetadataFormatException {
    final OdataMetadataManager metadataManager = new OdataMetadataManagerImpl(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/datagateway-definition.raml"));
    Assert.assertEquals(metadataManager.getEntityByName("gateways"), mockEntitySet.toList().get(0));
  }

  @Test
  public void folderWithSpaces() throws OdataMetadataEntityNotFoundException,
      OdataMetadataFieldsException, OdataMetadataFormatException {
    OdataMetadataManager metadataManager = new OdataMetadataManagerImpl(FileUtils.getAbsolutePath(
        "org/mule/module/apikit/odata/metadata/folder with spaces/datagateway-definition.raml"));
    Assert.assertEquals(metadataManager.getEntityByName("gateways"), mockEntitySet.toList().get(0));
    metadataManager =
        new org.mule.module.apikit.odata.backward.compatibility.OdataMetadataManagerImpl(
            FileUtils.getAbsolutePath(
                "org/mule/module/apikit/odata/metadata/folder with spaces/datagateway-definition.raml"));
    Assert.assertEquals(metadataManager.getEntityByName("gateways"), mockEntitySet.toList().get(0));
  }

  @Test
  public void retrieveEntityNegative() throws OdataMetadataFieldsException,
      OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
    thrown.expect(OdataMetadataEntityNotFoundException.class);
    thrown.expectMessage("Entity bla not found.");
    final OdataMetadataManager metadataManager = new OdataMetadataManagerImpl(FileUtils
        .getAbsolutePath("org/mule/module/apikit/odata/metadata/datagateway-definition.raml"));
    metadataManager.getEntityByName("bla");
  }

  @Test
  public void decimalPrecisionAndScaleTestCase() throws Exception {
    final OdataMetadataManager metadataManager = new OdataMetadataManagerImpl(
        FileUtils.getAbsolutePath("org/mule/module/apikit/odata/decimal-precision.raml"));
    final EntityDefinition myType = metadataManager.getEntityByName("myType");
    for (EntityDefinitionProperty entityDefinitionProperty : myType.getProperties()) {
      assertEquals("Edm.Decimal", entityDefinitionProperty.getType());
    }

  }

  @Test
  public void entityDefinitionSetToJsonTest() {
    JSONObject jsonObject = new JSONObject(mockEntitySet.toJsonString());
    assertTrue(!jsonObject.isEmpty());
  }

  @Test
  public void entityDefinitionSetUniquenessTest() {
    HashSet<EntityDefinitionSet> set = new HashSet<>();
    EntityDefinitionSet definitionSet1 = mockEntitySet();
    EntityDefinitionSet definitionSet2 = mockEntitySet();
    set.add(definitionSet1);
    assertFalse(set.add(definitionSet2));
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

    List<EntityDefinitionProperty> properties = new ArrayList<>();
    EntityDefinitionProperty definitionProperty = new EntityDefinitionProperty("id", "Edm.Int32",
        false, true, null, null, null, null, null, null, null);
    properties.add(definitionProperty);
    properties.add(new EntityDefinitionProperty("first_name", "Edm.String", false, false, null,
        null, null, null, null, null, null));
    properties.add(new EntityDefinitionProperty("last_name", "Edm.String", false, false, null, null,
        null, null, null, null, null));
    properties.add(new EntityDefinitionProperty("email", "Edm.String", false, false, null, null,
        null, null, null, null, null));
    entityDefinition = new EntityDefinition("users", properties, "users");
    entityDefinition.setHasPrimaryKey(true);
    newEntitySet.addEntity(entityDefinition);

    return newEntitySet;
  }

}
