/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import org.mule.module.apikit.odata.AbstractRouterInterface;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ODataMethodNotAllowedException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.formatter.ODataPayloadMetadataFormatter;
import org.mule.runtime.core.api.event.CoreEvent;
import reactor.core.publisher.Mono;
import java.util.List;

public class ODataMetadataProcessor extends ODataRequestProcessor {
  public ODataMetadataProcessor(OdataContext odataContext) {
    super(odataContext);
  }

  public Mono<ODataPayload<?>> process(CoreEvent event, AbstractRouterInterface router,
      List<Format> formats) {
    if ("GET".equalsIgnoreCase(super.oDataContext.getMethod())) {
      ODataPayload<?> oDataPayload = new ODataPayload<>(event);
      oDataPayload.setFormatter(new ODataPayloadMetadataFormatter(getMetadataManager()));
      return Mono.just(oDataPayload);
    } else {
      return Mono.error(new ODataMethodNotAllowedException("GET"));
    }

  }
}
