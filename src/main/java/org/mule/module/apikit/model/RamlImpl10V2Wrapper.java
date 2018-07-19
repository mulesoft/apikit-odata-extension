/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import com.google.common.base.Strings;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionSet;
import org.mule.module.apikit.odata.metadata.raml.RamlParser;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.api.Library;
import org.raml.v2.api.model.v10.datamodel.BooleanTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.DateTimeOnlyTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.DateTimeTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.DateTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.FileTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.IntegerTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.NullTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.NumberTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TimeOnlyTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.raml.v2.api.model.v10.declarations.AnnotationRef;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.mule.module.apikit.model.OdataServiceConstants.ODATA_MODEL;
import static org.mule.module.apikit.odata.metadata.raml.RamlParser.FLOAT;
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

public class RamlImpl10V2Wrapper {
    private RamlModelResult api;

    public RamlImpl10V2Wrapper(RamlModelResult api) {
        this.api = api;
    }

    public EntityDefinitionSet getSchemas() throws OdataMetadataFieldsException, OdataMetadataFormatException {

        final EntityDefinitionSet entityDefinitionSet = new EntityDefinitionSet();

        for (TypeDeclaration typeDeclaration : getTypes()) {
            if (typeDeclaration instanceof ObjectTypeDeclaration) {
                final EntityDefinition entityDefinition = buildEntityDefinition((ObjectTypeDeclaration) typeDeclaration);
                entityDefinitionSet.addEntity(entityDefinition);
            } else {
                throw new OdataMetadataFormatException("Type not supported. " + typeDeclaration.name());
            }
        }

        return entityDefinitionSet;
    }

    private EntityDefinition buildEntityDefinition(ObjectTypeDeclaration objectTypeDeclaration) throws OdataMetadataFormatException, OdataMetadataFieldsException {

        if (objectTypeDeclaration.properties().isEmpty()) throw new OdataMetadataFormatException("No schemas found.");

        // set entity properties
        String entityName = objectTypeDeclaration.name();
        String remoteName = getAnnotation(objectTypeDeclaration, NAMESPACE_REMOTE_NAME);
        EntityDefinition entityDefinition = new EntityDefinition(entityName, remoteName);

        for (TypeDeclaration propertyTypeDeclaration : objectTypeDeclaration.properties()) {
            final TypeDeclaration scalarTypeDeclaration = getScalarTypeDeclaration(propertyTypeDeclaration);

            final String propertyName = scalarTypeDeclaration.name();

            notNull("Property \"name\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", entityName);
            notNull("Property \"remote name\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", remoteName);

            final String key = getAnnotation(scalarTypeDeclaration, NAMESPACE_KEY_PROPERTY);
            notNull("Property \"key\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", key);
            final boolean isKey = Boolean.valueOf(key);

            final String nullable = getAnnotation(scalarTypeDeclaration, NAMESPACE_NULLABLE_PROPERTY);
            notNull("Property \"nullable\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", nullable);
            final boolean isNullable = Boolean.valueOf(nullable);

            final String type = getOdataType(scalarTypeDeclaration);
            notNull("Property \"type\" is missing in field \"" + propertyName + "\" in entity \"" + entityName + "\"", type);

            String maxLength = null;
            if (EDM_STRING.equals(type)) {
                Integer maxLengthInt = ((StringTypeDeclaration) scalarTypeDeclaration).maxLength();
                maxLength = maxLengthInt != null ? String.valueOf(maxLengthInt) : null;
            }

            final String defaultValue = scalarTypeDeclaration.defaultValue();
            final String precision = getAnnotation(scalarTypeDeclaration, NAMESPACE_PRECISION_PROPERTY);
            final String scale = getAnnotation(scalarTypeDeclaration, NAMESPACE_SCALE_PROPERTY);

            EntityDefinitionProperty entityDefinitionProperty = new EntityDefinitionProperty(propertyName, type, isNullable, isKey, defaultValue, maxLength, false, null, false, precision, scale);
            entityDefinition.addProperty(entityDefinitionProperty);

            if (!entityDefinition.hasPrimaryKey() && entityDefinitionProperty.isKey()) {
                entityDefinition.setHasPrimaryKey(true);
            }
        }

        if(!entityDefinition.hasPrimaryKey())
            throw new OdataMetadataFieldsException("Entity definition must have a primary key.");

        return entityDefinition;
    }

    private List<TypeDeclaration> getTypes() throws OdataMetadataFormatException {
        List<TypeDeclaration> types = new ArrayList<>();

        if(api.getApiV10() == null) {
            // parsing a library
            final Library library = api.getLibrary();
            if (library != null) types = library.types();
        } else {
            // types must be defined in the model library referenced in the root raml
            Api apiv10 = api.getApiV10();
            for(Library library: apiv10.uses()){
                if(library.name().equals(ODATA_MODEL)){
                    types = library.types();
                    break;
                }
            }
        }

        if (types.isEmpty()) {
            throw new OdataMetadataFormatException(format("No types defined in %s.", ODATA_MODEL));
        }

        return types;
    }

    private String getOdataType(TypeDeclaration propertyTypeDeclaration) throws OdataMetadataFieldsException {
        if (propertyTypeDeclaration instanceof BooleanTypeDeclaration) return EDM_BOOLEAN;
        if (propertyTypeDeclaration instanceof TimeOnlyTypeDeclaration) return EDM_TIME;
        if (propertyTypeDeclaration instanceof DateTimeOnlyTypeDeclaration) return EDM_DATETIME;
        if (propertyTypeDeclaration instanceof DateTimeTypeDeclaration) return EDM_DATETIMEOFFSET;
        if (propertyTypeDeclaration instanceof DateTypeDeclaration) return EDM_DATETIME;
        if (propertyTypeDeclaration instanceof NumberTypeDeclaration) return getNumberType((NumberTypeDeclaration) propertyTypeDeclaration);
        if (propertyTypeDeclaration instanceof StringTypeDeclaration) return getStringType((StringTypeDeclaration) propertyTypeDeclaration);
        if (propertyTypeDeclaration instanceof FileTypeDeclaration) return EDM_BINARY;

        throw new UnsupportedOperationException("Type not supported " + propertyTypeDeclaration.name());
    }

    private TypeDeclaration getScalarTypeDeclaration(TypeDeclaration propertyTypeDeclaration) throws OdataMetadataFieldsException {
        if(propertyTypeDeclaration instanceof UnionTypeDeclaration){
            UnionTypeDeclaration unionType = (UnionTypeDeclaration) propertyTypeDeclaration;
            for(TypeDeclaration unionSubType : unionType.of()){
                if(!(unionSubType instanceof NullTypeDeclaration)){
                    return unionSubType;
                }
            }

            throw new OdataMetadataFieldsException(format("Property %s cannot be just null.", propertyTypeDeclaration.name()));
        }

        return propertyTypeDeclaration;
    }

    private String getNumberType(NumberTypeDeclaration propTypeDeclaration) throws OdataMetadataFieldsException {
        String format = propTypeDeclaration.format();

        if (format != null) {
            switch (format) {
                case INT64: return EDM_INT64;
                case INT32: return EDM_INT32;
                case INT16: return EDM_INT16;
                case INT8: return EDM_BYTE;
                case FLOAT: if (!(propTypeDeclaration instanceof IntegerTypeDeclaration)) return EDM_SINGLE;
                default: throw new OdataMetadataFieldsException(format("Unexpected format %s for number type.", format));
            }
        }

        if (propTypeDeclaration instanceof IntegerTypeDeclaration) return EDM_INT32;

        final String scale = getAnnotation(propTypeDeclaration, NAMESPACE_SCALE_PROPERTY);
        final String precision = getAnnotation(propTypeDeclaration, NAMESPACE_PRECISION_PROPERTY);
        if (scale != null && precision != null) return EDM_DECIMAL;

        return EDM_DOUBLE;
    }

    private String getStringType(StringTypeDeclaration stringTypeDeclaration) {
        final String subType = getAnnotation(stringTypeDeclaration, NAMESPACE_TYPE_PROPERTY);

        if (GUID.equals(subType)) return EDM_GUID;

        return EDM_STRING;
    }

    private void notNull(String message, Object actual) throws OdataMetadataFieldsException {
        if (actual == null || Strings.isNullOrEmpty(actual.toString())) {
            throw new OdataMetadataFieldsException(message);
        }
    }

    @Nullable private String getAnnotation(TypeDeclaration typeDeclaration, String annotationName) {
        for (AnnotationRef annotation : typeDeclaration.annotations()) {
            if (annotationName.equals(annotation.name())) return annotation.structuredValue().value().toString();
        }

        return null;
    }

    public Api getApi() {
        return api.getApiV10();
    }
}
