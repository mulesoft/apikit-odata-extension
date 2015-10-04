/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.exception;

public class ODataInvalidFormatException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2615177524761296514L;

	public ODataInvalidFormatException(String message) {
		super(message, 415);
	}

}
