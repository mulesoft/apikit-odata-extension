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

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.io.File;
import static org.apache.commons.io.FileUtils.contentEquals;
import static org.junit.Assert.assertTrue;


public class ODataScaffolderServiceTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ODataScaffolderService scaffolder;
  private String RESOURCES_PATH = "src/test/resources/";

  @Before
  public void setUp() {
    scaffolder = new ODataScaffolderService();
  }

  private File getResource(String path) {
    return new File((RESOURCES_PATH + path).replace("/", File.separator));
  }

  @Test
  public void scaffoldPositive() throws IOException {
    File model = getResource("valid/api/odata.raml");
    File api = scaffolder.generateApi(model);
    assertTrue(contentEquals(getResource("valid/api/libraries/odataLibrary.raml"),
        new File("src/main/resources/libraries/odataLibrary.raml")));
    assertTrue(api.exists());
  }

  @Test
  public void scaffoldNegative() {
    expectedException.expect(RuntimeException.class);
    expectedException
        .expectMessage("Error: Property \"remote name\" is missing in entity Employee");
    scaffolder.generateApi(getResource("invalid/api/odata.raml"));
  }

  @Test
  public void noKeyError() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("Error: Entity defition must have a primary key.");
    scaffolder.generateApi(getResource("nokeyerror/api/odata.raml"));
  }

}
