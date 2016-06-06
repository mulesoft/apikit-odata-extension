/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.model.RamlImpl10V2Wrapper;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParser;
import org.mule.module.apikit.odata.metadata.raml.RamlParserUtils;

/**
 * 
 * @author arielsegura
 */
public class OdataMetadataManager {

	private static EntityDefinitionSet entitySet;
	static {
		entitySet = new EntityDefinitionSet();
		lock = new Object();
	}
	private static RamlImpl10V2Wrapper apiWrapper;
	private RamlParser ramlParser;
	private static Object lock;

	public OdataMetadataManager() {
		super();
		ramlParser = new RamlParser();
	}

	/**
	 * This method decides whether update entity set or not
	 * 
	 * @return
	 */
	private boolean update(RamlImpl10V2Wrapper apiWrapper, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataFormatException {
		if (forceUpdate)
			return true;
		if (apiWrapper == null)
			return true;
		if (apiWrapper != null && !RamlParserUtils.equalsRaml(apiWrapper, new RamlImpl10V2Wrapper(apiWrapper.getApi()))) {
			return true;
		}
		if (entitySet == null) {
			return true;
		}
		// TODO Implement cache
		return false;
	}

	private void performRefresh(RamlImpl10V2Wrapper apiWrapper, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {
		synchronized (lock) {
			if (update(apiWrapper, forceUpdate)) {
				apiWrapper = new RamlImpl10V2Wrapper(apiWrapper.getApi());
				entitySet = apiWrapper.getSchemas();
			}
		}
	}

	public EntityDefinitionSet refreshMetadata(AbstractConfiguration newConf, boolean forceUpdate) throws OdataMetadataFieldsException,
			OdataMetadataResourceNotFound, OdataMetadataFormatException {

		performRefresh(RamlParserUtils.getRaml(newConf), forceUpdate);

		return entitySet;

	}

	public EntityDefinitionSet refreshMetadata(String ramlPath, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {

		performRefresh(RamlParserUtils.getRaml(ramlPath), forceUpdate);

		return entitySet;
	}

	public EntityDefinitionSet refreshMetadata(RamlImpl10V2Wrapper apiWrapper, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {

		performRefresh(apiWrapper, forceUpdate);
		return entitySet;
	}

	public EntityDefinitionSet getEntitySet() throws OdataMetadataFieldsException, OdataMetadataResourceNotFound, OdataMetadataFormatException {
		synchronized (lock) {
			return entitySet;
		}
	}

	public EntityDefinition getEntityByName(String entityName) throws OdataMetadataEntityNotFoundException, OdataMetadataFieldsException,
			OdataMetadataResourceNotFound, OdataMetadataFormatException {
		synchronized (lock) {
			for (EntityDefinition entityDefinition : entitySet.toList()) {
				if (entityDefinition.getName().equalsIgnoreCase(entityName)) {
					return entityDefinition;
				}
			}
			throw new OdataMetadataEntityNotFoundException("Entity " + entityName + " not found.");
		}
	}

	public String[] getEntityKeys(String entityName) throws ODataException {
		return getEntityByName(entityName).getKeys().split(",");
	}

}
