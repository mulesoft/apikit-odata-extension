/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author arielsegura
 */
public class FileUtils {

  // This methods probably should be in another mulesoft labs repo
  public static String readFromFile(String filePath) throws FileNotFoundException, IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
    File file = new File(url.getPath());
    InputStream is = new FileInputStream(file);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer);
    is.close();
    return writer.toString();
  }

  public static String readFromFile(InputStream input) throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(input, writer);
    input.close();
    return writer.toString();
  }

  public static File stringToFile(String path, String body) throws IOException {
    File file = new File(path);

    try (FileOutputStream fop = new FileOutputStream(file)) {

      // if file doesn't exists, then create it
      if (!file.exists()) {
        file.createNewFile();
      }

      // get the content in bytes
      byte[] contentInBytes = body.getBytes();

      fop.write(contentInBytes);
      fop.flush();
      fop.close();

    } catch (IOException e) {
      throw e;
    }

    return file;
  }

  /**
   * Export a resource embedded into a Jar file to the local file path.
   *
   * @param resourceName
   *          ie.: "/SmartLibrary.dll"
   * @return The path to the exported resource
   * @throws Exception
   */
  static public String exportResource(String resourceName, String targetPath) throws Exception {
    InputStream in = null;
    OutputStream out = null;

    try {
      in = FileUtils.class.getResource(resourceName).openStream();

      out = new FileOutputStream(targetPath);

      IOUtils.copy(in, out);

    } catch (Exception ex) {
      throw ex;
    } finally {
      in.close();
      out.close();
    }

    return targetPath;
  }

  static public boolean createFolder(String path) {
    return new File(path).mkdir();
  }
}
