/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.processor;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class HttpListenerODataFormatHandlerTestCase extends MuleArtifactFunctionalTestCase
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
    public void defaultFormatTest() throws Exception
    {
        given()
            .expect()
                .header("Content-type", "application/atom+xml").statusCode(200)
            .when().get("/api/odata.svc/orders");
    }
    
    @Test
    public void atomHeaderFormatTest() throws Exception
    {
        given().header("Accept", "application/xml")
            .expect()
                .header("Content-type", "application/atom+xml").statusCode(200)
            .when().get("/api/odata.svc/orders");
        
        given().header("Accept", "application/atom+xml")
        	.expect()
            	.header("Content-type", "application/atom+xml").statusCode(200)
            .when().get("/api/odata.svc/orders");

        given().header("Accept", "application/xml")
	    	.expect()
	        	.header("Content-type", "application/atom+xml").statusCode(200)
	        .when().get("/api/odata.svc/orders");
        
        given().header("Accept", "application/atom+xml, application/atomsvc+xml, application/xml")
	    	.expect()
	        	.header("Content-type", "application/atom+xml").statusCode(200)
	        .when().get("/api/odata.svc/orders");
    }
    
    @Test
    public void jsonHeaderFormatTest() throws Exception
    {
        given().header("Accept", "application/json")
            .expect()
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/api/odata.svc/orders"); 
    }
    
    @Test
    public void jsonQueryFormatTest() throws Exception
    {
        given()
            .expect()
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/api/odata.svc/orders?$format=json"); 
        
        given().header("Accept", "application/xml")
	        .expect()
	            .header("Content-type", "application/json").statusCode(200)
	        .when().get("/api/odata.svc/orders?$format=json");
    }
    
    @Test
    public void atomQueryFormatTest() throws Exception
    {
        given()
            .expect()
                .header("Content-type", "application/atom+xml").statusCode(200)
            .when().get("/api/odata.svc/orders?$format=atom"); 
        
        given().header("Accept", "application/json")
	        .expect()
	            .header("Content-type", "application/atom+xml").statusCode(200)
	        .when().get("/api/odata.svc/orders?$format=atom");
    }
    
    @Test
    public void invalidQueryFormatTest() throws Exception
    {
        given()
            .expect()
                .header("Content-type", "application/xml").statusCode(400)
            .when().get("/api/odata.svc/orders?$format=atoc"); 
        
        given().header("Accept", "application/xml")
	        .expect()
	            .header("Content-type", "application/xml").statusCode(400)
	        .when().get("/api/odata.svc/orders?$format=atoc");
    }
    
    @Test
    public void invalidHeaderFormatTest() throws Exception
    {
        given().header("Accept", "application/xol")
            .expect()
                .header("Content-type", "application/xml").statusCode(415)
            .when().get("/api/odata.svc/orders");  
    }
    
}

