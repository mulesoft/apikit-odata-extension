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

public class EntityModelParser {

	static final Pattern elementPattern = Pattern.compile("^\\s{1,2}(\\w*):$");
	static final String TYPES_ELEMENT = "types:";
	 
	public List<Entity> getEntities(InputStream inputStream) throws IOException, EntityModelParsingException {

		try { 
  		
  		List<Entity> entities = new ArrayList<Entity>();
  		
  		StringWriter writer = new StringWriter();
  		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
  		String text = writer.toString();
  		String[] lines = text.split("\\n");
  		
  		boolean typesFound = false;
  		for (String line : lines) {
  			if (typesFound) {
  				Matcher matcher = elementPattern.matcher(line);
  				if (matcher.find()) {
  					entities.add(new Entity(matcher.group(1)));
  				}
  			}
  			if (line.startsWith(TYPES_ELEMENT)) {
  				typesFound = true;
  			}
  		}
  		
  		return entities;
  		
		} catch (Exception e) {
			throw new EntityModelParsingException(e);
		}
	}
	
}