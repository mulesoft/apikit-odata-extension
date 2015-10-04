/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.metadata.exception;

import org.mule.module.apikit.odata.exception.ODataException;

/**
 * 
 * @author arielsegura
 */
public class GatewayMetadataResourceNotFound extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7839109007316280213L;

	public GatewayMetadataResourceNotFound(String message) {
		super(message, 404);
	}
}
