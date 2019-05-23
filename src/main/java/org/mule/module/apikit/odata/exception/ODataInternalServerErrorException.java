/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.exception;

import org.mule.runtime.http.api.HttpConstants.HttpStatus;

/**
 * Created by arielsegura on 10/1/15.
 */
public class ODataInternalServerErrorException extends ODataException {

  /**
   * 
   */
  private static final long serialVersionUID = 5891700784326574919L;

  public ODataInternalServerErrorException(String message) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode());
  }

  public ODataInternalServerErrorException(Exception ex) {
    super(ex.getMessage(), ex, HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode());
  }
}
