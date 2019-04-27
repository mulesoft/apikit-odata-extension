/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.endtoend.httpconnector;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;

public class MultipleModelsTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Rule
  public DynamicPort serverPort2 = new DynamicPort("serverPort2");

  @Override
  protected String[] getConfigFiles()
  {
    final String[] configs = {"org/mule/module/apikit/odata/odata.xml","org/mule/module/apikit/odata/odata-for-multiple-model-test.xml"};
    return configs;
  }

  @Test
  public void checkMultipleModels() throws Exception {
    given().port(serverPort.getNumber()).header("Accept", "application/xml").expect().response()
            .body("Edmx.DataServices.Schema.EntityType.size()", Matchers.equalTo(2))
            .header("Content-type", "application/xml").statusCode(200).when().get("/api/odata.svc/$metadata");


    given().port(serverPort2.getNumber()).header("Accept", "application/xml").expect().response()
            .body("Edmx.DataServices.Schema.EntityType.size()", Matchers.equalTo(1))
            .header("Content-type", "application/xml").statusCode(200).when().get("/api/odata.svc/$metadata");
  }

}
