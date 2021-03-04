/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.api.spi.AbstractRouter;
import org.mule.module.apikit.api.spi.RouterService;
import org.mule.module.apikit.api.spi.RouterServiceV2;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.metadata.OdataMetadataManagerImpl;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;

public class ODataRouterService extends AbstractODataRouterService
    implements RouterService, RouterServiceV2 {

  private OdataContext oDataContext;

  @Override
  public RouterServiceV2 initialise(String filePath, Scheduler scheduler) {
    this.oDataContext = initialiseContext(filePath, scheduler);
    return this;
  }

  @Override
  public RouterService initialise(String filePath) {
    this.oDataContext = initialiseContext(filePath, null);
    return this;
  }

  private OdataContext initialiseContext(String filePath, Scheduler scheduler) {
    try {
      return new OdataContext(new OdataMetadataManagerImpl(filePath, scheduler));
    } catch (OdataMetadataFormatException e) {
      throw new ApikitRuntimeException(e);
    }
  }

  @Override
  public Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router) throws MuleException {
    return process(event, router::processEvent);
  }

  @Override
  protected OdataContext geODataContext() {
    return this.oDataContext;
  }
}
