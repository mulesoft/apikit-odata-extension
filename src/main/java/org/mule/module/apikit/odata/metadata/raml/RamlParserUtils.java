/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.parser.ParserWrapper;
import org.mule.module.apikit.parser.ParserWrapperV2;
import org.mule.raml.interfaces.model.IRaml;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlValidationService;

public class RamlParserUtils {
	public static IRaml getRaml(AbstractConfiguration config) {
		return config.getApi();
	}

	public static IRaml getRaml(String ramlPath) {
		ParserWrapper parser = new ParserWrapperV2(ramlPath, null);
		return parser.build();
	}

	private static void validateResults(List<ValidationResult> results) throws OdataMetadataFormatException {
		if (!results.isEmpty()) {
			for (ValidationResult result : results) {
				Logger.getLogger(RamlParser.class).error(result.toString());
			}
			throw new OdataMetadataFormatException("RAML is invalid. See log list.");
		} else {
			Logger.getLogger(RamlParser.class).info("RAML Validation ok");
		}
	}

	public static boolean equalsRaml(IRaml one, IRaml two) {
		return (one.getVersion().equals(two.getVersion()) && one.getUri().equals(two.getUri()) && one.getSchemas().equals(two.getSchemas()));
	}

	public static void validateRaml(String path) throws OdataMetadataFormatException {
		List<ValidationResult> results = RamlValidationService.createDefault().validate(path);
		validateResults(results);
	}

	public static void validateRaml(InputStream inputStream) throws OdataMetadataFormatException {
		List<ValidationResult> results = RamlValidationService.createDefault().validate(inputStream);
		validateResults(results);
	}
}
