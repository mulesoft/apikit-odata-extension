/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.exception;


public class ODataException extends Exception {
  private static final long serialVersionUID = 2615177524761296514L;
  private int httpStatus;

  public ODataException(String message, int httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public ODataException(String message, Throwable cause, int httpStatus) {
    super(message, cause);
    this.httpStatus = httpStatus;
  }

  public ODataException(int httpStatus) {
    super();
    this.httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

}
