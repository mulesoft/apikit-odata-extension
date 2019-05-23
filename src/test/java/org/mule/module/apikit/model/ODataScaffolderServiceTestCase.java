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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.apikit.model.RuntimeEdition;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    File api = getResource("valid/app/api.xml");
    api.delete();
  }

  private File getResource(String path) {
    File file = new File((RESOURCES_PATH + path).replace("/", File.separator));
    return file;
  }

  @Test
  public void scaffoldPositive() {
    File api = getResource("valid/app/api.xml");
    Assert.assertFalse(api.exists());
    List<File> ramlFiles = new ArrayList<File>();
    File model = getResource("valid/api/odata.raml");
    File appDir = getResource("valid/app");
    File domainDir = getResource("valid/domain");
    ramlFiles.add(model);
    scaffolder.executeScaffolder(ramlFiles, appDir, domainDir, "4.0.0", RuntimeEdition.EE);
    api = getResource("valid/app/api.xml");
    Assert.assertTrue(api.exists());
  }

  @Test
  public void scaffoldNegative() {
    File api = getResource("valid/app/api.xml");
    Assert.assertFalse(api.exists());
    List<File> ramlFiles = new ArrayList<File>();
    File modelJson = getResource("invalid/api/odata.raml");
    File appDir = getResource("invalid/app");
    File domainDir = getResource("invalid/domain");
    ramlFiles.add(modelJson);
    thrown.expect(RuntimeException.class);
    scaffolder.executeScaffolder(ramlFiles, appDir, domainDir, "4.0.0", RuntimeEdition.EE);
    api = getResource("valid/app/api.xml");
    Assert.assertFalse(api.exists());
  }


  @Test
  public void noKeyError() {
    File api = getResource("valid/app/api.xml");
    Assert.assertFalse(api.exists());
    List<File> ramlFiles = new ArrayList<File>();
    File modelJson = getResource("nokeyerror/api/odata.raml");
    File appDir = getResource("invalid/app");
    File domainDir = getResource("invalid/domain");
    ramlFiles.add(modelJson);
    try {
      scaffolder.executeScaffolder(ramlFiles, appDir, domainDir, "4.0.0", RuntimeEdition.EE);
    } catch (Exception e) {
      Assert.assertEquals("Error: Entity defition must have a primary key.", e.getMessage());
    }
  }

}
