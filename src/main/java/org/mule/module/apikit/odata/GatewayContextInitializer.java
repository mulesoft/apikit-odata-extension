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
import org.mule.api.MuleEvent;
import org.mule.api.transport.PropertyScope;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.HttpRestRequest;
import org.mule.module.apikit.odata.metadata.GatewayMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.util.ODataUriHelper;

public class GatewayContextInitializer {
	private GatewayMetadataManager gwMetadataManager;

	public GatewayMetadataManager initializeContext(MuleEvent event, Configuration config) throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException, GatewayMetadataEntityNotFoundException {
		this.gwMetadataManager = new GatewayMetadataManager();
		this.gwMetadataManager.refreshMetadata(config, Boolean.valueOf(String.valueOf(event.getMessage().getInboundProperty("force-update"))));

		HttpRestRequest request = new HttpRestRequest(event, config);

		String path = request.getResourcePath();

		// parse request
		Logger.getLogger(getClass()).info(path);
		String entityName = ODataUriHelper.parseEntity(path);
		Logger.getLogger(getClass()).info("Requesting entity " + entityName);
		if ("console".equalsIgnoreCase(entityName)) {
			Logger.getLogger(getClass()).info("Skipping console calls.");
			return gwMetadataManager;
		}
		if ("gw".equalsIgnoreCase(entityName)) {
			Logger.getLogger(getClass()).info("Skipping admin calls.");
			return gwMetadataManager;
		}
		if (entityName.isEmpty()) {
			Logger.getLogger(getClass()).info("Unknown entity call.");
			return gwMetadataManager;
		}
		EntityDefinition entity = gwMetadataManager.getEntityByName(entityName);
		event.getMessage().setProperty("odata.remoteEntityName", entity.getRemoteEntity(), PropertyScope.INBOUND);
		event.getMessage().setProperty("odata.keyNames", entity.getKeys(), PropertyScope.INBOUND);
		event.getMessage().setProperty("odata.fields", getFieldsAsList(entity.getProperties()), PropertyScope.INBOUND);

		return gwMetadataManager;
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
