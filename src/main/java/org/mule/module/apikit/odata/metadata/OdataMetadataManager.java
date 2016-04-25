/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParser;
import org.mule.module.apikit.odata.metadata.raml.RamlParserUtils;
import org.mule.raml.interfaces.model.IRaml;

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
	private static IRaml raml;
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
	private boolean update(IRaml newRaml, boolean forceUpdate) {
		if (forceUpdate)
			return true;
		if (raml == null)
			return true;
		if (newRaml != null && !RamlParserUtils.equalsRaml(raml, newRaml)) {
			return true;
		}
		if (entitySet == null) {
			return true;
		}
		// TODO Implement cache
		return false;
	}

	private void performRefresh(IRaml newRaml, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {
		synchronized (lock) {
			if (update(newRaml, forceUpdate)) {
				entitySet = ramlParser.getEntitiesFromRaml(newRaml);
				raml = newRaml;
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

	public EntityDefinitionSet refreshMetadata(IRaml raml, boolean forceUpdate) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException {

		performRefresh(raml, forceUpdate);
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
			for (EntityDefinition EntityDefinition : entitySet.toList()) {
				if (EntityDefinition.getName().equalsIgnoreCase(entityName)) {
					return EntityDefinition;
				}
			}
			throw new OdataMetadataEntityNotFoundException("Entity " + entityName + " not found.");
		}
	}

	public String[] getEntityKeys(String entityName) throws ODataException {
		return getEntityByName(entityName).getKeys().split(",");
	}

}
