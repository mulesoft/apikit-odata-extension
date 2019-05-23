/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.backward.compatibility;

import org.apache.log4j.Logger;
import org.mule.module.apikit.model.AMFWrapper;
import org.mule.module.apikit.odata.context.OdataContextVariables;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

import java.util.ArrayList;
import java.util.List;

import static org.mule.module.apikit.model.Entity.pluralizeName;

public class OdataMetadataManagerImpl implements OdataMetadataManager {

	private static AMFWrapper apiWrapper = null;
	private static EntityDefinitionSet entitySet = null;
	private static final Object lock = new Object();
	private static  Logger logger = Logger.getLogger(OdataMetadataManager.class);


	public OdataMetadataManagerImpl(String ramlPath) throws OdataMetadataFormatException {
		this(ramlPath, false);
	}

	public OdataMetadataManagerImpl(String ramlPath, boolean cleanCache) throws OdataMetadataFormatException {
		if (cleanCache) cleanCaches();

		if (apiWrapper == null) {
			synchronized (lock) {
				if (apiWrapper == null) {
					try {
						logger.info("Initializing Odata Metadata");
						apiWrapper = new AMFWrapper(ramlPath);
						logger.info("Odata Metadata initialized");
					} catch (Exception e) {
						throw new OdataMetadataFormatException(e.getMessage());
					}
				}
			}
		}
	}

	private void cleanCaches() {
		apiWrapper = null;
		entitySet = null;
	}

	@Override
	public EntityDefinitionSet getEntitySet(){
		if (entitySet == null) {
			synchronized (lock) {
				if (entitySet == null)
					entitySet = apiWrapper.getSchemas();
			}
		}
		return entitySet;
	}

	@Override
	public EntityDefinition getEntityByName(String entityName) throws OdataMetadataEntityNotFoundException, OdataMetadataFormatException, OdataMetadataFieldsException {
		for (EntityDefinition entityDefinition : getEntitySet().toList()) {
			final String entityDefinitionName = entityDefinition.getName();
			if (entityDefinitionName.equalsIgnoreCase(entityName) || pluralizeName(entityDefinitionName).equalsIgnoreCase(entityName)) {
				return entityDefinition;
			}
		}
		throw new OdataMetadataEntityNotFoundException("Entity " + entityName + " not found.");
	}

	@Override
	public String[] getEntityKeys(String entityName) throws ODataException {
		return getEntityByName(entityName).getKeys().split(",");
	}

	@Override
	public OdataContextVariables getOdataContextVariables(String entity) throws OdataMetadataEntityNotFoundException, OdataMetadataFormatException, OdataMetadataFieldsException {
		if(entity == null)
			return null;

		EntityDefinition entityDefinition = this.getEntityByName(entity);
		OdataContextVariables odata = new OdataContextVariables(entityDefinition.getRemoteEntity(),entityDefinition.getKeys(), getFieldsAsList(entityDefinition.getProperties()));

		return odata;
	}

	private List<String> getFieldsAsList(List<EntityDefinitionProperty> properties) {
		List<String> ret = new ArrayList<String>();

		for (EntityDefinitionProperty property : properties) {
			ret.add(property.getName());
		}

		return ret;
	}

}
