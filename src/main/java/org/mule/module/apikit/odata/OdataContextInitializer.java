/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;

public class OdataContextInitializer {

	private static final Logger logger = Logger.getLogger(OdataContextInitializer.class);

	public OdataContext initializeContext(String ramlPath,String method) throws OdataMetadataFieldsException,
			OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
		
		final OdataMetadataManager odataMetadataManager = new OdataMetadataManager(ramlPath);

		return new OdataContext(odataMetadataManager, method);
	}

	private List<String> getFieldsAsList(List<EntityDefinitionProperty> properties) {
		List<String> ret = new ArrayList<String>();

		for (EntityDefinitionProperty property : properties) {
			Logger.getLogger(getClass()).debug("Adding field of \n" + property.toString());
			ret.add(property.getName());
		}

		return ret;
	}
}
