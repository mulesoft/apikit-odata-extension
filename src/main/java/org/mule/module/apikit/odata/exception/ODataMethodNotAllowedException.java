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
public class ODataMethodNotAllowedException extends ODataException {

  public ODataMethodNotAllowedException(String method) {
    super("Method not allowed. Try with " + method, HttpStatus.METHOD_NOT_ALLOWED.getStatusCode());
  }

  public ODataMethodNotAllowedException() {
    super("Method not allowed. ", HttpStatus.METHOD_NOT_ALLOWED.getStatusCode());
  }
}
