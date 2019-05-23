/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.apache.log4j.Logger;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.api.spi.AbstractRouter;
import org.mule.module.apikit.api.spi.RouterService;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.OdataMetadataManagerImpl;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;

public class ODataRouterService extends AbstractODataRouterService implements RouterService {

  private static Logger logger = Logger.getLogger(ODataRouterService.class);

  private OdataContext oDataContext;

  @Override
  public RouterService initialise(String raml) {
    try {
      this.oDataContext = initializeModel(raml);
      return this;
    } catch (OdataMetadataFormatException e) {
      logger.error(e.getMessage(), e);
      throw new ApikitRuntimeException(e);
    }
  }

  @Override
  public Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router) throws MuleException {
    logger.debug("Handling odata enabled request.");
    return process(event,router::processEvent);
  }

  private static OdataContext initializeModel(String ramlPath) throws OdataMetadataFormatException {
    final OdataMetadataManager odataMetadataManager = new OdataMetadataManagerImpl(ramlPath);
    return new OdataContext(odataMetadataManager);
  }

  @Override
  protected OdataContext geODataContext() {
    return this.oDataContext;
  }
}
