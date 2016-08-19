/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EntityModelTestCase {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testPositive() throws Exception {
		Entity e = new Entity("customer");
		
		Assert.assertEquals("customer", e.getName());
		Assert.assertEquals("customers", e.getCollectionName());
		Assert.assertEquals("customer", e.getElementName());
	
		e = new Entity("customers");
		Assert.assertEquals("customers", e.getName());
		Assert.assertEquals("customers", e.getCollectionName());
		Assert.assertEquals("customers", e.getElementName());
		
		e = new Entity("Customers");
		Assert.assertEquals("Customers", e.getName());
		Assert.assertEquals("Customers", e.getCollectionName());
		Assert.assertEquals("Customers", e.getElementName());
		
		e = new Entity("Baby");
		Assert.assertEquals("Baby", e.getName());
		Assert.assertEquals("Babies", e.getCollectionName());
		Assert.assertEquals("Baby", e.getElementName());
		
		e = new Entity("Babies");
		Assert.assertEquals("Babies", e.getName());
		Assert.assertEquals("Babies", e.getCollectionName());
		Assert.assertEquals("Babies", e.getElementName());
	}

}
