/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.model.RamlImpl10V2Wrapper;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.v10.api.Library;

public class RamlParserUtils {
	public static RamlImpl10V2Wrapper getRaml(AbstractConfiguration config) throws OdataMetadataFormatException {
		return getRaml(config.getRaml());
	}

	public static RamlImpl10V2Wrapper getRaml(String ramlPath) throws OdataMetadataFormatException {
		RamlModelResult ramlModelResult = getRamlModelResult(ramlPath);
		return new RamlImpl10V2Wrapper(ramlModelResult);
	}

	private static RamlModelResult getRamlModelResult(String ramlPath) throws OdataMetadataFormatException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
		if(ramlModelResult.hasErrors()){
			throw new OdataMetadataFormatException(ramlModelResult.getValidationResults().get(0).getMessage());
		}
		return ramlModelResult;
	}

	public static boolean equalsRaml(RamlImpl10V2Wrapper one, RamlImpl10V2Wrapper two) throws OdataMetadataFieldsException, OdataMetadataFormatException {
		return (one.getApi().ramlVersion().equals(two.getApi().ramlVersion()) && one.getSchemas().equals(two.getSchemas()));
	}

	public static Library getLibrary(String pathToModel) throws OdataMetadataFormatException {
		RamlModelResult ramlModelResult = getRamlModelResult(pathToModel);
		return ramlModelResult.getLibrary();
	}
}
