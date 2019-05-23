/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.mule.module.apikit.model.exception.InvalidModelException;

public class Property {

  private String name;
  private String type;
  private String nullable;
  private String key;

  public Property(String name) throws InvalidModelException {
    setName(name);
  }

  private void setName(String name) throws InvalidModelException {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) throws InvalidModelException {
    this.type = type;
  }

  public String getNullable() {
    return nullable;
  }

  public void setNullable(String nullable) throws InvalidModelException {
    this.nullable = nullable;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) throws InvalidModelException {
    if (!isValid(key))
      throw new InvalidModelException();
    this.key = key;
  }

  public boolean isValid() throws InvalidModelException {
    if (!isValid(name))
      throw new InvalidModelException("a field name is missing");
    if (!isValid(type))
      throw new InvalidModelException(
          "the field '" + name + "' is missing the 'type' required property");
    if (!isValid(nullable))
      throw new InvalidModelException(
          "the field '" + name + "' is missing the 'nullable' required property");
    if (!isValid(key))
      throw new InvalidModelException(
          "the field '" + name + "' is missing the 'type' required property");
    return true;
  }

  private boolean isValid(String value) {
    if (value == null || value.isEmpty())
      return false;
    return true;
  }
}
