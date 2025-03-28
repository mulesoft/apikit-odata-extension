/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.context;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class OdataContextVariablesTestCase {

  private OdataContextVariables odataContextVariables;
  private String remoteEntityName;
  private String keyNames;
  private List<String> fields;

  @Before
  public void setUp() {
    remoteEntityName = "TestEntity";
    keyNames = "id,name";
    fields = Arrays.asList("id", "name", "description");
    odataContextVariables = new OdataContextVariables(remoteEntityName, keyNames, fields);
  }

  @Test
  public void testConstructorAndGetters() {
    assertEquals(remoteEntityName, odataContextVariables.getRemoteEntityName());
    assertEquals(keyNames, odataContextVariables.getKeyNames());
    assertEquals(fields, odataContextVariables.getFields());
  }

  @Test
  public void testSetAndGetRemoteEntityName() {
    String newRemoteEntityName = "NewTestEntity";
    odataContextVariables.setRemoteEntityName(newRemoteEntityName);
    assertEquals(newRemoteEntityName, odataContextVariables.getRemoteEntityName());
  }

  @Test
  public void testSetAndGetKeyNames() {
    String newKeyNames = "uuid,email";
    odataContextVariables.setKeyNames(newKeyNames);
    assertEquals(newKeyNames, odataContextVariables.getKeyNames());
  }

  @Test
  public void testSetAndGetFields() {
    List<String> newFields = Arrays.asList("uuid", "email", "age");
    odataContextVariables.setFields(newFields);
    assertEquals(newFields, odataContextVariables.getFields());
  }
}
