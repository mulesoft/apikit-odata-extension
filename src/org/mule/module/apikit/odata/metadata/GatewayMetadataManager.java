/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParser;
import org.mule.module.apikit.odata.metadata.raml.RamlParserUtils;
import org.raml.model.Raml;

/**
 * 
 * @author arielsegura
 */
public class GatewayMetadataManager {

	private static EntityDefinitionSet entitySet;
	static {
		entitySet = new EntityDefinitionSet();
		lock = new Object();
	}
	private static Raml raml;
	private RamlParser ramlParser;
	private static Object lock;

	public GatewayMetadataManager() {
		super();
		ramlParser = new RamlParser();
	}

	

	/**
	 * This method decides whether update entity set or not
	 * 
	 * @return
	 */
	private boolean update(Raml newRaml, boolean forceUpdate) {
		if(forceUpdate) return true;
		if(raml == null) return true;
		if (newRaml != null && !RamlParserUtils.equalsRaml(raml, newRaml)) {
			return true;
		}
		if (entitySet == null) {
			return true;
		}
		// TODO Implement cache
		return false;
	}

	private void performRefresh(Raml newRaml, boolean forceUpdate)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		synchronized (lock) {
			if (update(newRaml, forceUpdate)) {
				entitySet = ramlParser.getEntitiesFromRaml(newRaml);
				raml = newRaml;
			}
		}
	}

	public EntityDefinitionSet refreshMetadata(AbstractConfiguration newConf,
			boolean forceUpdate) throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {

		performRefresh(RamlParserUtils.getRaml(newConf), forceUpdate);

		return entitySet;

	}

	public EntityDefinitionSet refreshMetadata(String ramlPath,
			boolean forceUpdate) throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {

		performRefresh(RamlParserUtils.getRaml(ramlPath), forceUpdate);

		return entitySet;
	}

	public EntityDefinitionSet refreshMetadata(InputStream raml,
			boolean forceUpdate) throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		performRefresh(RamlParserUtils.getRaml(raml), forceUpdate);
		return entitySet;
	}

	public EntityDefinitionSet refreshMetadata(URL url, boolean forceUpdate)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException,
			IOException {
		performRefresh(RamlParserUtils.getRaml(url), forceUpdate);
		return entitySet;
	}

	public EntityDefinitionSet refreshMetadata(Raml raml, boolean forceUpdate)
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {

		performRefresh(raml, forceUpdate);
		return entitySet;
	}

	public EntityDefinitionSet getEntitySet()
			throws GatewayMetadataFieldsException,
			GatewayMetadataResourceNotFound, GatewayMetadataFormatException {
		synchronized (lock) {
			return entitySet;
		}
	}

	public EntityDefinition getEntityByName(String entityName)
			throws GatewayMetadataEntityNotFoundException,
			GatewayMetadataFieldsException, GatewayMetadataResourceNotFound,
			GatewayMetadataFormatException {
		synchronized (lock) {
			for (EntityDefinition EntityDefinition : entitySet.toList()) {
				if (EntityDefinition.getName().equalsIgnoreCase(entityName)) {
					return EntityDefinition;
				}
			}
			throw new GatewayMetadataEntityNotFoundException("Entity "
					+ entityName + " not found.");
		}
	}

	public String[] getEntityKeys(String entityName) throws ODataException {
		return getEntityByName(entityName).getKeys().split(",");
	}

}
