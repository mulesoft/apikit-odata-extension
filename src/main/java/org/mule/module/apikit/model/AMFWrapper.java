/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import static java.lang.String.format;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.GUID;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.INT16;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.INT32;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.INT64;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.INT8;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_KEY_PROPERTY;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_NULLABLE_PROPERTY;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_PRECISION_PROPERTY;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_REMOTE_NAME;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_SCALE_PROPERTY;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.NAMESPACE_TYPE_PROPERTY;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_BINARY;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_BOOLEAN;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_BYTE;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_DATETIME;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_DATETIMEOFFSET;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_DECIMAL;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_DOUBLE;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_GUID;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_INT16;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_INT32;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_INT64;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_SINGLE;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_STRING;
import static org.mule.module.apikit.odata.util.EDMTypeConverter.EDM_TIME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import amf.ProfileNames;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.Document;
import amf.client.parse.Raml10Parser;
import amf.client.validate.ValidationResult;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;

import com.google.common.base.Strings;

import amf.client.AMF;
import amf.client.model.document.Module;
import amf.client.model.domain.DomainElement;
import amf.client.model.domain.DomainExtension;
import amf.client.model.domain.FileShape;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.ScalarNode;
import amf.client.model.domain.ScalarShape;
import amf.client.model.domain.Shape;
import amf.client.model.domain.UnionShape;
import amf.plugins.features.validation.AMFValidatorPlugin;

public class AMFWrapper {
  public static final String AMF_STRING = "http://www.w3.org/2001/XMLSchema#string";
  public static final String AMF_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
  public static final String AMF_NUMBER = "http://a.ml/vocabularies/shapes#number";
  public static final String AMF_FLOAT = "http://www.w3.org/2001/XMLSchema#float";
  public static final String AMF_DATE_TIME_ONLY = "http://a.ml/vocabularies/shapes#dateTimeOnly";
  public static final String AMF_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
  public static final String AMF_TIME = "http://www.w3.org/2001/XMLSchema#time";
  public static final String AMF_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
  public static final String AMF_DATE_ONLY = "http://www.w3.org/2001/XMLSchema#date";
  public static final String AMF_LONG = "http://www.w3.org/2001/XMLSchema#long";

  static {
    try {
      AMF.init().get();
      AMFValidatorPlugin.withEnabledValidation(true);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private List<NodeShape> nodeShapesList;
  private Map<String, NodeShape> shapes = new HashMap<String, NodeShape>();
  private EntityDefinitionSet entityDefinitionSet = new EntityDefinitionSet();

  public AMFWrapper(String ramlPath) throws OdataMetadataFormatException, OdataMetadataFieldsException {
    try {
      initNodeShapesList(ramlPath);
    } catch (ExecutionException | InterruptedException e) {
      throw new OdataMetadataFormatException("RAML is invalid. See log list.");
    }

    for (NodeShape nodeShape : nodeShapesList) {
      shapes.put(nodeShape.name().value(), nodeShape);
      entityDefinitionSet.addEntity(buildEntityDefinition(nodeShape));
    }
  }

  private void validateOdataRaml(Raml10Parser parser) throws InterruptedException, ExecutionException, OdataMetadataFieldsException {
    List<ValidationResult> validationResults = parser.reportValidation(ProfileNames.RAML()).get().results();
    if (validationResults.isEmpty()) {
      return;
    }
    String errorMessage = "Parse of odata.raml file failed: ";
    for (ValidationResult validationResult : validationResults) {
      errorMessage = errorMessage.concat(validationResult.message() + "\n");
    }
    throw new OdataMetadataFieldsException(errorMessage);
  }

  private void initNodeShapesList(String ramlPath) throws ExecutionException, InterruptedException, OdataMetadataFieldsException, OdataMetadataFormatException {
    nodeShapesList = new ArrayList<>();

    Raml10Parser parser = AMF.raml10Parser();
    Object object = parser.parseFileAsync(ramlPath).get();
    validateOdataRaml(parser);

    if (object instanceof Module) { // If the parsed file is a module it comes from scaffolding odata.raml
      addNodeShapes((Module) object, true); //in this case we should throw an exception if remote name is missing
    } else if (object instanceof Document) {
      Document document = (Document) object;
      for (BaseUnit baseUnit : document.references()) { // If the parsed file is a document it is the initialization from the apikit based on the api.raml
        if (baseUnit instanceof Module)
          addNodeShapes((Module) baseUnit, false); // In order to allow the user to add other types at the api.raml we shouldn't throw an exception if remote name is missing
      }
    }

    if (nodeShapesList.isEmpty()) {
      throw new OdataMetadataFieldsException("odata.raml must declare at least one type");
    }
  }


  private void addNodeShapes(Module module, boolean throwException) throws OdataMetadataFormatException, OdataMetadataFieldsException {
    for (DomainElement domainElement : module.declares()) {
      if (domainElement instanceof Shape) {
        String remoteName = getAnnotation((Shape) domainElement, NAMESPACE_REMOTE_NAME);
        if (domainElement instanceof NodeShape) {
          NodeShape nodeShape = (NodeShape) domainElement;
          if (remoteName != null)
            nodeShapesList.add(nodeShape);
          else if (throwException)
            throw new OdataMetadataFieldsException("Property \"remote name\" is missing in entity " + nodeShape.name());
        } else if (remoteName != null) {
          throw new OdataMetadataFormatException("Type not supported. " + remoteName);
        }
      }
    }
  }

  public EntityDefinitionSet getSchemas() {
    return entityDefinitionSet;
  }

  private EntityDefinition buildEntityDefinition(NodeShape nodeShape) throws OdataMetadataFormatException, OdataMetadataFieldsException {

    if (nodeShape.properties().isEmpty()) {
      throw new OdataMetadataFormatException("No schemas found.");
    }

    // set entity properties
    String entityName = nodeShape.name().value();
    String remoteName = getAnnotation(nodeShape, NAMESPACE_REMOTE_NAME);
    EntityDefinition entityDefinition = new EntityDefinition(entityName, remoteName);

    for (PropertyShape propertyShape : nodeShape.properties()) {
      //Handle unions
      final Shape shape = getScalarShape(propertyShape.range());
      final String propertyName = propertyShape.name().value();

      notNull("Property \"name\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", entityName);
      notNull("Property \"remote name\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", remoteName);

      EntityDefinitionProperty entityDefinitionProperty = null;

      final String key = getAnnotation(shape, NAMESPACE_KEY_PROPERTY);
      notNull("Property \"key\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", key);
      final boolean isKey = Boolean.valueOf(key);

      final String nullable = getAnnotation(shape, NAMESPACE_NULLABLE_PROPERTY);
      notNull("Property \"nullable\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", nullable);
      final boolean isNullable = Boolean.valueOf(nullable);

      final String defaultValue = (propertyShape.defaultValue() != null ? propertyShape.defaultValue().name().value() : null);
      if (shape instanceof ScalarShape) {
        final ScalarShape scalarShape = (ScalarShape) shape;

        final String type = getOdataType(scalarShape);
        notNull("Property \"type\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", type);

        String maxLength = null;
        if (EDM_STRING.equals(type)) {

          Integer maxLengthInt = scalarShape.maxLength().value();
          maxLength = maxLengthInt != 0 ? String.valueOf(maxLengthInt) : null;
        }


        final String precision = getAnnotation(scalarShape, NAMESPACE_PRECISION_PROPERTY);
        final String scale = getAnnotation(scalarShape, NAMESPACE_SCALE_PROPERTY);

        entityDefinitionProperty = new EntityDefinitionProperty(propertyName, type, isNullable, isKey, defaultValue, maxLength, false, null, false, precision, scale);
      } else if (shape instanceof FileShape) {
        entityDefinitionProperty = new EntityDefinitionProperty(propertyName, EDM_BINARY, isNullable, isKey, defaultValue, null, false, null, false, null, null);
      } else {
        throw new OdataMetadataFieldsException("Type not supported of property " + propertyName);
      }

      entityDefinition.addProperty(entityDefinitionProperty);
      if (!entityDefinition.hasPrimaryKey() && entityDefinitionProperty.isKey()) {
        entityDefinition.setHasPrimaryKey(true);
      }
    }

    if (!entityDefinition.hasPrimaryKey()) {
      throw new OdataMetadataFieldsException("Entity defition must have a primary key.");
    }

    return entityDefinition;
  }

  private String getOdataType(ScalarShape scalarShape) throws OdataMetadataFieldsException {
    String dataType = scalarShape.dataType().value();

    if (dataType.equals(AMF_BOOLEAN)) {
      return EDM_BOOLEAN;
    }
    if (dataType.equals(AMF_STRING)) {
      return getStringType(scalarShape);
    }
    if (dataType.equals(AMF_FLOAT)) {
      return EDM_SINGLE;
    }
    if (dataType.equals(AMF_DATE_TIME_ONLY)) {
      return EDM_DATETIME;
    }
    if (dataType.equals(AMF_NUMBER) || dataType.equals(AMF_INTEGER) || dataType.equals(AMF_LONG)) {
      return getNumberType(scalarShape);
    }
    if (dataType.equals(AMF_TIME)) {
      return EDM_TIME;
    }
    if (dataType.equals(AMF_DATE_TIME)) {
      return EDM_DATETIME;
    }
    if (dataType.equals(AMF_DATE_ONLY)) {
      return EDM_DATETIMEOFFSET;
    }

    throw new UnsupportedOperationException("Type not supported " + dataType + " of property " + scalarShape.name());
  }

  private Shape getScalarShape(Shape shape) throws OdataMetadataFieldsException {
    if (shape instanceof UnionShape) {
      UnionShape unionShape = (UnionShape) shape;
      List<DomainExtension> annotations = shape.customDomainProperties();
      for (Shape unionSubShape : unionShape.anyOf()) {
        if (unionSubShape instanceof ScalarShape) {
          unionSubShape.withCustomDomainProperties(annotations);
          return unionSubShape;
        }
      }

      throw new OdataMetadataFieldsException(format("Property %s cannot be just null.", shape.name()));
    }
    return shape;
  }

  private String getNumberType(ScalarShape scalarShape) throws OdataMetadataFieldsException {
    String format = scalarShape.format().value();

    if (format != null) {
      switch (format) {
        case INT64:
          return EDM_INT64;
        case INT32:
          return EDM_INT32;
        case INT16:
          return EDM_INT16;
        case INT8:
          return EDM_BYTE;
        default:
          throw new OdataMetadataFieldsException(format("Unexpected format %s for number type.", format));
      }
    }

    if (scalarShape.dataType().value().equals(AMF_INTEGER)) {
      return EDM_INT32;
    }

    final String scale = getAnnotation(scalarShape, NAMESPACE_SCALE_PROPERTY);
    final String precision = getAnnotation(scalarShape, NAMESPACE_PRECISION_PROPERTY);
    if (scale != null && precision != null) {
      return EDM_DECIMAL;
    }

    return EDM_DOUBLE;
  }

  private String getStringType(ScalarShape scalarShape) {
    final String subType = getAnnotation(scalarShape, NAMESPACE_TYPE_PROPERTY);

    if (GUID.equals(subType)) {
      return EDM_GUID;
    }

    return EDM_STRING;
  }

  private void notNull(String message, Object actual) throws OdataMetadataFieldsException {
    if (actual == null || Strings.isNullOrEmpty(actual.toString())) {
      throw new OdataMetadataFieldsException(message);
    }
  }

  @Nullable
  private String getAnnotation(Shape nodeShape, String annotationName) {
    for (DomainExtension annotation : nodeShape.customDomainProperties()) {
      if (annotationName.equals(annotation.name().value())) {
        return ((ScalarNode) annotation.extension()).value();
      }
    }

    return null;
  }
}
