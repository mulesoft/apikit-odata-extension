/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.odata.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;

public class ODataUriParserTestCase {
	@Test
	public void sendingXMLAsBodyReturnsValidJSON() throws ODataInvalidFormatException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
				+ "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">"
				+ "  <title />" + "  <updated>2013-09-18T23:46:19.3857256Z</updated>" + "  <author>" + "    <name />" + "  </author>" + "  <id />"
				+ "  <content type=\"application/xml\">" + "    <m:properties>" + "      <d:OrderID>20000</d:OrderID>" + "      <d:ShipName>ship-name-1</d:ShipName>"
				+ "      <d:ShipAddress>ship-address-1</d:ShipAddress>" + "    </m:properties>" + "  </content>" + "</entry>";

		String actual = XmlBodyToJSonConverter.convertXMLPayloadIfRequired(true, xml);

		String expected = "{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-name-1\"}";

		assertEquals(expected, actual);
	}

	@Test
	public void sendingJSONAsBodyReturnsSameJSON() throws ODataInvalidFormatException {
		String input = "{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-name-1\"}";

		String actual = XmlBodyToJSonConverter.convertXMLPayloadIfRequired(false, input);

		String expected = "{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-name-1\"}";

		assertEquals(expected, actual);
	}

	@Test
	public void sendingXMLAsBodyButJsonAsContentTypeThrowsException() {
		try {
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
					+ "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">"
					+ "  <title />" + "  <updated>2013-09-18T23:46:19.3857256Z</updated>" + "  <author>" + "    <name />" + "  </author>" + "  <id />"
					+ "  <content type=\"application/xml\">" + "    <m:properties>" + "      <d:OrderID>20000</d:OrderID>" + "      <d:ShipName>ship-name-1</d:ShipName>"
					+ "      <d:ShipAddress>ship-address-1</d:ShipAddress>" + "    </m:properties>" + "  </content>" + "</entry>";

			XmlBodyToJSonConverter.convertXMLPayloadIfRequired(false, xml);
			fail("Exception expected");
		} catch (ODataInvalidFormatException e) {
		}
	}

	@Test
	public void sendingJSONAsBodyButXMLasContentTypeThrowsException() {
		try {
			String input = "{\"OrderID\":20000,\"ShipAddress\":\"ship-address-1\",\"ShipName\":\"ship-name-1\"}";

			XmlBodyToJSonConverter.convertXMLPayloadIfRequired(true, input);
			fail("Exception expected");
		} catch (ODataInvalidFormatException e) {
		}
	}
}
