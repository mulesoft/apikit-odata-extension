/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParser;
import org.mule.module.apikit.odata.util.EDMTypeConverter;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.*;
import org.raml.v2.api.model.v10.declarations.AnnotationRef;

import java.util.List;

/**
 * Created by arielsegura on 6/6/16.
 */
public class RamlImpl10V2Wrapper {
    Api api;
    public RamlImpl10V2Wrapper(Api api) {
        this.api = api;
    }

    public EntityDefinitionSet getSchemas() throws OdataMetadataFieldsException, OdataMetadataFormatException {

        EntityDefinitionSet entityDefinitionSet = new EntityDefinitionSet();

        List<TypeDeclaration> types = api.types();

        if(types.isEmpty()) {
            types = api.schemas();
        }

        for (TypeDeclaration typeDeclaration : types) {
            if(typeDeclaration instanceof ObjectTypeDeclaration){
                ObjectTypeDeclaration objectTypeDeclaration = (ObjectTypeDeclaration) typeDeclaration;
                // set entity properties
                String entityName = objectTypeDeclaration.name();
                String remoteName = getRemoteName(objectTypeDeclaration);
                EntityDefinition entityDefinition = new EntityDefinition(entityName, remoteName);

                if(objectTypeDeclaration.properties().isEmpty()){
                    throw new OdataMetadataFormatException("No schemas found.");
                }
                for(TypeDeclaration propertyTypeDeclaration : objectTypeDeclaration.properties()){
                    String propertyName = propertyTypeDeclaration.name();
                    String type = null;
                    String defaultValue = propertyTypeDeclaration.defaultValue();
                    String maxLength = null;
                    String collation = null;
                    String precision = null;
                    String scale = null;
                    Boolean nullable = null;
                    Boolean key = null;
                    Boolean fixedLength = false;
                    Boolean unicode = false;
                    for(AnnotationRef annotationRef : propertyTypeDeclaration.annotations()){
                        if(RamlParser.TYPE_PROPERTY.equals(annotationRef.name())){
                            type = annotationRef.structuredValue().value().toString();
                        } else if(RamlParser.NAMESPACE_KEY_PROPERTY.equals(annotationRef.name())){
                            key = Boolean.valueOf(annotationRef.structuredValue().value().toString());
                        } else if(RamlParser.NAMESPACE_NULLABLE_PROPERTY.equals(annotationRef.name())){
                            nullable = Boolean.valueOf(annotationRef.structuredValue().value().toString());
                        } else if(RamlParser.NAMESPACE_PRECISION_PROPERTY.equals(annotationRef.name())){
                            precision = annotationRef.structuredValue().value().toString();
                        } else if(RamlParser.NAMESPACE_SCALE_PROPERTY.equals(annotationRef.name())){
                            scale = annotationRef.structuredValue().value().toString();
                        }
                    }

                    if(propertyTypeDeclaration instanceof IntegerTypeDeclaration){
                        IntegerTypeDeclaration castedType = (IntegerTypeDeclaration) propertyTypeDeclaration;
                        castedType.maximum();

                        String format = castedType.format();
                        if(RamlParser.INT64.equals(format)){
                            type = EDMTypeConverter.EDM_INT64;
                        } else if(RamlParser.INT16.equals(format)){
                            type = EDMTypeConverter.EDM_INT16;
                        } else if(RamlParser.INT8.equals(format)){
                            type = EDMTypeConverter.EDM_BYTE;
                        } else {
                            type = EDMTypeConverter.EDM_INT32;
                        }

                    } else if(propertyTypeDeclaration instanceof BooleanTypeDeclaration){
                        BooleanTypeDeclaration castedType = (BooleanTypeDeclaration) propertyTypeDeclaration;
                        castedType.defaultValue();
                        type = EDMTypeConverter.EDM_BOOLEAN;
                    } else if(propertyTypeDeclaration instanceof DateTimeOnlyTypeDeclaration){
                        DateTimeOnlyTypeDeclaration castedType = (DateTimeOnlyTypeDeclaration) propertyTypeDeclaration;
                        type = EDMTypeConverter.EDM_DATETIME;
                    } else if(propertyTypeDeclaration instanceof DateTimeTypeDeclaration){
                        DateTimeTypeDeclaration castedType = (DateTimeTypeDeclaration) propertyTypeDeclaration;
                        type = EDMTypeConverter.EDM_DATETIME;
                    } else if(propertyTypeDeclaration instanceof DateTypeDeclaration){
                        DateTypeDeclaration castedType = (DateTypeDeclaration) propertyTypeDeclaration;
                        type = EDMTypeConverter.EDM_DATETIME;
                    } else if(propertyTypeDeclaration instanceof NumberTypeDeclaration){
                        NumberTypeDeclaration castedType = (NumberTypeDeclaration) propertyTypeDeclaration;
                        String format = castedType.format();
                        if("float".equals(format)){
                            type = EDMTypeConverter.EDM_SINGLE;
                        } else {
                            type = EDMTypeConverter.EDM_DECIMAL;
                        }
                    } else if(propertyTypeDeclaration instanceof StringTypeDeclaration){
                        StringTypeDeclaration castedType = (StringTypeDeclaration) propertyTypeDeclaration;
                        for(AnnotationRef annotation: castedType.annotations()){
                            if("guid".equals(annotation.name())){
                                type = EDMTypeConverter.EDM_GUID;
                            }
                        }
                        if(type == null) {
                            Integer maxLengthInt = castedType.maxLength();
                            maxLength = maxLengthInt != null ? String.valueOf(castedType.maxLength()) : null;
                            type = EDMTypeConverter.EDM_STRING;
                        }
                    } else {
                        throw new UnsupportedOperationException("Type not supported "+propertyTypeDeclaration.name());
                    }

                    notNull("Property \"name\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", entityName);
                    notNull("Property \"type\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", type);
                    notNull("Property \"key\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", key);
                    notNull("Property \"nullable\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", nullable);

                    EntityDefinitionProperty entityDefinitionProperty = new EntityDefinitionProperty(propertyName, type, nullable, key, defaultValue, maxLength, fixedLength, collation, unicode, precision, scale);
                    entityDefinition.addProperty(entityDefinitionProperty);

                    if(!entityDefinition.hasPrimaryKey() && entityDefinitionProperty.isKey()){
                        entityDefinition.setHasPrimaryKey(true);
                    }
                }
                entityDefinitionSet.addEntity(entityDefinition);
            } else {
                throw new OdataMetadataFormatException("Type not supported. "+typeDeclaration.name());
            }
        }
        return entityDefinitionSet;
    }

    private void notNull(String message, Object actual) throws OdataMetadataFieldsException {
        if (actual == null) {
            throw new OdataMetadataFieldsException(message);
        }
    }

    private String getRemoteName(ObjectTypeDeclaration objectTypeDeclaration) {
        for(AnnotationRef annotationRef : objectTypeDeclaration.annotations()){
            if(RamlParser.NAMESPACE_REMOTE_NAME.equals(annotationRef.name())){
                return annotationRef.structuredValue().value().toString();
            }
        }
        return "";
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }
}
