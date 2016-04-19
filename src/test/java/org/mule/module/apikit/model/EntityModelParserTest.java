/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.model.exception.EntityModelParsingException;

/**
 * 
 * @author arielsegura
 */
public class EntityModelParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testPositive() throws IOException, EntityModelParsingException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("model/validOdataModel.raml");
		File file = new File(url.getPath());
		InputStream inputStream = new FileInputStream(file);

		List<Entity> entities = new EntityModelParser().getEntities(inputStream);
		Assert.assertEquals("Employee", entities.get(0).getName());
		Assert.assertEquals("Employee", entities.get(0).getElementName());
		Assert.assertEquals("Employees", entities.get(0).getCollectionName());
		Assert.assertEquals("EmployeeId", entities.get(0).getIdElementName());
		Assert.assertEquals("Customer", entities.get(1).getName());
		Assert.assertEquals("Customer", entities.get(1).getElementName());
		Assert.assertEquals("Customers", entities.get(1).getCollectionName());
		Assert.assertEquals("CustomerId", entities.get(1).getIdElementName());

	}

}
