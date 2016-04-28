/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.DefaultMuleContext;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleConfiguration;
import org.mule.module.apikit.odata.exception.ClientErrorException;
import org.mule.module.apikit.odata.exception.ODataBadRequestException;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;

public class ODataApikitProcessorTestCase {

	MuleConfiguration mockedConfiguration;
	MuleContext muleContext;

	@Before
	public void setUp(){
		muleContext = Mockito.mock(MuleContext.class);

		mockedConfiguration = Mockito.mock(MuleConfiguration.class);
		Mockito.when(mockedConfiguration.getDefaultEncoding()).thenReturn("UTF-8");

		Mockito.when(muleContext.getConfiguration()).thenReturn(mockedConfiguration);
	}

	@Test
	public void testCheckResponseHttpStatusPositive() throws ODataInvalidFormatException, ODataBadRequestException, ClientErrorException {

		// Sunny int scenario
		int a = 200;

		MuleMessage intStatusMessage = new DefaultMuleMessage("", muleContext);
		intStatusMessage.setOutboundProperty("http.status", a);
		ODataApikitProcessor.checkResponseHttpStatus(intStatusMessage);

		// Sunny String scenario
		String b = "200";
		MuleMessage stringStatusMessage = new DefaultMuleMessage("", muleContext);
		stringStatusMessage.setOutboundProperty("http.status", b);
		ODataApikitProcessor.checkResponseHttpStatus(stringStatusMessage);

		// Sunny long scenario
		long c = 200;
		MuleMessage longStatusMessage = new DefaultMuleMessage("", muleContext);
		longStatusMessage.setOutboundProperty("http.status", c);
		ODataApikitProcessor.checkResponseHttpStatus(longStatusMessage);

		// Sunny long scenario
		Set<String> d = new HashSet<String>();
		MuleMessage setStatusMessage = new DefaultMuleMessage("", muleContext);
		setStatusMessage.setOutboundProperty("http.status", d);
		ODataApikitProcessor.checkResponseHttpStatus(setStatusMessage);

	}

	@Test
	public void testCheckResponseHttpStatusNegative() throws ODataInvalidFormatException, ODataBadRequestException {
		String errorMessage = "This is an error message";
		try {
			// Sunny int scenario
			int a = 401;
			MuleMessage intStatusMessage = new DefaultMuleMessage(errorMessage, muleContext);
			intStatusMessage.setOutboundProperty("http.status", a);
			ODataApikitProcessor.checkResponseHttpStatus(intStatusMessage);
			fail("Exception expected");
		} catch (ClientErrorException e) {
			//exception expected
			assertTrue(errorMessage.equals(e.getMessage()));
		}

		try {
			// Sunny String scenario
			String b = "401";
			MuleMessage stringStatusMessage = new DefaultMuleMessage(errorMessage, muleContext);
			stringStatusMessage.setOutboundProperty("http.status", b);
			ODataApikitProcessor.checkResponseHttpStatus(stringStatusMessage);
			fail("Exception expected");
		} catch (ClientErrorException e) {
			//exception expected
			assertTrue(errorMessage.equals(e.getMessage()));
		}

		try {
			// Sunny long scenario
			long c = 401;
			MuleMessage longStatusMessage = new DefaultMuleMessage(errorMessage, muleContext);
			longStatusMessage.setOutboundProperty("http.status", c);
			ODataApikitProcessor.checkResponseHttpStatus(longStatusMessage);
			fail("Exception expected");
		} catch (ClientErrorException e) {
			//exception expected
			assertTrue(errorMessage.equals(e.getMessage()));
		}

	}
}
