/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import static java.lang.String.valueOf;
import static org.json.JSONObject.NULL;
import static org.mule.module.apikit.odata.util.DateParser.parse;
import static org.odata4j.core.OProperties.binary;
import static org.odata4j.core.OProperties.boolean_;
import static org.odata4j.core.OProperties.byte_;
import static org.odata4j.core.OProperties.datetime;
import static org.odata4j.core.OProperties.datetimeOffset;
import static org.odata4j.core.OProperties.decimal;
import static org.odata4j.core.OProperties.double_;
import static org.odata4j.core.OProperties.int16;
import static org.odata4j.core.OProperties.int32;
import static org.odata4j.core.OProperties.int64;
import static org.odata4j.core.OProperties.sbyte_;
import static org.odata4j.core.OProperties.single;
import static org.odata4j.core.OProperties.string;
import static org.odata4j.core.OProperties.time;
import static org.odata4j.core.UnsignedByte.parseUnsignedByte;
import static org.odata4j.edm.EdmSimpleType.BINARY;
import static org.odata4j.edm.EdmSimpleType.BOOLEAN;
import static org.odata4j.edm.EdmSimpleType.BYTE;
import static org.odata4j.edm.EdmSimpleType.DATETIME;
import static org.odata4j.edm.EdmSimpleType.DATETIMEOFFSET;
import static org.odata4j.edm.EdmSimpleType.DECIMAL;
import static org.odata4j.edm.EdmSimpleType.DOUBLE;
import static org.odata4j.edm.EdmSimpleType.GUID;
import static org.odata4j.edm.EdmSimpleType.INT16;
import static org.odata4j.edm.EdmSimpleType.INT32;
import static org.odata4j.edm.EdmSimpleType.INT64;
import static org.odata4j.edm.EdmSimpleType.SBYTE;
import static org.odata4j.edm.EdmSimpleType.SINGLE;
import static org.odata4j.edm.EdmSimpleType.STRING;
import static org.odata4j.edm.EdmSimpleType.TIME;
import java.math.BigDecimal;
import org.joda.time.DateTime;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;

public class EDMTypeConverter {

  public static final String EDM_STRING = "Edm.String";
  public static final String EDM_DATETIME = "Edm.DateTime";
  public static final String EDM_BOOLEAN = "Edm.Boolean";
  public static final String EDM_DECIMAL = "Edm.Decimal";
  public static final String EDM_DOUBLE = "Edm.Double";
  public static final String EDM_SINGLE = "Edm.Single";
  public static final String EDM_INT16 = "Edm.Int16";
  public static final String EDM_INT32 = "Edm.Int32";
  public static final String EDM_INT64 = "Edm.Int64";
  public static final String EDM_TIME = "Edm.Time";
  public static final String EDM_DATETIMEOFFSET = "Edm.DateTimeOffset";
  public static final String EDM_BINARY = "Edm.Binary";
  public static final String EDM_BYTE = "Edm.Byte";
  public static final String EDM_GUID = "Edm.Guid";
  public static final String EDM_SBYTE = "Edm.SByte";

  public static EdmSimpleType convert(String type) {

    if (EDM_STRING.equals(type)) {
      return STRING;
    } else if (EDM_DATETIME.equals(type)) {
      return DATETIME;
    } else if (EDM_BOOLEAN.equals(type)) {
      return BOOLEAN;
    } else if (EDM_DECIMAL.equals(type)) {
      return DECIMAL;
    } else if (EDM_DOUBLE.equals(type)) {
      return DOUBLE;
    } else if (EDM_SINGLE.equals(type)) {
      return SINGLE;
    } else if (EDM_INT16.equals(type)) {
      return INT16;
    } else if (EDM_INT32.equals(type)) {
      return INT32;
    } else if (EDM_INT64.equals(type)) {
      return INT64;
    } else if (EDM_TIME.equals(type)) {
      return TIME;
    } else if (EDM_DATETIMEOFFSET.equals(type)) {
      return DATETIMEOFFSET;
    } else if (EDM_BINARY.equals(type)) {
      return BINARY;
    } else if (EDM_BYTE.equals(type)) {
      return BYTE;
    } else if (EDM_GUID.equals(type)) {
      return GUID;
    } else if (EDM_SBYTE.equals(type)) {
      return SBYTE;
    }

    return STRING;
  }


  public static OProperty getOProperty(String name, Object value, EdmType type) {
    boolean isNotNullValue = value != null && value != NULL;

    if (INT16.equals(type)) {
      return int16(name, isNotNullValue ? Short.valueOf(valueOf(value)) : null);

    } else if (INT32.equals(type)) {
      return int32(name, isNotNullValue ? Integer.valueOf(valueOf(value)) : null);

    } else if (INT64.equals(type)) {
      return int64(name, isNotNullValue ? Long.valueOf(valueOf(value)) : null);

    } else if (BINARY.equals(type)) {
      return binary(name, isNotNullValue ? valueOf(value).getBytes() : new byte[0]);

    } else if (BOOLEAN.equals(type)) {
      return boolean_(name, isNotNullValue ? Boolean.valueOf(valueOf(value)) : null);

    } else if (BYTE.equals(type)) {
      return byte_(name, isNotNullValue ? parseUnsignedByte(valueOf(value)) : null);

    } else if (DATETIME.equals(type)) {
      return datetime(name, isNotNullValue ? parse(valueOf(value)) : null);

    } else if (DATETIMEOFFSET.equals(type)) {
      return datetimeOffset(name, isNotNullValue ? new DateTime(parse(valueOf(value))) : null);

    } else if (DECIMAL.equals(type)) {
      return decimal(name, isNotNullValue ? new BigDecimal(valueOf(value)) : null);

    } else if (DOUBLE.equals(type)) {
      return double_(name, isNotNullValue ? Double.valueOf(valueOf(value)) : null);

    } else if (SINGLE.equals(type)) {
      return single(name, isNotNullValue ? Float.valueOf(valueOf(value)) : null);

    } else if (SBYTE.equals(type)) {
      return sbyte_(name, isNotNullValue ? Byte.valueOf(valueOf(value)) : 0);

    } else if (TIME.equals(type)) {
      return time(name, isNotNullValue ? parse(valueOf(value)) : null);

    } else {
      return string(name, isNotNullValue ? valueOf(value) : null);
    }
  }

}
