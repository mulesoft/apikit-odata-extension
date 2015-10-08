/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.raml.model.Raml;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

public class RamlParserUtils {
	public static Raml getRaml(AbstractConfiguration config) {
		ResourceLoader loader = config.getRamlResourceLoader();
		RamlDocumentBuilder builder = new RamlDocumentBuilder(loader);
		return builder.build(config.getRaml());
	}

	public static Raml getRaml(String ramlPath) {
		RamlDocumentBuilder builder = new RamlDocumentBuilder();
		return builder.build(ramlPath);
	}

	public static Raml getRaml(InputStream ramlInput) {
		RamlDocumentBuilder builder = new RamlDocumentBuilder();
		return builder.build(ramlInput);
	}

	public static Raml getRaml(URL url) throws IOException {
		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		return getRaml(conn.getInputStream());
	}
	
	private static void validateResults(List<ValidationResult> results) throws GatewayMetadataFormatException{
		if (!results.isEmpty()) {
			for (ValidationResult result : results) {
				Logger.getLogger(RamlParser.class).error(result.toString());
			}
			throw new GatewayMetadataFormatException("RAML is invalid. See log list.");
		} else {
			Logger.getLogger(RamlParser.class).info("RAML Validation ok");
		}
	}
	
	public static boolean equalsRaml(Raml one, Raml two) {
		return (one.getVersion().equals(two.getVersion())
				&& one.getUri().equals(two.getUri()) && one.getSchemas()
				.equals(two.getSchemas()));
	}


	public static void validateRaml(String path)  throws GatewayMetadataFormatException{
		List<ValidationResult> results = RamlValidationService.createDefault()
				.validate(path);
		validateResults(results);
	}

	public static void validateRaml(InputStream inputStream)   throws GatewayMetadataFormatException{
		List<ValidationResult> results = RamlValidationService.createDefault()
				.validate(inputStream);
		validateResults(results);
	}
}
