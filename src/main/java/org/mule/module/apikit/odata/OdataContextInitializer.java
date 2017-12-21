/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.apache.log4j.Logger;
import org.mule.api.MuleEvent;
import org.mule.api.transport.PropertyScope;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.HttpRestRequest;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.util.ODataUriHelper;

import java.util.ArrayList;
import java.util.List;

public class OdataContextInitializer {

	private static final Logger logger = Logger.getLogger(OdataContextInitializer.class);

	public OdataContext initializeContext(MuleEvent event, Configuration config) throws OdataMetadataFieldsException,
			OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
		final OdataMetadataManager odataMetadataManager = new OdataMetadataManager(config.getRaml());

		final HttpRestRequest request = new HttpRestRequest(event, config);
		final String method = request.getMethod();

		final String path = request.getResourcePath();
		final OdataContext odataContext = new OdataContext(odataMetadataManager, method);

		// parse request
		logger.info(path);
		final String entityName = ODataUriHelper.parseEntity(path);
		logger.info("Requesting entity " + entityName);
		if ("console".equalsIgnoreCase(entityName)) {
			logger.info("Skipping console calls.");
			return odataContext;
		}
		if ("gw".equalsIgnoreCase(entityName)) {
			logger.info("Skipping admin calls.");
			return odataContext;
		}
		if (entityName.isEmpty()) {
			logger.info("Unknown entity call.");
			return odataContext;
		}
		final EntityDefinition entity = odataMetadataManager.getEntityByName(entityName);
		event.getMessage().setProperty("odata.remoteEntityName", entity.getRemoteEntity(), PropertyScope.INBOUND);
		event.getMessage().setProperty("odata.keyNames", entity.getKeys(), PropertyScope.INBOUND);
		event.getMessage().setProperty("odata.fields", getFieldsAsList(entity.getProperties()), PropertyScope.INBOUND);

		return odataContext;
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
