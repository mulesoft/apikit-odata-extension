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
public class GatewayMetadataEntityNotFoundException extends ODataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8973414438805066252L;

	public GatewayMetadataEntityNotFoundException(String message) {
		super(message, 404);
	}

}
