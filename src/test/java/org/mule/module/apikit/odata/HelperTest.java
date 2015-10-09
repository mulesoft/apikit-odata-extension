/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
/*
* Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.GatewayMetadataResourceNotFound;

public class HelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@Ignore
	@Test
	public void test() throws GatewayMetadataFieldsException, GatewayMetadataResourceNotFound, JSONException, IOException, GatewayMetadataFormatException {
		// Assert.assertEquals(Helper.refreshMetadataManager("datagateway-definition.raml").getEntitySet(),
		// Helper.refreshMetadataManager(new
		// URL("http://localhost:8081/api/config")).getEntitySet());
	}
}
