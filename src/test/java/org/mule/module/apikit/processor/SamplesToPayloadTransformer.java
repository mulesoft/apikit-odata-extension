/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.processor;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SamplesToPayloadTransformer{

	public static String getJson(String  requestPath) throws Exception {

		String fileName = requestPath.replace("/", "_");
		fileName = fileName.replaceAll("\\?[^\\?]+$", "");

		String fileContent = readFile( "/org/mule/module/apikit/odata/processor/" + fileName + ".json");

		return fileContent;
	}

	static String readFile(String path) throws IOException, URISyntaxException {
		URL resource = SamplesToPayloadTransformer.class.getResource(path);

		byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}