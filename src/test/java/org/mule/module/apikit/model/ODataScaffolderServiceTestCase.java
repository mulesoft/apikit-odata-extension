/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author arielsegura
 */
public class ODataScaffolderServiceTestCase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  public ODataScaffolderService scaffolder;
  public String RESOURCES_PATH = "src/test/resources/";

  @Before
  public void setUp() throws Exception {
    scaffolder = new ODataScaffolderService();
  }

  private File getResource(String path) {
    File file = new File((RESOURCES_PATH + path).replace("/", File.separator));
    return file;
  }

  @Test
  public void scaffoldPositive() {
    File model = getResource("valid/api/odata.raml");
    File api = scaffolder.generateApi(model);
    assertTrue(api.exists());
  }

  @Test
  public void scaffoldNegative() {
    File model = getResource("invalid/api/odata.raml");
    try {
      scaffolder.generateApi(model);
    } catch (Exception e) {
      assertEquals("Error: Property \"remote name\" is missing in entity Employee", e.getMessage());
    }
  }

  @Test
  public void noKeyError() {
    File model = getResource("nokeyerror/api/odata.raml");
    try {
      scaffolder.generateApi(model);
    } catch (Exception e) {
      assertEquals("Error: Entity defition must have a primary key.", e.getMessage());
    }
  }

}
