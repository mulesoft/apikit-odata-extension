/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.model.exception.InvalidModelException;

public class EntityModelTestCase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEntityNamesSetByConstructor() throws Exception {
    Entity e = new Entity("customer");

    Assert.assertEquals("customer", e.getName());
    Assert.assertEquals("customers", e.getCollectionName());
    Assert.assertEquals("customer", e.getElementName());

    e = new Entity("customers");
    Assert.assertEquals("customers", e.getName());
    Assert.assertEquals("customers", e.getCollectionName());
    Assert.assertEquals("customers", e.getElementName());

    e = new Entity("Customers");
    Assert.assertEquals("Customers", e.getName());
    Assert.assertEquals("Customers", e.getCollectionName());
    Assert.assertEquals("Customers", e.getElementName());

    e = new Entity("Baby");
    Assert.assertEquals("Baby", e.getName());
    Assert.assertEquals("Babies", e.getCollectionName());
    Assert.assertEquals("Baby", e.getElementName());

    e = new Entity("Babies");
    Assert.assertEquals("Babies", e.getName());
    Assert.assertEquals("Babies", e.getCollectionName());
    Assert.assertEquals("Babies", e.getElementName());
  }

  @Test
  public void testValidEntityModel() throws InvalidModelException {
    Entity entity = new Entity("customer");
    Property property = getValidProperty();
    entity.addProperty(property);
    entity.setPropertiesFound(true);
    entity.setRemote("remote_customer");
    Assert.assertTrue(entity.isValid());
  }

  @Test
  public void testInvalidNameForEntity() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Entity entity = new Entity(StringUtils.EMPTY);
    entity.isValid();
  }

  @Test
  public void testMissingPropertyForEntity() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Entity entity = new Entity("customer");
    entity.setRemote("remote_customer");
    entity.isValid();
  }

  @Test
  public void testMissingRemoteForEntity() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Entity entity = new Entity("customer");
    entity.addProperty(getValidProperty());
    entity.setPropertiesFound(true);
    entity.isValid();
  }

  @Test
  public void testValidProperty() throws InvalidModelException {
    Property property = getValidProperty();
    Assert.assertTrue(property.isValid());
  }

  @Test
  public void testInvalidNameForProperty() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Property property = new Property(StringUtils.EMPTY);
    property.isValid();
  }

  @Test
  public void testMissingKeyForProperty() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Property property = new Property("id");
    property.setNullable("false");
    property.setType("Edm.String");
    property.isValid();
  }

  @Test
  public void testMissingNullableForProperty() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Property property = new Property("id");
    property.setKey("true");
    property.setType("Edm.String");
    property.isValid();
  }

  @Test
  public void testMissingTypeForProperty() throws InvalidModelException {
    thrown.expect(InvalidModelException.class);
    Property property = new Property("id");
    property.setNullable("false");
    property.setKey("true");
    property.isValid();
  }

  private Property getValidProperty() throws InvalidModelException {
    Property property = new Property("id");
    property.setKey("true");
    property.setNullable("false");
    property.setType("Edm.String");
    return property;
  }

}
