/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.metadata.raml;

import org.mule.module.apikit.model.AMFWrapper;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import static java.lang.String.format;

public class RamlParser {

  public static final String NAMESPACE = "odata";

  public static final String REMOTE_NAME = "remote";
  public static final String TYPE_PROPERTY = "type";
  public static final String NULLABLE_PROPERTY = "nullable";
  public static final String KEY_PROPERTY = "key";
  public static final String PRECISION_PROPERTY = "precision";
  public static final String SCALE_PROPERTY = "scale";

  public static final String NAMESPACE_REMOTE_NAME = format("%s.%s", NAMESPACE, REMOTE_NAME);
  public static final String NAMESPACE_NULLABLE_PROPERTY =
      format("%s.%s", NAMESPACE, NULLABLE_PROPERTY);
  public static final String NAMESPACE_KEY_PROPERTY = format("%s.%s", NAMESPACE, KEY_PROPERTY);
  public static final String NAMESPACE_PRECISION_PROPERTY =
      format("%s.%s", NAMESPACE, PRECISION_PROPERTY);
  public static final String NAMESPACE_SCALE_PROPERTY = format("%s.%s", NAMESPACE, SCALE_PROPERTY);
  public static final String NAMESPACE_TYPE_PROPERTY = format("%s.%s", NAMESPACE, TYPE_PROPERTY);

  public static final String INT64 = "int64";
  public static final String INT32 = "int32";
  public static final String INT16 = "int16";
  public static final String INT8 = "int8";
  public static final String FLOAT = "float";

  public static final String GUID = "guid";


  public EntityDefinitionSet getEntitiesFromRaml(String path)
      throws OdataMetadataFieldsException, OdataMetadataFormatException {
    AMFWrapper amfWrapper;

    amfWrapper = new AMFWrapper(path);

    return getEntitiesFromRaml(amfWrapper);
  }

  public EntityDefinitionSet getEntitiesFromRaml(AMFWrapper apiWrapper) {
    return (apiWrapper.getSchemas());
  }

}
