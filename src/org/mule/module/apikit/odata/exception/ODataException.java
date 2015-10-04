/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.exception;

public class ODataException extends Exception
{
	private static final long serialVersionUID = 2615177524761296514L;
	private int httpStatus = 400;

	public ODataException(String message, int httpStatus)
	{
		super(message);
		this.httpStatus = httpStatus;
	}

	public ODataException(Throwable cause, int httpStatus)
	{
		super(cause);
		this.httpStatus = httpStatus;
	}

	public ODataException(int httpStatus)
	{
		super();
		this.httpStatus = httpStatus;
	}
	
	public int getHttpStatus()
	{
		return httpStatus;
	}
	
}
