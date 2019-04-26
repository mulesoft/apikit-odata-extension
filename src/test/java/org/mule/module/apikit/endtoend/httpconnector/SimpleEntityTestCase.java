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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class SimpleEntityTestCase extends MuleArtifactFunctionalTestCase
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
    public void getOrdersPositive() throws Exception
    {
        given().header("Accept", "application/json")
        .expect().statusCode(200)
        .when().get("api/orders?$format=json");
    	
    	given().header("Accept", "application/json")
            .expect()
                .response().body("d.results.OrderID", hasItems(10248, 10249))
                .header("Content-type", "application/json").statusCode(200)
            .when().get("api/odata.svc/orders?$format=json");
    	
    	given()
        .expect()
            .response().body("d.results.OrderID", hasItems(10248, 10249))
            .header("Content-type", "application/json").statusCode(200)
        .when().get("api/odata.svc/orders?$format=json");
    }
    
    @Test
    public void gettingNonExistentEntityShouldResponse404Code() throws Exception
    {
    	given().header("Accept", "application/json")
            .expect()
                .header("Content-type", "application/json").statusCode(404)
            .when().get("api/odata.svc/nonExistentProperty");
    }
    
    // test the $count param
    @Test
    public void countCollections() throws Exception
    {
    	// send a wrong MIME-type in the header
        given().header("Accept", "application/json")
        .expect()
        	.response().body("\"odata.error\".message.value", containsString("Unsupported media type requested"))
        .when().get("api/odata.svc/orders/$count");
        
        // send the right MIME-type in the header
        given().header("Accept", "text/plain")
        .expect()
            .response().body(equalTo("2"))
            .header("Content-Type", containsString("text/plain"))
        .when().get("api/odata.svc/orders/$count");
        
        // send no headers at all
        given()
        .expect()
            .response().body(equalTo("2"))
            .header("Content-Type", containsString("text/plain"))
        .when().get("api/odata.svc/orders/$count");
        
        // send wrong $format in query
        given()
        .expect()
        	.response().body("\"odata.error\".message.value", containsString("Unsupported media type requested"))
        .when().get("api/odata.svc/orders/$count?$format=json");
    }
    
    @Test
    public void getOrderPositive() throws Exception
    {
        given().header("Accept", "application/json")
        .expect()	
        	.response().body("d.results.__metadata.uri", hasItems("http://127.0.0.1:" + serverPort.getNumber() + "/api/odata.svc/orders(10248)"))
            .header("Content-type", "application/json").statusCode(200)
        .when().get("api/odata.svc/orders(10248)");
    }
    
}
