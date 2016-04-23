/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.mule.module.apikit.model.exception.EntityModelParsingException;
import org.mule.module.apikit.model.exception.InvalidModelException;

public class EntityModelParser {

	static final String typesPattern = "^types:\\s*$";
	static final Pattern elementPattern = Pattern.compile("^\\s{2}(\\w+):\\s*$");
	static final Pattern remotePattern = Pattern.compile("^\\s{4}\\(odata.remote\\):\\s*(\\w+)\\s*$");
	static final String propertiesPattern = "^\\s{4}properties:\\s*$";
	static final Pattern fieldPattern = Pattern.compile("^\\s{6}(\\w+):\\s*$");
	static final Pattern typePropertyPattern = Pattern.compile("^\\s{8}type:\\s*(\\w+)\\s*$");
	static final Pattern keyPropertyPattern = Pattern.compile("^\\s{8}\\(odata.key\\):\\s*(\\w+)\\s*$");
	static final Pattern nullablePropertyPattern = Pattern.compile("^\\s{8}\\(odata.nullable\\):\\s*(\\w+)\\s*$");
	 
	/**
	 * Parses the entities out of the RAML file and looks for required fields
	 * @param inputStream
	 * @return
	 * @throws IOException
	 * @throws EntityModelParsingException
	 */
	public List<Entity> getEntities(InputStream inputStream) throws IOException, EntityModelParsingException {

		try { 
  		String[] lines = getFileLines(inputStream);
  		return getEntities(lines);
		} catch (Exception e) {
			throw new EntityModelParsingException(e.getMessage());
		}
	}

	private List<Entity> getEntities(String[] lines) throws InvalidModelException {
		List<Entity> entities = new ArrayList<Entity>();
		boolean typesFound = false;

		Entity entity = new Entity(null);
		Property property = new Property(null);
		for (String line : lines) {
			
			// Looks for the "types:" keyword
			if (line.matches(typesPattern)) {
				typesFound = true;
			}
			
			// If "types" was found, looks for other possible matches to build the entities
			if (typesFound) {
				
				// Matches element name
				Matcher matcher = elementPattern.matcher(line);
				if (matcher.find()) {
					if (entity.getName() == null) {
						entity = new Entity(matcher.group(1));
					} else {
						if (entity.isValid()) {
							entities.add(entity);
							entity = new Entity(matcher.group(1));
						}
					}
				}

				matcher = remotePattern.matcher(line);
				if (matcher.find()) {
					entity.setRemote(matcher.group(1));
				}
				
				// Matches the properties keyword
				if (line.matches(propertiesPattern)) {
					entity.setPropertiesFound(true);
				}
				matcher = fieldPattern.matcher(line);
				if (matcher.find()) {
					if (property.getName() == null) {
						property = new Property(matcher.group(1));
					} else {
						if (property.isValid()) {
							entity.addProperty(property);
							property = new Property(matcher.group(1));
						}
					}
				}
				
				// Matches the required property attributes
				matcher = typePropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setType(matcher.group(1));
				}
				
				matcher = keyPropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setKey(matcher.group(1));
				}
				
				matcher = nullablePropertyPattern.matcher(line);
				if (matcher.find()) {
					property.setNullable(matcher.group(1));
				}
			}	
		}
		
		if (!typesFound) {
			throw new InvalidModelException("no types definition where found, please check the model");
		}
		
		
		// if there is any entity left...
		if (property.isValid()) {
			entity.addProperty(property);
		}
		
		if (entity.isValid()) {
			entities.add(entity);
		}
		
		return entities;
	}	
	
	private String[] getFileLines(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		String text = writer.toString();
		String[] lines = text.split("\\n");
		return lines;
	}
	
}