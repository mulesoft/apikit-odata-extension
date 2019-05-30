/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import org.apache.log4j.Logger;
import org.mule.module.apikit.model.AMFWrapper;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import java.net.URLDecoder;

public class OdataMetadataManagerImpl extends OdataMetadataManager {
  private final EntityDefinitionSet entitySet;
  private static Logger logger = Logger.getLogger(OdataMetadataManagerImpl.class);

  public OdataMetadataManagerImpl(String ramlPath) throws OdataMetadataFormatException {
    logger.info("Initializing Odata Metadata");
    try {
      AMFWrapper apiWrapper = new AMFWrapper(URLDecoder.decode(ramlPath));
      entitySet = apiWrapper.getSchemas();
    } catch (OdataMetadataFieldsException e) {
      throw new OdataMetadataFormatException(e.getMessage());
    }
    logger.info("Odata Metadata initialized");
  }

  @Override
  public EntityDefinitionSet getEntitySet() {
    return entitySet;
  }
}
