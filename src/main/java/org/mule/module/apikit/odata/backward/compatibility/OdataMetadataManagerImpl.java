/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.backward.compatibility;

import org.apache.log4j.Logger;
import org.mule.module.apikit.model.AMFWrapper;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

/**
 * Singleton implementation only for backward compatibility purpose.
 *
 * @deprecated
 * @see OdataMetadataManager
 */
@Deprecated
public class OdataMetadataManagerImpl extends OdataMetadataManager {
	private static EntityDefinitionSet entitySet = null;
	private static final Object lock = new Object();
	private static  Logger logger = Logger.getLogger(OdataMetadataManager.class);


	public OdataMetadataManagerImpl(String ramlPath) throws OdataMetadataFormatException {
		this(ramlPath, false);
	}

	public OdataMetadataManagerImpl(String ramlPath, boolean cleanCache) throws OdataMetadataFormatException {
		if (cleanCache) cleanCaches();

		if (entitySet == null) {
			synchronized (lock) {
				if (entitySet == null) {
					try {
						logger.info("Initializing Odata Metadata");
						AMFWrapper apiWrapper = new AMFWrapper(ramlPath);
						entitySet = apiWrapper.getSchemas();
						logger.info("Odata Metadata initialized");
					} catch (Exception e) {
						throw new OdataMetadataFormatException(e.getMessage());
					}
				}
			}
		}
	}

	private void cleanCaches() {
		entitySet = null;
	}

	@Override
	public EntityDefinitionSet getEntitySet(){
		return entitySet;
	}
}
