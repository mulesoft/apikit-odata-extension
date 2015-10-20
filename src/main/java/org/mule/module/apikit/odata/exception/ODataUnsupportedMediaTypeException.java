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
public class ODataUnsupportedMediaTypeException extends ODataException {

    public ODataUnsupportedMediaTypeException(String message) {
        super(message, HttpConstants.SC_UNSUPPORTED_MEDIA_TYPE);
    }
    
    public ODataUnsupportedMediaTypeException(String message, Throwable cause) {
        super(message, cause, HttpConstants.SC_UNSUPPORTED_MEDIA_TYPE);
    }
}