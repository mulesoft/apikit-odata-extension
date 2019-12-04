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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ODataScaffolderService {

  private static final String LIBRARIES_FOLDER = "/libraries";
  private static final String LIBRARIES_ODATA_RAML = "/libraries/odataLibrary.raml";
  private final static String API_FOLDER = "api";
  private final static String ODATA_MODEL_FILE = "odata.raml";
  private final static String FINAL_RAML_FILE = "api.raml";

  /**
   * Generates the api.raml root file from the odata model and it's dependencies
   *
   * @param model
   * @return
   */
  public File generateApi(File model) {
    File ramlFile = null;

    if (model != null && isODataModel(model)) {
      copyRamlTemplateFiles(model);
      ramlFile = generateApiRaml(model);
    }

    return ramlFile;
  }

  /**
   * Generates the api.raml root file from the odata model
   *
   * @param model
   * @return
   */
  private File generateApiRaml(File model) {
    RamlGenerator ramlGenerator = new RamlGenerator();
    File raml = null;

    try {
      String ramlContents = ramlGenerator.generate(model.toURI().toString());
      String path = model.getCanonicalPath().replace(ODATA_MODEL_FILE, FINAL_RAML_FILE);
      raml = FileUtils.stringToFile(path, ramlContents);
    } catch (Exception e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }

    return raml;
  }

  /**
   * Copies the api.raml dependencies to the project
   *
   * @param model
   * @return
   */
  private List<String> copyRamlTemplateFiles(File model) {
    List<String> ramlFiles = new ArrayList<String>();

    try {
      FileUtils.createFolder(model.getParentFile().getAbsolutePath() + LIBRARIES_FOLDER);
      ramlFiles.add(FileUtils.exportResource(LIBRARIES_ODATA_RAML,
          model.getParentFile().getAbsolutePath() + LIBRARIES_ODATA_RAML));
    } catch (Exception e) {
      throw new RuntimeException("Error copying template files", e);
    }

    return ramlFiles;
  }

  private boolean isODataModel(File file) {
    try {
      String path = file.getCanonicalPath();
      return path.contains(API_FOLDER + File.separator + ODATA_MODEL_FILE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
