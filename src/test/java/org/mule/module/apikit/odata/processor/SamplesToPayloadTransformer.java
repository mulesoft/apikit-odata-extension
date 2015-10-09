/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class SamplesToPayloadTransformer implements Callable {
	public Object onCall(MuleEventContext eventContext) throws Exception {
		Object payload = eventContext.getMessage().getInboundProperty("http.request.path");

		String fileName = payload.toString().replace("/", "_");
		fileName = fileName.replaceAll("\\?[^\\?]+$", "");

		String fileContent = readFile("/org/mule/module/apikit/odata/processor/" + fileName + ".json");

		return fileContent;
	}

	static String readFile(String path) throws IOException, URISyntaxException {
		URL resource = SamplesToPayloadTransformer.class.getResource(path);

		byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}