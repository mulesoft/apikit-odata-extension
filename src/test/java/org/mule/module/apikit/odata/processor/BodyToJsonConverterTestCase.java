/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import org.json.JSONObject;
import org.junit.Test;
import org.mule.module.apikit.odata.util.FileUtils;

import static org.junit.Assert.assertTrue;

/**
 * Created by arielsegura on 5/12/16.
 */
public class BodyToJsonConverterTestCase {


    @Test
    public void dateTimeInputWithoutQuotesShouldBeCastToDateTime() throws Exception {
        String input = FileUtils.readFromFile("org/mule/module/apikit/odata/processor/_api_create_order.xml");
        String json = BodyToJsonConverter.convertPayload(null, true, input);
        JSONObject jsonObject = new JSONObject(json);
        assertTrue(Double.compare((Double) jsonObject.get("Double"), 1.2) == 0);
        assertTrue(jsonObject.get("OrderDate").equals("1996-08-05 00:00:00"));
        assertTrue(Double.compare((Double) jsonObject.get("Freight"), 32.01) == 0);
        assertTrue(jsonObject.get("ShipAddress").equals("Direccion"));
        assertTrue(Integer.compare((Integer)jsonObject.get("SmallInt"), 1) == 0);
        assertTrue(jsonObject.get("ShipName").equals("Testing"));
        assertTrue(jsonObject.isNull("NullValue"));
        assertTrue(jsonObject.getString("String").equals("111"));
    }
}
