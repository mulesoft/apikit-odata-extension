/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.exception;

import org.mule.transport.http.HttpConstants;

/**
 * Created by arielsegura on 10/1/15.
 */
public class ODataInternalServerErrorException extends ODataException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5891700784326574919L;

	public ODataInternalServerErrorException(String message) {
        super(message, HttpConstants.SC_INTERNAL_SERVER_ERROR);
    }
    public ODataInternalServerErrorException(Exception ex) {
        super(ex.getMessage(), ex, HttpConstants.SC_INTERNAL_SERVER_ERROR);
    }
}