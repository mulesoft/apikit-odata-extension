/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.exception.ODataUnsupportedMediaTypeException;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.processor.ODataApikitProcessor;
import org.mule.module.apikit.odata.processor.ODataMetadataProcessor;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.odata.processor.ODataServiceDocumentProcessor;
import org.mule.module.apikit.odata.util.ODataUriHelper;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ODataUriParserTestCase {

	private static OdataContext oDataContext;

	@BeforeClass
	public static void setUp() throws ODataException {
		final OdataMetadataManager odataMetadataManager = new OdataMetadataManager("src/test/resources/org/mule/module/apikit/odata/api-mk.raml", true);
		oDataContext = new OdataContext(odataMetadataManager, "GET");
	}

	// should parse a $metadata request and return a Metadata processor
	@Test
	public void parseMetadataRequest() throws ODataException {
		ODataRequestProcessor processor = org.mule.module.apikit.odata.ODataUriParser.parse(oDataContext, "/odata.svc/$metadata", "");
		assertTrue(processor instanceof ODataMetadataProcessor);
	}

	// should parse a '/' request and return a Service Document processor
	@Test
	public void parseServiceDocumentRequestTrailingSlash() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/", "");
		assertTrue(processor instanceof ODataServiceDocumentProcessor);
	}

	// should parse an empty request and return a Service Document processor
	@Test
	public void parseServiceDocumentRequest() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc", "");
		assertTrue(processor instanceof ODataServiceDocumentProcessor);
	}

	// should parse a /orders/$count request and return an Apikit processor
	// pointing to /orders and set as entityCount = true
	@Test
	public void parseEntityCountRequest() throws ODataException {
		ODataApikitProcessor processor = (ODataApikitProcessor) ODataUriParser.parse(oDataContext, "/odata.svc/orders/$count", "");

		assertTrue(processor.isEntityCount());
		assertEquals("/orders", processor.getPath());
	}

	// should reject a $count request with a $format, since $count always returns
	// text/plain
	@Test(expected = ODataUnsupportedMediaTypeException.class)
	public void rejectUnsupportedFormatForCountRequest() throws ODataException {
		ODataUriParser.parse(oDataContext, "/odata.svc/orders/$count", "$format=json");
	}

	// a non-collection resource should not accept a $count request
	@Test
	public void rejectCountForNonCollectionResourceRequest() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)/$count", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should parse a /orders request and return an Apikit processor pointing to
	// /orders
	@Test
	public void parseEntityRequest() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/orders", apikitProcessor.getPath());
	}

	// should parse a /orders(1) request and return an Apikit processor pointing to
	// /orders/1
	@Test
	public void parseEntityRequestIntKey() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/orders/1", apikitProcessor.getPath());
	}

	// should parse a /orders(1234L) request and return an Apikit processor pointing to
	// /orders/1234L
	@Test
	public void parseEntityRequestInt64Key() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1234L)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/orders/1234", apikitProcessor.getPath());
	}

	// should parse a /orders('juan') request and return an Apikit processor
	// pointing to /orders/juan
	@Test
	public void parseEntityRequestStringKey() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/orders/juan", apikitProcessor.getPath());
	}

	// should reject a request to a resource with a key that's neither an int nor
	// a string
	@Test
	public void rejectInvalidFormatKeyRequest() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(broken)", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should parse a /orders(userId=3) request and return an Apikit processor
	// pointing to /orders/3
	@Test
	public void parseEntityRequestSimpleKey() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/orders/3", apikitProcessor.getPath());
	}

	// should parse a /customers(age=22,name='juan',zebra=1) request and return an
	// Apikit processor pointing to /customers/age_22-name_juan-zebra_1
	@Test
	public void parseEntityRequestCompositeKey() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(age=22,name='juan',zebra=1)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());
		HashMap<String, Object> keys = (HashMap<String, Object>) apikitProcessor.getKeys();
		String name = (String) keys.get("name");
		int age = Integer.parseInt((String) keys.get("age"));
		assertEquals(name, "juan");
		assertEquals(age, 22);
	}

	// should parse a /customers request and return an Apikit processor pointing
	// to /customers/
	@Test
	public void parseCollectionEntityRequestCompositeKey() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers", apikitProcessor.getPath());
	}

	// should parse odata requests with the same multiple keys in different order
	// and always return the same path, in alphabetized order
	@Test
	public void keepOrderOfCompositeKeys() throws ODataException {

		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(zebra=1,name='juan',age=22)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());

		processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(name='juan',zebra=1,age=22)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());

		processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(name='juan',age=22,zebra=1)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());

		processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(age=22,name='juan',zebra=1)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());

		processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(age=22,zebra=1,name='juan')", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());

		processor = ODataUriParser.parse(oDataContext, "/odata.svc/customers(zebra=1,age=22,name='juan')", "");
		assertTrue(processor instanceof ODataApikitProcessor);
		apikitProcessor = (ODataApikitProcessor) processor;
		assertEquals("/customers/age_22-name_juan-zebra_1", apikitProcessor.getPath());
	}

	// should reject repeated system query params
	@Test
	public void rejectRepeatedQueryParams() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top=1&$top=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject an invalid key
	@Test
	public void rejectInvalidKey() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(fakeKey=10)", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject keys that are not part of the model (in this case the model
	// for 'customers' has 2 primary keys: age and name)
	@Test
	public void rejectUnknownKey() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/customer(name='juan',fake=10)", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject a request with missing keys (in this case the model for
	// 'customers' has 2 primary keys: age and name)
	@Test
	public void rejectMissingKeys() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/customer(name='juan')", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject a request with extra keys (in this case the model for
	// 'customers' has 2 primary keys: age and name)
	@Test
	public void rejectExtraKeys() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/customer(name='juan',age=22,extra=1)", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject a key in an invalid format (i.e. missing the '=something')
	@Test
	public void rejectInvalidFormatKey() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/customer(name='juan',broken)", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should reject a query in an invalid format (i.e. missing the '=something')
	@Test
	public void rejectInvalidFormatQuery() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?broken");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should ignore params in a query that don't start with '$'
	@Test
	public void ignoreNonSystemReservedQueries() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	// should parse uri in different OData and REST formats and return a string
	// array with the entity and the id (if any)
	@Test
	public void parseOdataAndRestRequests() {

		String[] parsed = ODataUriHelper.parseRequest("/odata.svc/orders(1)");
		assertEquals("orders", parsed[0]);
		assertEquals("1", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/odata.svc/orders");
		assertEquals("orders", parsed[0]);
		assertEquals("", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/orders(1)");
		assertEquals("orders", parsed[0]);
		assertEquals("1", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/orders('juan')/");
		assertEquals("orders", parsed[0]);
		assertEquals("'juan'", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/orders");
		assertEquals("orders", parsed[0]);
		assertEquals("", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/orders/1");
		assertEquals("orders", parsed[0]);
		assertEquals("1", parsed[1]);

		parsed = ODataUriHelper.parseRequest("/orders/1/books/2");
		assertEquals("orders", parsed[0]);
		assertEquals("1", parsed[1]);
		assertEquals("books", parsed[2]);
		assertEquals("2", parsed[3]);
	}

	// should reject requests to non-existent entities
	@Test
	public void rejectNonExistentEntityRequest() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/nonExistentEntity", "");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	// should parse resources and keys with uppercase and lowercase characters,
	// underscores and dashes
	@Test
	public void parseUppercaseLowercaseUnderscoreAndDashResourceRequest() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/Weird-resource_NAMEs(just-A_key='bla',ANOTHER-strange_Key=2)/",
				"$format=json");

		assertTrue(processor instanceof ODataApikitProcessor);
		ODataApikitProcessor apikitProcessor = (ODataApikitProcessor) processor;

		String actual = apikitProcessor.getPath();
		String expected = "/Weird-resource_NAMEs/ANOTHER-strange_Key_2-just-A_key_bla";
		assertEquals(actual, expected);

		HashMap<String, Object> keys = (HashMap<String, Object>) apikitProcessor.getKeys();

		actual = (String) keys.get("just-A_key");
		expected = "bla";
		assertEquals(actual, expected);

		actual = (String) keys.get("ANOTHER-strange_Key");
		expected = "2";
		assertEquals(actual, expected);

		actual = apikitProcessor.getQuery();
		expected = "format=json";
		assertEquals(actual, expected);
	}

	// Generated Tests

	@Test
	public void parseEntityListNoQuery1() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryTop2() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top=1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryTopInlinecount3() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top=1&$inlinecount=none");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryTopAndIgnoreParams4() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top=1&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQuerySkip5() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$skip=4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQuerySkipOrderby6() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$skip=4&$orderby=Rating asc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQuerySkipAndIgnoreParams7() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$skip=4&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecount8() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=allpages");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecountSkip9() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=allpages&$skip=4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecountAndIgnoreParams10() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=allpages&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecount11() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=none");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecountSkip12() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=none&$skip=4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryInlinecountAndIgnoreParams13() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=none&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderby14() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyInlinecount15() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating&$inlinecount=allpages");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyAndIgnoreParams16() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderby17() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating asc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyInlinecount18() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating asc&$inlinecount=none");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyAndIgnoreParams19() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating asc&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderby20() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating,Category/Name desc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyInlinecount21() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating,Category/Name desc&$inlinecount=allpages");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseEntityListQueryOrderbyAndIgnoreParams22() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Rating,Category/Name desc&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat23() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat24() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat25() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect26() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect27() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect28() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand29() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand30() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand31() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter32() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Address/City eq 'Redmond'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter33() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Address/City ne 'London'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter34() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price gt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter35() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price ge 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter36() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price lt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter37() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price le 100");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter38() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price le 200 and Price gt 3.5");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter39() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price le 3.5 or Price gt 200");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter40() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=not endswith(Description,'milk')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter41() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price add 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter42() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price sub 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter43() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price mul 2 gt 2000");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter44() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price div 2 gt 4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter45() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=Price mod 2 eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter46() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=(Price sub 5) gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter47() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=substringof('Alfreds', CompanyName) eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter48() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=endswith(CompanyName, 'Futterkiste') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter49() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=startswith(CompanyName, 'Alfr') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter50() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=length(CompanyName) eq 19");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter51() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=indexof(CompanyName, 'lfreds') eq 1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter52() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter53() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter54() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter55() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=tolower(CompanyName) eq 'alfreds futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter56() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter57() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter58() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter59() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=day(BirthDate) eq 8");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter60() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=hour(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter61() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=minute(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter62() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=month(BirthDate) eq 12");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter63() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=second(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter64() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter65() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter66() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=round(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter67() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter68() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=floor(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter69() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=ceiling(Freight) eq 33d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter70() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=floor(Freight) eq 33");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter71() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=isof('NorthwindModel.Order')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter72() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=isof(ShipCountry, 'Edm.String')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListNoQuery73() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryTop74() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$top=1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryTopOrderby75() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$top=1&$orderby=Rating asc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryTopAndIgnoreParams76() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$top=1&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQuerySkip77() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$skip=4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQuerySkipInlinecount78() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$skip=4&$inlinecount=none");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQuerySkipAndIgnoreParams79() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$skip=4&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecount80() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=allpages");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecountOrderby81() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=allpages&$orderby=Rating,Category/Name desc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecountAndIgnoreParams82() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=allpages&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecount83() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=none");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecountSkip84() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=none&$skip=4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryInlinecountAndIgnoreParams85() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=none&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderby86() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyTop87() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating&$top=1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyAndIgnoreParams88() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderby89() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating asc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyInlinecount90() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating asc&$inlinecount=allpages");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyAndIgnoreParams91() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating asc&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderby92() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating,Category/Name desc");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyTop93() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating,Category/Name desc&$top=1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashEntityListQueryOrderbyAndIgnoreParams94() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Rating,Category/Name desc&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat95() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat96() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat97() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect98() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect99() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect100() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand101() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand102() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand103() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter104() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Address/City eq 'Redmond'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter105() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Address/City ne 'London'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter106() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price gt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter107() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price ge 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter108() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price lt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter109() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price le 100");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter110() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price le 200 and Price gt 3.5");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter111() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price le 3.5 or Price gt 200");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter112() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=not endswith(Description,'milk')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter113() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price add 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter114() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price sub 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter115() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price mul 2 gt 2000");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter116() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price div 2 gt 4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter117() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=Price mod 2 eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter118() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=(Price sub 5) gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter119() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=substringof('Alfreds', CompanyName) eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter120() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=endswith(CompanyName, 'Futterkiste') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter121() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=startswith(CompanyName, 'Alfr') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter122() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=length(CompanyName) eq 19");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter123() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=indexof(CompanyName, 'lfreds') eq 1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter124() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter125() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter126() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter127() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=tolower(CompanyName) eq 'alfreds futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter128() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter129() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter130() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter131() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=day(BirthDate) eq 8");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter132() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=hour(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter133() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=minute(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter134() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=month(BirthDate) eq 12");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter135() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=second(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter136() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter137() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter138() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=round(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter139() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter140() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=floor(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter141() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=ceiling(Freight) eq 33d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter142() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=floor(Freight) eq 33");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter143() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=isof('NorthwindModel.Order')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter144() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=isof(ShipCountry, 'Edm.String')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityNoQuery145() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat146() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatFilter147() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=json&$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatAndIgnoreParams148() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=json&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat149() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatFilter150() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$format=atom&$filter=tolower(CompanyName) eq 'alfreds futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatAndIgnoreParams151() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=atom&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormat152() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatFilter153() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=xml&$filter=floor(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFormatAndIgnoreParams154() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$format=xml&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect155() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectFilter156() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Price&$filter=Price mul 2 gt 2000");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectAndIgnoreParams157() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Price&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect158() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectFormat159() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Name,Category&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectAndIgnoreParams160() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Name,Category&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelect161() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectFilter162() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser
				.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Rating,Description,Price&$filter=day(BirthDate) eq 8");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQuerySelectAndIgnoreParams163() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Rating,Description,Price&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand164() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandSelect165() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandAndIgnoreParams166() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand167() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandFilter168() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser
				.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers&$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandAndIgnoreParams169() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpand170() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandFormat171() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers,Products&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryExpandAndIgnoreParams172() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Categories/Suppliers,Products&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter173() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City eq 'Redmond'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat174() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City eq 'Redmond'&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams175() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City eq 'Redmond'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter176() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City ne 'London'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat177() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City ne 'London'&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams178() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Address/City ne 'London'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter179() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price gt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat180() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price gt 20&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams181() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price gt 20&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter182() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price ge 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat183() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price ge 10&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams184() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price ge 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter185() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price lt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect186() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price lt 20&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams187() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price lt 20&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter188() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 100");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand189() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 100&$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams190() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 100&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter191() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 200 and Price gt 3.5");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect192() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 200 and Price gt 3.5&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams193() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 200 and Price gt 3.5&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter194() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 3.5 or Price gt 200");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat195() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 3.5 or Price gt 200&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams196() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price le 3.5 or Price gt 200&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter197() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=not endswith(Description,'milk')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat198() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=not endswith(Description,'milk')&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams199() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=not endswith(Description,'milk')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter200() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price add 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand201() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=Price add 5 gt 10&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams202() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price add 5 gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter203() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price sub 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat204() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price sub 5 gt 10&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams205() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price sub 5 gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter206() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mul 2 gt 2000");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect207() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mul 2 gt 2000&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams208() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mul 2 gt 2000&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter209() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price div 2 gt 4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat210() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price div 2 gt 4&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams211() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price div 2 gt 4&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter212() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mod 2 eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat213() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mod 2 eq 0&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams214() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=Price mod 2 eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter215() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=(Price sub 5) gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand216() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=(Price sub 5) gt 10&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams217() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=(Price sub 5) gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter218() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=substringof('Alfreds', CompanyName) eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand219() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=substringof('Alfreds', CompanyName) eq true&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams220() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=substringof('Alfreds', CompanyName) eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter221() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=endswith(CompanyName, 'Futterkiste') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect222() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=endswith(CompanyName, 'Futterkiste') eq true&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams223() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=endswith(CompanyName, 'Futterkiste') eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter224() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=startswith(CompanyName, 'Alfr') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect225() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=startswith(CompanyName, 'Alfr') eq true&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams226() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=startswith(CompanyName, 'Alfr') eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter227() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=length(CompanyName) eq 19");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat228() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=length(CompanyName) eq 19&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams229() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=length(CompanyName) eq 19&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter230() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=indexof(CompanyName, 'lfreds') eq 1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand231() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=indexof(CompanyName, 'lfreds') eq 1&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams232() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=indexof(CompanyName, 'lfreds') eq 1&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter233() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand234() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams235() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter236() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand237() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams238() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter239() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect240() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=substring(CompanyName, 1, 2) eq 'lf'&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams241() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=substring(CompanyName, 1, 2) eq 'lf'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter242() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=tolower(CompanyName) eq 'alfreds futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand243() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=tolower(CompanyName) eq 'alfreds futterkiste'&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams244() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=tolower(CompanyName) eq 'alfreds futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter245() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat246() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams247() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter248() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect249() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams250() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter251() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterSelect252() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams253() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter254() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=day(BirthDate) eq 8");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand255() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=day(BirthDate) eq 8&$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams256() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=day(BirthDate) eq 8&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter257() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=hour(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat258() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=hour(BirthDate) eq 0&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams259() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=hour(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter260() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=minute(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat261() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=minute(BirthDate) eq 0&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams262() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=minute(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter263() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=month(BirthDate) eq 12");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand264() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=month(BirthDate) eq 12&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams265() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=month(BirthDate) eq 12&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter266() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=second(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat267() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=second(BirthDate) eq 0&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams268() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=second(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter269() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat270() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=year(BirthDate) eq 1948&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams271() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=year(BirthDate) eq 1948&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter272() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat273() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams274() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter275() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand276() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams277() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter278() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat279() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams280() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=round(Freight) eq 32d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter281() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat282() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 32&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams283() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 32&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter284() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=ceiling(Freight) eq 33d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat285() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=ceiling(Freight) eq 33d&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams286() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=ceiling(Freight) eq 33d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter287() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 33");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterFormat288() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 33&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams289() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=floor(Freight) eq 33&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter290() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=isof('NorthwindModel.Order')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand291() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)",
				"?$filter=isof('NorthwindModel.Order')&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams292() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=isof('NorthwindModel.Order')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilter293() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=isof(ShipCountry, 'Edm.String')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterExpand294() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=isof(ShipCountry, 'Edm.String')&$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseSingleEntityQueryFilterAndIgnoreParams295() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=isof(ShipCountry, 'Edm.String')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityNoQuery296() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat297() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatFilter298() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=json&$filter=Price mod 2 eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatAndIgnoreParams299() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=json&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat300() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatFilter301() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=atom&$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatAndIgnoreParams302() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=atom&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormat303() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatFilter304() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$format=xml&$filter=endswith(CompanyName, 'Futterkiste') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFormatAndIgnoreParams305() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$format=xml&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect306() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectFilter307() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Price&$filter=length(CompanyName) eq 19");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectAndIgnoreParams308() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Price&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect309() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectFilter310() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$select=Name,Category&$filter=indexof(CompanyName, 'lfreds') eq 1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectAndIgnoreParams311() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Name,Category&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelect312() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectFilter313() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$select=Rating,Description,Price&$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQuerySelectAndIgnoreParams314() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$select=Rating,Description,Price&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand315() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandFilter316() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$expand=Categories&$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandAndIgnoreParams317() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand318() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandFilter319() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories/Suppliers&$filter=Price le 100");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandAndIgnoreParams320() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories/Suppliers&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpand321() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandFilter322() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$expand=Categories/Suppliers,Products&$filter=ceiling(Freight) eq 33d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryExpandAndIgnoreParams323() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$expand=Categories/Suppliers,Products&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter324() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Address/City eq 'Redmond'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand325() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=Address/City eq 'Redmond'&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams326() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Address/City eq 'Redmond'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter327() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Address/City ne 'London'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect328() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Address/City ne 'London'&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams329() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Address/City ne 'London'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter330() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price gt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat331() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price gt 20&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams332() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price gt 20&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter333() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price ge 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat334() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price ge 10&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams335() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price ge 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter336() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price lt 20");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect337() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price lt 20&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams338() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price lt 20&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter339() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 100");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat340() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 100&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams341() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 100&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter342() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 200 and Price gt 3.5");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat343() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 200 and Price gt 3.5&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams344() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 200 and Price gt 3.5&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter345() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 3.5 or Price gt 200");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect346() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=Price le 3.5 or Price gt 200&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams347() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price le 3.5 or Price gt 200&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter348() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=not endswith(Description,'milk')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect349() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=not endswith(Description,'milk')&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams350() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=not endswith(Description,'milk')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter351() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price add 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect352() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price add 5 gt 10&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams353() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price add 5 gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter354() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price sub 5 gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect355() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=Price sub 5 gt 10&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams356() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price sub 5 gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter357() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mul 2 gt 2000");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect358() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mul 2 gt 2000&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams359() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mul 2 gt 2000&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter360() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price div 2 gt 4");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat361() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price div 2 gt 4&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams362() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price div 2 gt 4&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter363() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mod 2 eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect364() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mod 2 eq 0&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams365() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=Price mod 2 eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter366() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=(Price sub 5) gt 10");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect367() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=(Price sub 5) gt 10&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams368() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=(Price sub 5) gt 10&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter369() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=substringof('Alfreds', CompanyName) eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand370() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substringof('Alfreds', CompanyName) eq true&$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams371() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substringof('Alfreds', CompanyName) eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter372() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=endswith(CompanyName, 'Futterkiste') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat373() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=endswith(CompanyName, 'Futterkiste') eq true&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams374() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=endswith(CompanyName, 'Futterkiste') eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter375() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=startswith(CompanyName, 'Alfr') eq true");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat376() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=startswith(CompanyName, 'Alfr') eq true&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams377() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=startswith(CompanyName, 'Alfr') eq true&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter378() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=length(CompanyName) eq 19");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat379() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=length(CompanyName) eq 19&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams380() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=length(CompanyName) eq 19&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter381() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=indexof(CompanyName, 'lfreds') eq 1");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat382() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=indexof(CompanyName, 'lfreds') eq 1&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams383() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser
				.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=indexof(CompanyName, 'lfreds') eq 1&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter384() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat385() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams386() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=replace(CompanyName, ' ', '') eq 'AlfredsFutterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter387() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand388() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams389() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substring(CompanyName, 1) eq 'lfreds Futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter390() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=substring(CompanyName, 1, 2) eq 'lf'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand391() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substring(CompanyName, 1, 2) eq 'lf'&$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams392() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=substring(CompanyName, 1, 2) eq 'lf'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter393() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=tolower(CompanyName) eq 'alfreds futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand394() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=tolower(CompanyName) eq 'alfreds futterkiste'&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams395() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=tolower(CompanyName) eq 'alfreds futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter396() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat397() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'&$format=atom");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams398() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=toupper(CompanyName) eq 'ALFREDS FUTTERKISTE'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter399() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect400() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams401() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=trim(CompanyName) eq 'Alfreds Futterkiste'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter402() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand403() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams404() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=concat(concat(City, ', '), Country) eq 'Berlin, Germany'&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter405() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=day(BirthDate) eq 8");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand406() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=day(BirthDate) eq 8&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams407() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=day(BirthDate) eq 8&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter408() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=hour(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat409() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=hour(BirthDate) eq 0&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams410() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=hour(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter411() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=minute(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand412() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=minute(BirthDate) eq 0&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams413() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=minute(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter414() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=month(BirthDate) eq 12");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand415() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=month(BirthDate) eq 12&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams416() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=month(BirthDate) eq 12&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter417() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=second(BirthDate) eq 0");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand418() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=second(BirthDate) eq 0&$expand=Categories/Suppliers");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams419() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=second(BirthDate) eq 0&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter420() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=year(BirthDate) eq 1948");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect421() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=year(BirthDate) eq 1948&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams422() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=year(BirthDate) eq 1948&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter423() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect424() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=round(Freight) eq 32d&$select=Rating,Description,Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams425() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter426() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect427() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams428() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter429() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect430() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32d&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams431() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=round(Freight) eq 32d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter432() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 32");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect433() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 32&$select=Name,Category");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams434() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 32&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter435() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=ceiling(Freight) eq 33d");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterSelect436() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=ceiling(Freight) eq 33d&$select=Price");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams437() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=ceiling(Freight) eq 33d&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter438() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 33");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat439() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 33&$format=json");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams440() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=floor(Freight) eq 33&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter441() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=isof('NorthwindModel.Order')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterFormat442() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=isof('NorthwindModel.Order')&$format=xml");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams443() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=isof('NorthwindModel.Order')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilter444() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=isof(ShipCountry, 'Edm.String')");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterExpand445() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/",
				"?$filter=isof(ShipCountry, 'Edm.String')&$expand=Categories/Suppliers,Products");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void parseTrailingSlashSingleEntityQueryFilterAndIgnoreParams446() throws ODataException {
		ODataRequestProcessor processor = ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')/", "?$filter=isof(ShipCountry, 'Edm.String')&ignore=bla");
		assertTrue(processor instanceof ODataApikitProcessor);
	}

	@Test
	public void rejectInvalidQueryTop1() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop2() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby3() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby4() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby5() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect6() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip7() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand8() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand9() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount10() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter11() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter12() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter13() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter14() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop15() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop16() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby17() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby18() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby19() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect20() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip21() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand22() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand23() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount24() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter25() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter26() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter27() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter28() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders/", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop29() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop30() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby31() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby32() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby33() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect34() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip35() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand36() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand37() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount38() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter39() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter40() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter41() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter42() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryTop43() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$top=5");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQuerySkip44() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$skip=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryOrderby45() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$orderby=Name");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryInlinecount46() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(1)", "?$inlinecount=allpages");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop47() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop48() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby49() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby50() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby51() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect52() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip53() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand54() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand55() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount56() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter57() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter58() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter59() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter60() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryTop61() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$top=5");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQuerySkip62() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$skip=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryOrderby63() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$orderby=Name");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryInlinecount64() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(420)/", "?$inlinecount=allpages");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop65() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop66() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby67() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby68() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby69() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect70() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip71() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand72() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand73() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount74() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter75() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter76() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter77() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter78() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryTop79() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$top=5");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQuerySkip80() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$skip=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryOrderby81() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$orderby=Name");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryInlinecount82() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders('juan')", "?$inlinecount=allpages");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop83() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop84() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby85() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby86() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby87() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect88() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip89() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand90() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand91() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount92() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter93() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter94() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter95() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter96() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryTop97() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$top=5");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQuerySkip98() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$skip=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryOrderby99() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$orderby=Name");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryInlinecount100() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(userId=3)", "?$inlinecount=allpages");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop101() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$top=nan");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryTop102() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$top='quotes'");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby103() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$orderby=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby104() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$orderby=TrailingSlash/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryOrderby105() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$orderby=InvalidOrder asd/");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySelect106() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$select=Extra,Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQuerySkip107() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$skip=bla");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand108() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$expand=Extra/Comma,");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryExpand109() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$expand=Back\\Slash");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryInlinecount110() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$inlinecount=onepage");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter111() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$filter=le missingLeft");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter112() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$filter=missingRight gt");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter113() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$filter=(invalid");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectInvalidQueryFilter114() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$filter=fakeFunction(5)");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryTop115() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$top=5");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQuerySkip116() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$skip=2");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryOrderby117() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$orderby=Name");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	@Test
	public void rejectUnsupportedQueryInlinecount118() {
		try {
			ODataUriParser.parse(oDataContext, "/odata.svc/orders(name='juan',age=22)", "?$inlinecount=allpages");
			fail("Exception expected");
		} catch (Exception e) {
			// Expected exception, doing nothing.
		}
	}

	//SE-8810 OData APIKit deployment to CloudHub naming restrictions
	//To get the odata url it was stripping the first occurrence of the entity which causes issues when the begining of
	//the app name was equal to the entity name, so instead of it now we are taking the substring before 'odata.svc/'
	@Test
	public void getOdataUrlTest(){
		String completeUrl = "http://companies.us-e2.cloudhub.io/api/odata.svc/companies";
		String odataUrl = "http://companies.us-e2.cloudhub.io/api/odata.svc/";
		ODataUriHelper.getOdataUrl(completeUrl);

		assertEquals(odataUrl,ODataUriHelper.getOdataUrl(completeUrl));

		completeUrl = "http://assets.us-e2.cloudhub.io/api/odata.svc/assets";
		odataUrl = "http://assets.us-e2.cloudhub.io/api/odata.svc/";

		assertEquals(odataUrl,ODataUriHelper.getOdataUrl(completeUrl));
	}
}
