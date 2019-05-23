/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.processor;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class ODataApikitRequestEntityWithDateTimeTestCase extends MuleArtifactFunctionalTestCase {
  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    System.setProperty("java.net.preferIPv4Stack", "true");
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/odata/processor/apikit-processor-with-datetime.xml";
  }

  @Test
  public void getOrdersPositive() throws Exception {
    given().header("Accept", "application/json").expect().response()
        .body("d.results.OrderID", hasItems(10248, 10249))
        .header("Content-type", "application/json").statusCode(200).when()
        .get("api/odata.svc/orders?$format=json");
  }

  @Test
  public void getOrderByIdJsonOutput() throws Exception {
    given().header("Accept", "application/json").expect().response()
        .body("d.results.OrderID", hasItem(10248)).header("Content-type", "application/json")
        .statusCode(200).when().get("api/odata.svc/orders(10248)?$format=json");
  }

  @Test
  public void getOrderByIdNegative() throws Exception {
    given().header("Accept", "application/json").expect().statusCode(500).when()
        .get("api/odata.svc/orders(10250)?$format=json");
  }

  @Test
  public void getOrderByIdAtomOutput() throws Exception {
    given().expect().response()
        .body("feed.entry.content.properties.OrderID", containsString("10248"))
        .header("Content-type", "application/atom+xml").statusCode(200).when()
        .get("api/odata.svc/orders(10248)?$format=atom");
  }

  @Test
  public void getOrderByIdAtomOutputNegative() throws Exception {
    given().header("Accept", "application/xml").expect().header("Content-type", "application/xml")
        .statusCode(500).when().get("api/odata.svc/orders(20248)?$format=atom");
  }

  @Test
  public void settingJsonFormatUsingHeader() throws Exception {
    given().header("Accept", "application/json").expect().response()
        .body("d.results.OrderID", hasItem(10248)).header("Content-type", "application/json")
        .statusCode(200).when().get("api/odata.svc/orders(10248)");
  }

  @Test
  public void settingAtomFormatUsingHeader() throws Exception {
    given().header("Accept", "application/xml").expect().response()
        .body("eed.entry.content.properties.OrderID", containsString("10248"))
        .header("Content-type", "application/atom+xml").statusCode(200).when()
        .get("api/odata.svc/orders(10248)");
  }

  @Test
  public void settingJsonFormatWithFormatPriority() throws Exception {
    given().header("Accept", "application/xml").expect().response()
        .body("d.results.OrderID", hasItem(10248)).header("Content-type", "application/json")
        .statusCode(200).when().get("api/odata.svc/orders(10248)?$format=json");
  }

  @Test
  public void settingAtomFormatWithoutHeader() throws Exception {
    given().expect().response()
        .body("eed.entry.content.properties.OrderID", containsString("10248"))
        .header("Content-type", "application/atom+xml").statusCode(200).when()
        .get("api/odata.svc/orders(10248)?$format=atom");
  }

  @Test
  public void settingJsonFormatWithoutHeader() throws Exception {
    given().expect().response().body("d.results.OrderID", hasItem(10248))
        .header("Content-type", "application/json").statusCode(200).when()
        .get("api/odata.svc/orders(10248)?$format=json");
  }

  @Test
  public void creatingEntityUsingXMLBody() throws Exception {
    given().header("Accept", "application/xml")
        .body("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
            + "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">"
            + "  <title />" + "  <updated>2013-09-18T23:46:19.3857256Z</updated>" + "  <author>"
            + "    <name />" + "  </author>" + "  <id />" + "  <content type=\"application/xml\">"
            + "    <m:properties>" + "      <d:OrderID type=\"Edm.Int32\">20000</d:OrderID>"
            + "      <d:ShipName>\"ship-1\"</d:ShipName>"
            + "      <d:ShipAddress>ship-address-1</d:ShipAddress>" + "    </m:properties>"
            + "  </content>" + "</entry>")
        .expect().statusCode(200).when().post("api/odata.svc/orders");
  }

  @Test
  public void creatingEntityUsingXMLBodyNegative() throws Exception {
    given().header("Accept", "application/json")
        .body("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
            + "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">"
            + "  <title />" + "  <updated>2013-09-18T23:46:19.3857256Z</updated>" + "  <author>"
            + "    <name />" + "  </author>" + "  <id />" + "  <content type=\"application/xml\">"
            + "    <m:properties>" + "      <d:OrderID>20000</d:OrderID>"
            + "      <d:ShipName>ship-name-1</d:ShipName>"
            + "      <d:ShipAddress>ship-address-1</d:ShipAddress>" + "    </m:properties>"
            + "  </content>" + "</entry>")
        .expect().statusCode(400).when().post("api/odata.svc/orders");
  }

  @Test
  public void creatingEntityUsingJSONBody() throws Exception {
    given().header("Accept", "application/json")
        .body("{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-1\"}")
        .expect().statusCode(200).when().post("api/odata.svc/orders");
  }

  @Test
  public void creatingEntityUsingJSONBodyWithNullProps() throws Exception {
    given().header("Accept", "application/json")
        .body("{\"OrderID\":20000,\"ShipAddress\": null,\"ShipName\":\"ship-1\"}").expect()
        .statusCode(200).when().post("api/odata.svc/orders");
  }

  @Test
  public void creatingEntityUsingJSONBodyWithUndefinedProps() throws Exception {
    given().header("Accept", "application/json").body("{\"OrderID\":20000,\"ShipName\":\"ship-1\"}")
        .expect().statusCode(200).when().post("api/odata.svc/orders");
  }

  @Test
  public void creatingEntityUsingJSONBodyNegative() throws Exception {
    given().header("Accept", "application/xml")
        .body("{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-name-1\"}")
        .expect().statusCode(400).when().post("api/odata.svc/orders");
  }

  @Test
  public void gettingOrderWithLessPropertiesShouldNotReturnError() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).when().get("api/odata.svc/orders(10249)");
  }

  @Test
  public void gettingOrderWithUnknownPropertyShouldReturnError() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(500).when().get("api/odata.svc/orders(10250)");
  }

  @Test
  public void filterByOrderDateTestCase() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when()
        .get("api/odata.svc/orders?$filter=OrderDate eq datetime'2017-03-01T00:00:00'");
  }

  @Test
  public void filterByOrderDateAndShipNameTestCase() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when().get(
            "api/odata.svc/orders?$filter=OrderDate eq datetime'2017-03-01T00:00:00' and ShipName eq 'pepe'");
  }

  @Test
  public void filterByShipNameAndOrderDateTestCase() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when().get(
            "api/odata.svc/orders?$filter=ShipName eq 'pepe' and OrderDate eq datetime'2017-03-01T00:00:00'");
  }

  @Test
  public void testLogicalOperators() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when().get(
            "api/odata.svc/orders?$filter=ShipName eq 'pepe' and (ShipName eq 'pepe' or ShipName eq 'pepe')");
  }

  @Test
  public void testLogicalOperators2() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when().get(
            "api/odata.svc/orders?$filter=(ShipName eq 'pepe' or ShipName eq 'pepe') and ShipName eq 'pepe'");
  }

  @Test
  public void filterByOrderDateOffsetTestCase() throws Exception {
    given().header("Accept", "application/json").expect().header("Content-type", "application/json")
        .statusCode(200).log().ifError().when()
        .get("api/odata.svc/orders?$filter=OrderDate eq datetime'2017-03-01T00:00:00'");
  }

}
