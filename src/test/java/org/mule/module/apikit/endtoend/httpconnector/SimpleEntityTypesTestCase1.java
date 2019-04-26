/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.endtoend.httpconnector;

import com.jayway.restassured.RestAssured;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class SimpleEntityTypesTestCase1 extends MuleArtifactFunctionalTestCase {

	@Rule
	public DynamicPort serverPort = new DynamicPort("serverPort");

	@Override
	public int getTestTimeoutSecs() {
		return 6000;
	}

	@Override
	protected void doSetUp() throws Exception {
		RestAssured.port = serverPort.getNumber();
		super.doSetUp();
	}

	@Override
	protected String getConfigFile() {
		return "org/mule/module/apikit/odata/datatypes/odata1.xml";
	}

	@Test
	public void apikitRestDefaultError() throws Exception {
		given().header("Accept", "application/xml").expect().header("Content-type", "application/json").statusCode(404).when().get("api/dsdadorders");
	}
	
	@Test
	public void doubleDataTypeTest1() throws Exception {
		given().header("Accept", "application/xml").expect().header("Content-type", "application/xml").statusCode(200).when().get("api/odata.svc/orders");
	}

}
