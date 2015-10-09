/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
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
