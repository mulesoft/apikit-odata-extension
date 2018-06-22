/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import org.mule.module.apikit.model.RamlImpl10V2Wrapper;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParserUtils;

import static org.mule.module.apikit.model.Entity.pluralizeName;

import java.util.concurrent.ExecutionException;

import org.mule.module.apikit.model.AMFWrapper;

public class OdataMetadataManager {

	private static AMFWrapper apiWrapper = null;
	private static EntityDefinitionSet entitySet = null;
	private static final Object lock = new Object();

	public OdataMetadataManager(String ramlPath) throws OdataMetadataFormatException {
		this(ramlPath, false);
	}

	public OdataMetadataManager(String ramlPath, boolean cleanCache) throws OdataMetadataFormatException {
		if (cleanCache) cleanCaches();

		if (apiWrapper == null) {
			synchronized (lock) {
				if (apiWrapper == null) {
					try {
						apiWrapper = new AMFWrapper(ramlPath);
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

	public EntityDefinitionSet getEntitySet() throws OdataMetadataFormatException, OdataMetadataFieldsException {
		if (entitySet == null) {
			synchronized (lock) {
				if (entitySet == null)
					entitySet = apiWrapper.getSchemas();
			}
		}
		return entitySet;
	}

	public EntityDefinition getEntityByName(String entityName) throws OdataMetadataEntityNotFoundException, OdataMetadataFormatException, OdataMetadataFieldsException {
		for (EntityDefinition entityDefinition : getEntitySet().toList()) {
			final String entityDefinitionName = entityDefinition.getName();
			if (entityDefinitionName.equalsIgnoreCase(entityName) || pluralizeName(entityDefinitionName).equalsIgnoreCase(entityName)) {
				return entityDefinition;
			}
		}
		throw new OdataMetadataEntityNotFoundException("Entity " + entityName + " not found.");
	}

	public String[] getEntityKeys(String entityName) throws ODataException {
		return getEntityByName(entityName).getKeys().split(",");
	}

}
