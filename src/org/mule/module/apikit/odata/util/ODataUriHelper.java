/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;
import org.mule.module.apikit.odata.exception.ODataInvalidUriException;

/**
 * 
 * @author juancazala
 */

public class ODataUriHelper {
	
	public static final String REST_ENTITY_REGEXP = "^(/(\\w|\\-)+)*/?$";
	public static final String[] ODATA_VALID_PARAMS = { "$orderby", "$top", "$skip", "$filter", "$expand", "$format", "$select", "$inlinecount" };
	public static final String[] ODATA_LIST_PARAMS = { "$orderby", "$inlinecount", "$skip", "$top" };
	public static final String ODATA_RESOURCE_REGEXP = "^/((\\w|\\-)+)(\\(((\\w|\\-)+|'[^']*'|(,?(\\w|\\-)+=((\\w|\\-)+|'[^']*'))+)\\))?(/|/\\$(\\w|\\-)+)?$";
	public static final String ODATA_ENTITY_REGEXP = "^/((\\w|\\-)+)((/|\\().*)?$";
	public static final String ODATA_COLLECTION_REGEXP = "^/((\\w|\\-)+)(/|/\\$(\\w|\\-)+)?$";
	public static final String ODATA_SINGLE_KEY_REGEXP = "^(\\d+|'[^']*')$";
	public static final String ODATA_MULTIPLE_KEYS_REGEXP = "^(((\\w|\\-)+)=((\\w|\\-)+|'[^']*'),?)+$";
	public static final String ODATA_ORDERBY_REGEXP = "^((\\w|\\-)+(/\\w)?(\\s(desc|asc))?(,\\s*(\\w|\\-)+(/\\w)?(\\s(desc|asc))?)*)+$";
	public static final String ODATA_TOP_REGEXP = "^\\d+$";
	public static final String ODATA_SKIP_REGEXP = "^\\d+$";
	public static final String ODATA_EXPAND_REGEXP = "^(\\w|\\-)+(/(\\w|\\-)+)?(,\\s*(\\w|\\-)+(/(\\w|\\-)+)?)*$";
	public static final String ODATA_FORMAT_REGEXP = "^(atom|json|xml)$";
	public static final String ODATA_SELECT_REGEXP = "^(\\w|\\-)+(,\\s*(\\w|\\-)+)*$";
	public static final String ODATA_INLINECOUNT_REGEXP = "^(allpages|none)$";
	public static final String ODATA_FILTER_LOGICAL_OPERATOR_REGEXP = "^(.+)\\s+(eq|ne|gt|ge|lt|le|and|or)\\s+(.+)$";
	public static final String ODATA_FILTER_ARITHMETIC_OPERATOR_REGEXP = "^(.+)\\s+(add|sub|mul|div|mod)\\s+(.+)$";
	public static final String ODATA_FILTER_NEGATION_OPERATOR_REGEXP = "^not\\s+(.+)$";
	public static final String ODATA_FILTER_GROUP_OPERATOR_REGEXP = "^\\((.*)\\)$";
	public static final String ODATA_FILTER_FIELD_NAME_REGEXP = "^(\\w|\\-)+(/(\\w|\\-)+)?$";
	public static final String ODATA_FILTER_DATA_TYPE_REGEXP = "^(Null|Edm.Binary|Edm.Boolean|Edm.Byte|Edm.DateTime|Edm.Decimal|Edm.Double|Edm.Single|Edm.Guid|Edm.Int16|Edm.Int32|Edm.Int64|Edm.SByte|Edm.String|Edm.Time|Edm.DateTimeOffset)$";
	public static final String ODATA_FILTER_FUNCTION_REGEXP = "^(\\w+)\\((\\w+|'[^']*'|\\w+\\(.*\\))?(,\\s*(\\w+|'[^']*'|\\w+\\(.*\\)))?(,\\s*(\\w+|'[^']*'|\\w+\\(.*\\)))?\\)$";
	public static final String ODATA_PRIMITIVE_NULL_REGEXP = "^null$";
	public static final String ODATA_PRIMITIVE_BINARY_REGEXP = "^binary'[A-Fa-f0-9]+$";
	public static final String ODATA_PRIMITIVE_BYTE_REGEXP = "^\\d+$";
	public static final String ODATA_PRIMITIVE_BOOLEAN_REGEXP = "^true|false$";
	public static final String ODATA_PRIMITIVE_DATETIME_REGEXP = "^datetime'[0-9999]{1,4}\\-[1-12]{1,2}\\-[1-31]{1,2}T[0-23]{1,2}:[0-59]{1,2}(:[0-59]{1,2}(.\\d{1,8})?)?'$";
	public static final String ODATA_PRIMITIVE_DECIMAL_REGEXP = "^\\-?\\d+(\\.\\d+)?(m|M)?$";
	public static final String ODATA_PRIMITIVE_DOUBLE_REGEXP = "^\\-?\\d+(\\.\\d+)?(E(\\+|\\-)\\d+)?d$";
	public static final String ODATA_PRIMITIVE_FLOAT_REGEXP = "^\\-?\\d+(\\.\\d+)?f$";
	public static final String ODATA_PRIMITIVE_GUID_REGEXP = "^guid'[A-Fa-f0-9]{8}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{4}\\-[A-Fa-f0-9]{8}'$";
	public static final String ODATA_PRIMITIVE_LONG_REGEXP = "^\\d+L$";
	public static final String ODATA_PRIMITIVE_STRING_REGEXP = "^'([^']*)'$";
	public static final String ODATA_PRIMITIVE_TIME_REGEXP = "^[0-23]{1,2}:[0-59]{1,2}:[0-59]{1,2}$";
	public static final String ODATA_PRIMITIVE_DATETIME_OFFSET_REGEXP = "^[0-9999]{1,4}\\-[1-12]{1,2}\\-[1-31]{1,2}T[0-23]{1,2}:[0-59]{2}:[0-59]{2}Z$";
	
	public static final Pattern REST_ENTITY_PATTERN = Pattern.compile(REST_ENTITY_REGEXP);
	public static final Pattern ODATA_RESOURCE_PATTERN = Pattern.compile(ODATA_RESOURCE_REGEXP);
	public static final Pattern ODATA_ENTITY_PATTERN = Pattern.compile(ODATA_ENTITY_REGEXP);
	public static final Pattern ODATA_COLLECTION_PATTERN = Pattern.compile(ODATA_COLLECTION_REGEXP);
	public static final Pattern ODATA_SINGLE_KEY_PATTERN = Pattern.compile(ODATA_SINGLE_KEY_REGEXP);
	public static final Pattern ODATA_MULTIPLE_KEYS_PATTERN = Pattern.compile(ODATA_MULTIPLE_KEYS_REGEXP);
	public static final Pattern ODATA_ORDERBY_PATTERN = Pattern.compile(ODATA_ORDERBY_REGEXP);
	public static final Pattern ODATA_TOP_PATTERN = Pattern.compile(ODATA_TOP_REGEXP);
	public static final Pattern ODATA_SKIP_PATTERN = Pattern.compile(ODATA_SKIP_REGEXP);
	public static final Pattern ODATA_EXPAND_PATTERN = Pattern.compile(ODATA_EXPAND_REGEXP);
	public static final Pattern ODATA_FORMAT_PATTERN = Pattern.compile(ODATA_FORMAT_REGEXP);
	public static final Pattern ODATA_SELECT_PATTERN = Pattern.compile(ODATA_SELECT_REGEXP);
	public static final Pattern ODATA_INLINECOUNT_PATTERN = Pattern.compile(ODATA_INLINECOUNT_REGEXP);
	public static final Pattern ODATA_FILTER_LOGICAL_OPERATOR_PATTERN = Pattern.compile(ODATA_FILTER_LOGICAL_OPERATOR_REGEXP);
	public static final Pattern ODATA_FILTER_ARITHMETIC_OPERATOR_PATTERN = Pattern.compile(ODATA_FILTER_ARITHMETIC_OPERATOR_REGEXP);
	public static final Pattern ODATA_FILTER_NEGATION_OPERATOR_PATTERN = Pattern.compile(ODATA_FILTER_NEGATION_OPERATOR_REGEXP);
	public static final Pattern ODATA_FILTER_GROUP_OPERATOR_PATTERN = Pattern.compile(ODATA_FILTER_GROUP_OPERATOR_REGEXP);
	public static final Pattern ODATA_FILTER_FIELD_NAME_PATTERN = Pattern.compile(ODATA_FILTER_FIELD_NAME_REGEXP);
	public static final Pattern ODATA_FILTER_DATA_TYPE_PATTERN = Pattern.compile(ODATA_FILTER_DATA_TYPE_REGEXP);
	public static final Pattern ODATA_FILTER_FUNCTION_PATTERN = Pattern.compile(ODATA_FILTER_FUNCTION_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_NULL_PATTERN = Pattern.compile(ODATA_PRIMITIVE_NULL_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_BINARY_PATTERN = Pattern.compile(ODATA_PRIMITIVE_BINARY_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_BYTE_PATTERN = Pattern.compile(ODATA_PRIMITIVE_BYTE_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_BOOLEAN_PATTERN = Pattern.compile(ODATA_PRIMITIVE_BOOLEAN_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_DATETIME_PATTERN = Pattern.compile(ODATA_PRIMITIVE_DATETIME_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_DECIMAL_PATTERN = Pattern.compile(ODATA_PRIMITIVE_DECIMAL_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_DOUBLE_PATTERN = Pattern.compile(ODATA_PRIMITIVE_DOUBLE_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_FLOAT_PATTERN = Pattern.compile(ODATA_PRIMITIVE_FLOAT_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_GUID_PATTERN = Pattern.compile(ODATA_PRIMITIVE_GUID_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_LONG_PATTERN = Pattern.compile(ODATA_PRIMITIVE_LONG_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_STRING_PATTERN = Pattern.compile(ODATA_PRIMITIVE_STRING_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_TIME_PATTERN = Pattern.compile(ODATA_PRIMITIVE_TIME_REGEXP);
	public static final Pattern ODATA_PRIMITIVE_DATETIME_OFFSET_PATTERN = Pattern.compile(ODATA_PRIMITIVE_DATETIME_OFFSET_REGEXP);
	
	
	public static boolean isMetadata(String path){
		return path.contains("$metadata");
	}
	
	public static boolean isCount(String path){
		return path.contains("$count");
	}
	
	public static boolean hasFormat(String query){
		return query.contains("$format");
	}
	
	public static boolean isCollection(String path){
		return ODATA_COLLECTION_PATTERN.matcher(path).matches();
	}
	
	public static boolean isServiceDocument(String path){
		return path.isEmpty() || path.equals("/");
	}
	
	public static boolean isResourceRequest(String path){
		return ODATA_RESOURCE_PATTERN.matcher(path).matches();
	}
	
	
	public static String[] parseRequest(String path) {
		
		if (path.startsWith("/odata.svc"))
		{
			path = path.replace("/odata.svc", "");

		}
		
		String entity = "";
		String id = "";
		
		Matcher m = ODATA_RESOURCE_PATTERN.matcher(path);
		
		if (m.matches()){ // it's OData
			
			entity = m.group(1);
			if (m.group(4) != null)
			{
				id = m.group(4);
			}
		} else if (REST_ENTITY_PATTERN.matcher(path).matches()) // it's Rest
		{
			if (path.startsWith("/"))
			{
				return path.substring(1).split("/");
			}
			return path.split("/");
		}
		
		return new String[]{ entity, id };
	}
	
	public static String parseEntity(String path) {
		if (path.startsWith("/odata.svc"))
		{
			path = path.replace("/odata.svc", "");

		}
		
		Matcher m = ODATA_ENTITY_PATTERN.matcher(path);
		if (m.matches())
		{
			return m.group(1);
		} else {
			return "";
		}
	}
	
	public static HashMap<String, Object> parseKeys(String path, String[] entityKeys) throws ODataInvalidUriException {
		
		HashMap<String, Object> keys = new HashMap<String, Object>();
		String parsedId = parseRequest(path)[1];
		String[] parsedKeys;
		
		// parse raw keys
		if (parsedId != null)
		{
			Matcher m = ODATA_MULTIPLE_KEYS_PATTERN.matcher(parsedId);
			if (m.matches())
			{
				parsedKeys = parsedId.split(",");
			} else {
				if (parsedId.isEmpty())
				{
					parsedKeys = new String[] {};
				} else {
					m = ODATA_SINGLE_KEY_PATTERN.matcher(parsedId);
					if (m.matches())
					{
						m = ODATA_PRIMITIVE_STRING_PATTERN.matcher(parsedId);
						if (m.matches())
						{
							parsedId = parsedId.replace("'", "");
						}
						parsedKeys = new String[] { parsedId };
					} else {
						throw new ODataInvalidUriException("Invalid format for key value '" + parsedId + "' should be an int or a string wrapped in single quotes.");
					}
				}
			}
		} else {
			parsedKeys = new String[] {};
		}
		
		if (entityKeys.length == 1) // id is a single key
		{
			String entityKeyName = entityKeys[0];
			if (parsedKeys.length == 1 && !parsedKeys[0].isEmpty())
			{
				String[] id = parsedKeys[0].split("=");
				
				if (id.length == 1)  // id isn't a key-value pair
				{
					keys.put(entityKeyName, id[0]);
					
				} else { // id is a key-value pair
					
					String key = id[0];
					String value = id[1];
					
					if (key.equals(entityKeyName))
					{
						keys.put(key, value);
					} else {
						throw new ODataInvalidUriException("Invalid key '" + key + "' should be '" + entityKeyName + "'.");
					}
				}
			} else if (parsedKeys.length > 1)
			{
				throw new ODataInvalidUriException("Unexpected number of keys: found " + parsedKeys.length + " where " + entityKeys.length + " were expected.");
			}
		} else if (entityKeys.length > 1) { // id is a composite key
			
			if (parsedKeys.length != 0 && parsedKeys.length != entityKeys.length) // invalid number of keys
			{
				throw new ODataInvalidUriException("Unexpected number of keys: found " + parsedKeys.length + " where " + entityKeys.length + " were expected.");
			}
			
			for (String parsedKey: parsedKeys) // iterate over the parsed keys
			{
				String[] parts = parsedKey.split("=");
				
				String key = parts[0];
				String value = parts[1];
				
				if (key == null || value == null) // missing '='
				{
					throw new ODataInvalidUriException("Invalid format for key '" + parsedKey + "'.");
				}
				
				if (Arrays.asList(entityKeys).contains(key)) // check if the key found is an expected key
				{
					Matcher s = ODATA_PRIMITIVE_STRING_PATTERN.matcher(value);
					if (s.matches()) // remove surrounding quotes
					{
						value = s.group(1);
					}
					keys.put(key, value);
				} else {
					throw new ODataInvalidUriException("Unknown key '" + key + "'."); // unexpected key
				}
			}
		}
		
		return keys;
	}
	
	public static boolean allowedQuery(String path, String query) throws ODataInvalidUriException {
		
		if (query.isEmpty())
		{
			return true;
		} 
		String[] queryParams = query.replace("?", "").split("&");
		
		if (isMetadata(path) || isServiceDocument(path))
		{
			for (String queryParam : queryParams) {
				String argument = queryParam.split("=")[0];
				if (argument.startsWith("$")) {
					
					if (argument.equals("$format"))
					{
						return true;
					}
						else if (Arrays.asList(ODATA_VALID_PARAMS).contains(argument))
					{
						throw new ODataInvalidUriException("Query options $select, $expand, $filter, $orderby, $inlinecount, $skip and $top are not supported by this request method or cannot be applied to the requested resource.");
					} else {
						throw new ODataInvalidUriException("The query parameter '" + argument + "' begins with a system-reserved '$' character but is not recognized.");
					}
				}
			}
		} 
		else if (isResourceRequest(path))
		{
		 	if (!isCollection(path)) {
				for (String queryParam : queryParams) {
					String argument = queryParam.split("=")[0];
					if (argument.startsWith("$")) {
						if (Arrays.asList(ODATA_LIST_PARAMS).contains(argument))
						{
							throw new ODataInvalidUriException("Query options $orderby, $inlinecount, $skip and $top are not supported by this request method or cannot be applied to the requested resource.");
						} 
						else if (!Arrays.asList(ODATA_VALID_PARAMS).contains(argument))
						{
							throw new ODataInvalidUriException("The query parameter '" + argument + "' begins with a system-reserved '$' character but is not recognized.");
						}
					}
				}	
		 	} else {
				for (String queryParam : queryParams) {
					String argument = queryParam.split("=")[0];
					if (argument.startsWith("$") && !Arrays.asList(ODATA_VALID_PARAMS).contains(argument))
					{
						throw new ODataInvalidUriException("The query parameter '" + argument + "' begins with a system-reserved '$' character but is not recognized.");
					}
				}
		 	}
		}
		
		return true;
	}
	
	
	
	public static boolean validQuery(String query) throws ODataInvalidUriException, ODataInvalidFormatException{
		if (!query.isEmpty()) {
			String[] queryParams = query.replace("?", "").split("&");
			ArrayList<String> usedParams = new ArrayList<String>();
			for (String queryParam : queryParams) {
				String[] elems = queryParam.split("=");
				if (elems.length == 2) {
					String key = elems[0];
					String value = elems[1];
					if (usedParams.contains(key)){
						throw new ODataInvalidUriException("The query argument '" + key + "' is repeated.");
					}
					usedParams.add(key);
					validArgumentValue(key,value);
				} else {
					throw new ODataInvalidUriException("Incorrect format, missing '=' in query argument.");
				}
			}
		}
		return true;
	}
	
	public static boolean validArgumentValue(String argument, String value) throws ODataInvalidUriException, ODataInvalidFormatException {
		
		// validate the value of the argument
		Matcher m;
		switch(argument)
		{ 
		case "$orderby":
			m = ODATA_ORDERBY_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $orderby argument '" + value + "'.");
			break;
			
		case "$top":
			m = ODATA_TOP_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $top argument '" + value + "'.");
			break;
			
		case "$skip":
			m = ODATA_SKIP_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $skip argument '" + value + "'.");
			break;
			
		case "$expand":
			m = ODATA_EXPAND_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $expand argument '" + value + "'.");
			break;
			
		case "$format":
			m = ODATA_FORMAT_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidFormatException("Incorrect format for $format argument '" + value + "'.");
			break;
			
		case "$select":
			m = ODATA_SELECT_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $select argument '" + value + "'.");
			break;
			
		case "$inlinecount":
			m = ODATA_INLINECOUNT_PATTERN.matcher(value);
			if (!m.matches())
				throw new ODataInvalidUriException("Incorrect format for $inlinecount argument '" + value + "'.");
			break;
			
		case "$filter":
			if (!validFilter(value))
				throw new ODataInvalidUriException("Incorrect format for $filter argument '" + value + "'.");
			break;
		}
		
		return true;
	}
	
	private static boolean validFilter(String filter) throws ODataInvalidUriException{
		Matcher m;
		
		// validate grouping operator
		m = ODATA_FILTER_GROUP_OPERATOR_PATTERN.matcher(filter);
		if (m.matches())
		{
			String expression = m.group(1);
			return validFilter(expression);
		}
		
		// validate logical binary operators
		m = ODATA_FILTER_LOGICAL_OPERATOR_PATTERN.matcher(filter);
		if (m.matches())
		{
			String left = m.group(1);
			String right = m.group(3);
			return validFilter(left) && validFilter(right);
		}
		
		// validate arithmetic binary operators
		m = ODATA_FILTER_ARITHMETIC_OPERATOR_PATTERN.matcher(filter);
		if (m.matches())
		{
			String left = m.group(1);
			String right = m.group(3);
			return validFilter(left) && validFilter(right);
		}
		
		// validate negation unary operator
		m = ODATA_FILTER_NEGATION_OPERATOR_PATTERN.matcher(filter);
		if (m.matches())
		{
			String expression = m.group(1);
			return validFilter(expression);
		}
		
		// validate field name expression
		m = ODATA_FILTER_FIELD_NAME_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive string
		m = ODATA_PRIMITIVE_STRING_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive byte
		m = ODATA_PRIMITIVE_BYTE_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive boolean
		m = ODATA_PRIMITIVE_BOOLEAN_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive datetime
		m = ODATA_PRIMITIVE_DATETIME_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive null
		m = ODATA_PRIMITIVE_NULL_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive decimal
		m = ODATA_PRIMITIVE_DECIMAL_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive double
		m = ODATA_PRIMITIVE_DOUBLE_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive float
		m = ODATA_PRIMITIVE_FLOAT_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive guid
		m = ODATA_PRIMITIVE_GUID_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive long
		m = ODATA_PRIMITIVE_LONG_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive time
		m = ODATA_PRIMITIVE_TIME_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive datetime offset
		m = ODATA_PRIMITIVE_DATETIME_OFFSET_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate primitive binary
		m = ODATA_PRIMITIVE_BINARY_PATTERN.matcher(filter);
		if (m.matches())
		{
			return true;
		}
		
		// validate function
		m = ODATA_FILTER_FUNCTION_PATTERN.matcher(filter);
		if (m.matches())
		{	
			String functionName = m.group(1).toLowerCase();
			String arg0 = m.group(2);
			String arg1 = m.group(4);
			String arg2 = m.group(6);
			
			Pattern stringType = ODATA_PRIMITIVE_STRING_PATTERN;
			Pattern intType = ODATA_PRIMITIVE_BYTE_PATTERN;
			Pattern dateType = ODATA_PRIMITIVE_DATETIME_PATTERN;
			Pattern decimalType = ODATA_PRIMITIVE_DECIMAL_PATTERN;
			Pattern doubleType = ODATA_PRIMITIVE_DOUBLE_PATTERN;
			Pattern typeType = ODATA_FILTER_DATA_TYPE_PATTERN;
			Pattern fieldType = ODATA_FILTER_FIELD_NAME_PATTERN;
			
			try {
				// validate argument types
				switch(functionName)
				{
					case "substringof":
					case "endswith":
					case "startswith":
					case "indexof":	
					case "concat":
						if (validArgumentType(arg0, stringType) && validArgumentType(arg1, stringType) && arg2 == null)
							return true;
						break;
					case "length":
					case "tolower":
					case "toupper":
					case "trim":
						if (validArgumentType(arg0, stringType) && arg1 == null && arg2 == null)
							return true;
						break;
					case "replace":
						if (validArgumentType(arg0, stringType) && validArgumentType(arg1, stringType) && validArgumentType(arg2, stringType))
							return true;
						break;
					case "substring":
						if (validArgumentType(arg0, stringType) && validArgumentType(arg1, intType) && (arg2 == null || validArgumentType(arg2, intType)))
							return true;
						break;
					case "day":
					case "hour":
					case "minute":
					case "second":
					case "month":
					case "year":
						if (validArgumentType(arg0, dateType) && arg1 == null && arg2 == null)
							return true;
						break;
					case "round":
					case "floor":
					case "ceiling":
						if ((validArgumentType(arg0, decimalType) || validArgumentType(arg0, doubleType))  && arg1 == null && arg2 == null)
							return true;
						break;
					case "isof":
						if (validArgumentType(arg0, typeType) && arg1 == null && arg2 == null || validArgumentType(arg0, fieldType) && validArgumentType(arg1, typeType) && arg2 == null)
							return true;
						break;
						
				}
			} catch(Exception e){
				throw new ODataInvalidUriException("Incorrect argument type in function '" + functionName + "': " + e.getMessage());
			}
			
		}
		
		return false;
	}
	
	private static boolean validArgumentType(String argument, Pattern type) throws Exception {
		Pattern fieldType = ODATA_FILTER_FIELD_NAME_PATTERN;
		if (!type.matcher(argument).matches() && !fieldType.matcher(argument).matches())
			if (!validFilter(argument))
				throw new Exception("Unexpected value '" + argument + "'.");
		return true;
	}
}
