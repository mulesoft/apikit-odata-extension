/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.model.EntityModelParser;
import org.mule.module.apikit.model.FileUtils;
import org.mule.module.apikit.model.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * 
 * @author arielsegura
 */
public class ODataScaffolderServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public ODataScaffolderService scaffolder;
	public String RESOURCES_PATH = "src/test/resources/";

	@Before
	public void setUp() throws Exception {
		scaffolder = new ODataScaffolderService();
	}

	private File getFile(String path) {
		try {
			System.out.println((new File("src/test/resources")).getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = new File((RESOURCES_PATH + path).replace("/", File.separator));
		return file;
	}

	@Test
	public void scaffoldPositive() {

		List<File> ramlFiles = new ArrayList<File>();

		File modelJson = getFile("valid/api/model.json");
		File appDir = getFile("valid/app");
		File domainDir = getFile("valid/domain");

		ramlFiles.add(modelJson);

		scaffolder.executeScaffolder(ramlFiles, appDir, domainDir);
	}

	@Test
	public void scaffoldNegative() {

		List<File> ramlFiles = new ArrayList<File>();

		File modelJson = getFile("invalid/api/model.json");
		File appDir = getFile("invalid/app");
		File domainDir = getFile("invalid/domain");

		ramlFiles.add(modelJson);

		thrown.expect(RuntimeException.class);
		scaffolder.executeScaffolder(ramlFiles, appDir, domainDir);

	}

}
