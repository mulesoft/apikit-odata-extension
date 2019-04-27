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

public class ServiceDocumentTestCase extends MuleArtifactFunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigFile()
    {
	return "org/mule/module/apikit/odata/odata.xml";
    }

    
    @Test
    public void serviceSheetJsonPositive() throws Exception
    {
        given().header("Accept", "application/json")
            .expect()
                .response().body("d.EntitySets", hasItems("orders", "customers"))
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/api/odata.svc?$format=json");
    }
    
    @Test
    public void serviceSheetXmlPositive() throws Exception
    {
        given().header("Accept", "application/xml")
            .expect()
                .response().body("service.workspace.collection", hasItems("orders"))
                .statusCode(200)
            .when().get("/api/odata.svc");
    }

    @Test
    public void serviceSheetJsonNegativePost(){
        given().header("Accept", "application/json")
                .expect()
                .statusCode(405)
                .when().post("/api/odata.svc");
    }

    @Test
    public void serviceSheetJsonNegativePut(){
        given().header("Accept", "application/json")
                .expect()
                .statusCode(405)
                .when().put("/api/odata.svc");
    }

    @Test
    public void serviceSheetJsonNegativeDelete(){
        given().header("Accept", "application/json")
                .expect()
                .statusCode(405)
                .when().delete("/api/odata.svc");
    }

    @Test
    public void serviceSheetXmlNegativePost(){
        given().header("Accept", "application/xml")
                .expect()
                .statusCode(405)
                .when().post("/api/odata.svc");
    }


    @Test
    public void serviceSheetXmlNegativePut(){
        given().header("Accept", "application/xml")
                .expect()
                .statusCode(405)
                .when().put("/api/odata.svc");
    }

    @Test
    public void serviceSheetXmlNegativeDelete(){
        given().header("Accept", "application/xml")
                .expect()
                .statusCode(405)
                .when().delete("/api/odata.svc");
    }

}
