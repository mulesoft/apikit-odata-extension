/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.model.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import freemarker.template.TemplateException;

/**
 * 
 * @author arielsegura
 */
public class RamlGeneratorTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testSingleKey() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		Assert.assertEquals(readFromFile("model/custom.raml"), new RamlGenerator().generate(("model/validOdataModel2.raml")));
	}

	@Test
	public void testDoubleKey() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		Assert.assertEquals(readFromFile("model/custom-double-key.raml"), new RamlGenerator().generate(("model/valid-doublekey-OdataModel.raml")));
	}

	@Test
	public void invalidModel1Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("there are entities with empty names, please fix the model");
		new RamlGenerator().generate("model/invalidOdataModel1.raml");
	}

	@Test
	public void invalidModel2Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("the entity named 'MyEntity' is missing the remote field definition");
		new RamlGenerator().generate("model/invalidOdataModel2.raml");
	}

	@Test
	public void invalidModel3Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("the field 'MyField' is missing the 'nullable' required property");
		new RamlGenerator().generate("model/invalidOdataModel3.raml");
	}

	@Test
	public void invalidModel4Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("a field name is missing");
		new RamlGenerator().generate("model/invalidOdataModel4.raml");
	}

	@Test
	public void invalidModel5Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("no types definition where found, please check the model");
		new RamlGenerator().generate("model/invalidOdataModel5.raml");
	}

	@Test
	public void invalidModel6Test() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		thrown.expectMessage("the entity named 'MyEntity' has no properties defined");
		new RamlGenerator().generate("model/invalidOdataModel6.raml");
	}

	public static String readFromFile(String filePath) throws FileNotFoundException, IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
		File file = new File(url.getPath());
		InputStream is = new FileInputStream(file);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		is.close();
		return writer.toString();
	}

}
