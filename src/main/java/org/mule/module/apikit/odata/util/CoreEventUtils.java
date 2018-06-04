/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.core.api.event.CoreEvent;

public class CoreEventUtils {
	
	public static HttpRequestAttributes getHttpRequestAttributes(CoreEvent event) {
		
		return  (HttpRequestAttributes) event.getMessage().getAttributes().getValue();
	}

	
	public static HttpResponseAttributes getHttpResponseAttributes(CoreEvent event) {
		
		return  (HttpResponseAttributes) event.getMessage().getAttributes().getValue();
	}
	



	public static String getPayloadAsString(CoreEvent event) {
		
		return event.getMessage().getPayload().getValue().toString();
	}
}
