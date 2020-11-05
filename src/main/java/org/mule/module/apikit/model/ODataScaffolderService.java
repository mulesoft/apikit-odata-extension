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

import static org.mule.module.apikit.odata.util.FileUtils.createFolder;
import static org.mule.module.apikit.odata.util.FileUtils.exportResource;
import org.mule.module.apikit.odata.util.FileUtils;
import java.io.File;
import java.io.IOException;

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
    if (model != null && isODataModel(model)) {
      copyLibraryFiles(model.getParentFile().getAbsolutePath());
      return generateApiRaml(model);
    }
    return null;
  }

  /**
   * Generates the api.raml root file from the odata model
   *
   * @param model
   * @return
   */
  private File generateApiRaml(File model) {
    try {
      String ramlContents = new RamlGenerator().generate(model.toURI().toString());
      String path = model.getCanonicalPath().replace(ODATA_MODEL_FILE, FINAL_RAML_FILE);
      return FileUtils.stringToFile(path, ramlContents);
    } catch (Exception e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  /**
   * Create folder /libraries
   * and copy odataLibrary.raml to the project
   */
  private void copyLibraryFiles(String absolutePath) {
    try {
      createFolder(absolutePath + LIBRARIES_FOLDER);
      exportResource(LIBRARIES_ODATA_RAML, absolutePath + LIBRARIES_ODATA_RAML);
    } catch (Exception e) {
      throw new RuntimeException("Error copying template files", e);
    }
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
