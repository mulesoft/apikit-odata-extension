/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.endtoend.httpconnector;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class MetadataTestCase extends MuleArtifactFunctionalTestCase {

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
		return "org/mule/module/apikit/odata/odata.xml";
	}

	@Test
	public void metadataPositive() throws Exception {
		given().header("Accept", "application/xml").expect().response().body("Edmx.DataServices.Schema.EntityType.@Name", hasItems("orders"))
				.header("Content-type", "application/xml").statusCode(200).when().get("/api/odata.svc/$metadata");
	}

	@Test
	public void metadataJsonNegativePut() {
		given().header("Accept", "application/json").expect().statusCode(405).when().put("/api/odata.svc/$metadata");
	}

	@Test
	public void metadataJsonNegativeDelete() {
		given().header("Accept", "application/json").expect().statusCode(405).when().delete("/api/odata.svc/$metadata");
	}

	@Test
	public void metadataXmlNegativePost() {
		given().header("Accept", "application/xml").expect().statusCode(405).when().post("/api/odata.svc/$metadata");
	}

	@Test
	public void metadataXmlNegativePut() {
		given().header("Accept", "application/xml").expect().statusCode(405).when().put("/api/odata.svc/$metadata");
	}

	@Test
	public void metadataXmlNegativeDelete() {
		given().header("Accept", "application/xml").expect().statusCode(405).when().delete("/api/odata.svc/$metadata");
	}

}
