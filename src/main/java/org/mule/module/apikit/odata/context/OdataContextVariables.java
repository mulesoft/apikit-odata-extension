/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.context;

import java.util.List;

public class OdataContextVariables {

  private String remoteEntityName;
  private String keyNames;
  private List<String> fields;

  public OdataContextVariables(String remoteEntityName, String keyNames, List<String> fields) {
    this.remoteEntityName = remoteEntityName;
    this.keyNames = keyNames;
    this.fields = fields;
  }

  public String getRemoteEntityName() {
    return remoteEntityName;
  }

  public void setRemoteEntityName(String remoteEntityName) {
    this.remoteEntityName = remoteEntityName;
  }

  public String getKeyNames() {
    return keyNames;
  }

  public void setKeyNames(String keyNames) {
    this.keyNames = keyNames;
  }

  public List<String> getFields() {
    return fields;
  }

  public void setFields(List<String> fields) {
    this.fields = fields;
  }
}
