/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.UnsignedByte;
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
	
	public static EdmType convert(String type) {

		if (type.equals(EDM_STRING)) {
		  return EdmSimpleType.STRING;
		} else if (type.equals(EDM_DATETIME)) {
		  return EdmSimpleType.DATETIME;
		} else if (type.equals(EDM_BOOLEAN)) {
		  return EdmSimpleType.BOOLEAN;
		} else if (type.equals(EDM_DECIMAL)) {
		  return EdmSimpleType.DECIMAL;
		} else if (type.equals(EDM_DOUBLE)) {
		  return EdmSimpleType.DOUBLE;
		} else if (type.equals(EDM_SINGLE)) {
		  return EdmSimpleType.SINGLE;
		} else if (type.equals(EDM_INT16)) {
		  return EdmSimpleType.INT16;
		} else if (type.equals(EDM_INT32)) {
		  return EdmSimpleType.INT32;
		} else if (type.equals(EDM_INT64)) {
		  return EdmSimpleType.INT64;
		} else if (type.equals(EDM_TIME)) {
		  return EdmSimpleType.TIME;
		} else if (type.equals(EDM_DATETIMEOFFSET)) {
		  return EdmSimpleType.DATETIMEOFFSET;
		} else if (type.equals(EDM_BINARY)) {
		  return EdmSimpleType.BINARY;
		} else if (type.equals(EDM_BYTE)) {
		  return EdmSimpleType.BYTE;
		} else if (type.equals(EDM_GUID)) {
		  return EdmSimpleType.GUID;
		} else if (type.equals(EDM_SBYTE)) {
		  return EdmSimpleType.SBYTE;
		}
		
		return EdmSimpleType.STRING;
	}

	/*
	 * Sorry man, this class sucks but it was either this or a million smaller builder classes.
	 */
	public static OProperty getOProperty(String name, Object value, EdmType type) {
		if (type.equals(EdmSimpleType.INT16)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.int16(name, Short.valueOf(String.valueOf(value)));
			} else {
				return OProperties.int16(name, null);
			}
		} else if (type.equals(EdmSimpleType.INT32)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.int32(name, Integer.valueOf(String.valueOf(value)));
			} else {
				return OProperties.int32(name, null);
			}
		} else if (type.equals(EdmSimpleType.INT64)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.int64(name, Long.valueOf(String.valueOf(value)));
			} else {
				return OProperties.int64(name, null);
			}	
		} else if (type.equals(EdmSimpleType.BINARY)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.binary(name, String.valueOf(value).getBytes());
			} else {
				return OProperties.binary(name, new byte[0]);
			}	
		} else if (type.equals(EdmSimpleType.BOOLEAN)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.boolean_(name, Boolean.valueOf(String.valueOf(value)));
			} else {
				return OProperties.boolean_(name, null);
			}	
		} else if (type.equals(EdmSimpleType.BYTE)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.byte_(name, UnsignedByte.parseUnsignedByte(String.valueOf(value)));
			} else {
				return OProperties.byte_(name, null);
			}		
		} else if (type.equals(EdmSimpleType.DATETIME)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.datetime(name, DateParser.parse(String.valueOf(value)));
			} else {
				LocalDateTime date = null;
				return OProperties.datetime(name, date);
			}		
		} else if (type.equals(EdmSimpleType.DATETIMEOFFSET)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				Date d = DateParser.parse(String.valueOf(value));
				if (d!=null && value!=org.json.JSONObject.NULL) {
					return OProperties.datetimeOffset(name, new DateTime(d));
				} else {
					DateTime date = null;
					return OProperties.datetimeOffset(name, date);
				}			
			} else {
				DateTime date = null;
				return OProperties.datetimeOffset(name, date);
			}					
		} else if (type.equals(EdmSimpleType.DECIMAL)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.decimal(name, BigDecimal.valueOf(Double.valueOf(String.valueOf(value))));
			} else {
				BigDecimal bd = null;
				return OProperties.decimal(name, bd);
			}	
		} else if (type.equals(EdmSimpleType.DOUBLE)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.double_(name, Double.valueOf(String.valueOf(value)));
			} else {
				return OProperties.double_(name, null);
			}	
		} else if (type.equals(EdmSimpleType.SINGLE)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.single(name, Float.valueOf(String.valueOf(value)));
			} else {
				return OProperties.single(name, null);
			}	
		} else if (type.equals(EdmSimpleType.SBYTE)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.sbyte_(name, Byte.valueOf(String.valueOf(value)));
			} else {
				byte b = 0;
				return OProperties.sbyte_(name, b);
			}	
		} else if (type.equals(EdmSimpleType.TIME)) {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				Date d = DateParser.parse(String.valueOf(value));
				if (d!=null) {
					return OProperties.time(name, d);
				} else {
					Time t = null;
					return OProperties.time(name, t);
				}			
			} else {
				Time t = null;
				return OProperties.time(name, t);
			}		
		} else {
			if (value!=null && value!=org.json.JSONObject.NULL) {
				return OProperties.string(name, String.valueOf(value));
			} else {
				return OProperties.string(name, null);
			}
		}
	}
	
}
