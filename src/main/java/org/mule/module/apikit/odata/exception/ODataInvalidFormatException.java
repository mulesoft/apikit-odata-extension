/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
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

	public ODataInvalidFormatException(String message, Throwable cause) {
		super(message, cause, 415);
	}

}
