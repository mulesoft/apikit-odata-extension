/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mule.module.apikit.odata.exception.ClientErrorException;
import org.mule.module.apikit.odata.exception.ODataBadRequestException;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;

public class ODataApikitProcessorTestCase {
	
	@Test
	public void testCheckResponseHttpStatusPositive() throws ODataInvalidFormatException, ODataBadRequestException, ClientErrorException {
		
		// Sunny int scenario
		int a = 200;
		ODataApikitProcessor.checkResponseHttpStatus(a);

		// Sunny String scenario
		String b = "200";
		ODataApikitProcessor.checkResponseHttpStatus(b);

		// Sunny long scenario
		long c = 200;
		ODataApikitProcessor.checkResponseHttpStatus(c);

		// Sunny long scenario
		Set<String> d = new HashSet<String>();
		ODataApikitProcessor.checkResponseHttpStatus(d);
		
	}

	@Test
	public void testCheckResponseHttpStatusNegative() throws ODataInvalidFormatException, ODataBadRequestException {
		
		try {
  		// Sunny int scenario
  		int a = 401;
  		ODataApikitProcessor.checkResponseHttpStatus(a);
  		fail("Exception expected");
		} catch (ClientErrorException e) {
			//exception expected
		}

		try {
  		// Sunny String scenario
  		String b = "401";
  		ODataApikitProcessor.checkResponseHttpStatus(b);
  		fail("Exception expected");
  	} catch (ClientErrorException e) {
			//exception expected
  	}
  	
		try {
  		// Sunny long scenario
  		long c = 401;
  		ODataApikitProcessor.checkResponseHttpStatus(c);
  		fail("Exception expected");
  	} catch (ClientErrorException e) {
			//exception expected
  	}
		
	}
}
