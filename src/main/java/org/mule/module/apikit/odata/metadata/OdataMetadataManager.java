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
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

import java.util.ArrayList;
import java.util.List;

import static org.mule.module.apikit.model.Entity.pluralizeName;

public abstract class OdataMetadataManager {

  public abstract EntityDefinitionSet getEntitySet();

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
