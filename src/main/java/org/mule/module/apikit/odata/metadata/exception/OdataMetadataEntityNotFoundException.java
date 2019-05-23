/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.exception;

import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.runtime.http.api.HttpConstants.HttpStatus;

/**
 * 
 * @author arielsegura
 */
public class OdataMetadataEntityNotFoundException extends ODataException {

  /**
   * 
   */
  private static final long serialVersionUID = -8973414438805066252L;

  public OdataMetadataEntityNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND.getStatusCode());
  }

}
