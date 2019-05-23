/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */

package org.mule.module.apikit.odata.formatter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.OdataMetadataManagerImpl;
import org.mule.module.apikit.odata.util.FileUtils;
import static org.junit.Assert.assertTrue;

public class FormatterTestCase {

  private static OdataContext oDataContext;

  @BeforeClass
  public static void setUp() throws ODataException {
    final OdataMetadataManager odataMetadataManager = new OdataMetadataManagerImpl(
        FileUtils.getAbsolutePath("org/mule/module/apikit/odata/model-mk.raml"));
    oDataContext = new OdataContext(odataMetadataManager, "GET");
  }

  @Test
  public void serviceDocumentPayloadFormatter() throws Exception {
    OdataMetadataManager odataMetadataManager = oDataContext.getOdataMetadataManager();

    ServiceDocumentPayloadFormatter formatter = new ServiceDocumentPayloadFormatter(
        odataMetadataManager, "http://localhost:8081/api/odata.svc");

    String format = formatter.format(ODataPayloadFormatter.Format.Atom);
    assertTrue(format.contains("<collection href=\"customers\">"));
    assertTrue(format.contains("<atom:title>customers</atom:title>"));
    // Entity city pluralized to cities
    assertTrue(format.contains("<collection href=\"cities\">"));
    assertTrue(format.contains("<atom:title>cities</atom:title>"));
  }

  @Test
  public void payloadMetadataFormatter() throws Exception {
    OdataMetadataManager odataMetadataManager = oDataContext.getOdataMetadataManager();

    ODataPayloadMetadataFormatter formatter =
        new ODataPayloadMetadataFormatter(odataMetadataManager);

    String format = formatter.format(ODataPayloadFormatter.Format.Atom);
    assertTrue(format.contains("<EntityType Name=\"customers\">"));
    assertTrue(format.contains("<EntityType Name=\"city\">"));
  }
}
