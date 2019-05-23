/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata;

import org.mule.module.apikit.odata.context.OdataContextVariables;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

public interface OdataMetadataManager {
  EntityDefinitionSet getEntitySet();

  EntityDefinition getEntityByName(String entityName) throws OdataMetadataEntityNotFoundException, OdataMetadataFormatException, OdataMetadataFieldsException;

  String[] getEntityKeys(String entityName) throws ODataException;

  OdataContextVariables getOdataContextVariables(String entity) throws OdataMetadataEntityNotFoundException, OdataMetadataFormatException, OdataMetadataFieldsException;
}
