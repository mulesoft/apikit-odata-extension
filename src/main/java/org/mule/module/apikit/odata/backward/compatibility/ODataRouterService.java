/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.backward.compatibility;

import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.odata.AbstractODataRouterService;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.spi.AbstractRouter;
import org.mule.module.apikit.spi.RouterService;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router implementation only for backward compatibility purpose.
 *
 * @deprecated
 * @see AbstractODataRouterService
 */
@Deprecated
public class ODataRouterService extends AbstractODataRouterService implements RouterService {

  private static Logger logger = LoggerFactory.getLogger(ODataRouterService.class);

  private OdataContext oDataContext;

  @Override
  protected OdataContext geODataContext() {
    return this.oDataContext;
  }

  @Override
  public Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router, String ramlPath)
      throws MuleException {
    initializeModel(router.getRaml().getUri());
    return process(event, router::processEvent);
  }

  private void initializeModel(String ramlPath) {
    OdataMetadataManager odataMetadataManager;
    try {
      odataMetadataManager = new OdataMetadataManagerImpl(ramlPath);
    } catch (OdataMetadataFormatException e) {
      logger.error(e.getMessage(), e);
      throw new ApikitRuntimeException(e);
    }
    this.oDataContext = new OdataContext(odataMetadataManager);
  }
}
