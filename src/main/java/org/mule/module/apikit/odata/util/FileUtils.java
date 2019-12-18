/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;


import java.io.File;
import java.io.FileInputStream;
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

  public static String readFromFile(String filePath) throws IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
    File file = new File(url.getPath());
    try (InputStream is = new FileInputStream(file); StringWriter writer = new StringWriter()) {
      IOUtils.copy(is, writer);
      return writer.toString();
    }
  }

  public static File stringToFile(String path, String body) throws IOException {
    File file = new File(path);

    try (FileOutputStream fop = new FileOutputStream(file)) {

      // if file doesn't exists, then create it
      if (!file.exists() && !file.createNewFile()) {
        throw new IOException("Cannot create " + path);
      }

      // get the content in bytes
      byte[] contentInBytes = body.getBytes();

      fop.write(contentInBytes);
      fop.flush();

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
  public static String exportResource(String resourceName, String targetPath) throws IOException {
    try (InputStream in = FileUtils.class.getResource(resourceName).openStream();
        OutputStream out = new FileOutputStream(targetPath)) {
      IOUtils.copy(in, out);
    }
    return targetPath;
  }

  public static boolean createFolder(String path) {
    return new File(path).mkdir();
  }

  public static String getAbsolutePath(String relativePath) {
    return Thread.currentThread().getContextClassLoader().getResource(relativePath).toString();
  }
}
