/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package org.mule.module.apikit.odata.formatter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;

import static org.junit.Assert.assertTrue;

public class FormatterTestCase {

	private static OdataContext oDataContext;

    @BeforeClass
    public static void setUp() throws ODataException {
        OdataMetadataManager odataMetadataManager = new OdataMetadataManager();
        String ramlPath = "src/test/resources/org/mule/module/apikit/odata/api-mk.raml";
        odataMetadataManager.refreshMetadata(ramlPath, true);
        oDataContext = new OdataContext(odataMetadataManager, "GET");
    }

    @Test
    public void serviceDocumentPayloadFormatter() throws Exception {
        OdataMetadataManager odataMetadataManager = oDataContext.getOdataMetadataManager();

        ServiceDocumentPayloadFormatter formatter = new ServiceDocumentPayloadFormatter(odataMetadataManager, "http://localhost:8081/api/odata.svc");

        String format = formatter.format(ODataPayloadFormatter.Format.Atom);
        assertTrue(format.contains("<collection href=\"customers\">"));
        assertTrue(format.contains("<atom:title>customers</atom:title>"));
        // Entity city pluralized to cities
        assertTrue(format.contains("<collection href=\"cities\">"));
        assertTrue(format.contains("<atom:title>cities</atom:title>"));
    }
}
