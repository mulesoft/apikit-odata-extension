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
import static org.hamcrest.CoreMatchers.containsString;

public class ErrorHandlerTestCase extends MuleArtifactFunctionalTestCase
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
    public void unsupportedMediaTypeRequested() throws Exception
    {
        given()
        .expect()
        	.response().body("error.message", containsString("Incorrect format for $format argument 'jsol'."))
            .header("Content-type", "application/xml").statusCode(400)
        .when().get("api/odata.svc/orders?$format=jsol");
    	
        given().header("Accept", "application/jsol")
        .expect()
        	.response().body("error.message", containsString("Unsupported media type requested."))
            .header("Content-type", "application/xml").statusCode(415)
        .when().get("api/odata.svc/orders");     
    }
    
    @Test
    public void xmlRequest() throws Exception
    {
        given().header("Accept", "application/xml")
        .expect()
        	.response().body("\"odata.error\".message.value", containsString("Entity ordd not found."))
            .header("Content-type", "application/json").statusCode(404)
        .when().get("api/odata.svc/ordd?$format=json");
    	
        given().header("Accept", "application/xml")
        .expect()
        	.response().body("error.message", containsString("Entity ordd not found."))
            .header("Content-type", "application/xml").statusCode(404)
        .when().get("api/odata.svc/ordd");     
    }

    @Test
    public void jsonRequest() throws Exception
    {
        given().header("Accept", "application/json")
        .expect()
        	.response().body("error.message", containsString("Entity ordd not found."))
            .header("Content-type", "application/xml").statusCode(404)
        .when().get("api/odata.svc/ordd?$format=xml");
    	
        given().header("Accept", "application/json")
        .expect()
        	.response().body("\"odata.error\".message.value", containsString("Entity ordd not found."))
            .header("Content-type", "application/json").statusCode(404)
        .when().get("api/odata.svc/ordd");     
    }
}
