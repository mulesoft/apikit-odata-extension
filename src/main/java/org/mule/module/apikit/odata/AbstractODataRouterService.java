/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import static org.mule.module.apikit.odata.util.CoreEventUtils.getHttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.error.ODataErrorHandler;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import java.util.List;

public abstract class AbstractODataRouterService {

  protected static final String ODATA_SVC_URI_PREFIX = "odata.svc";

  static {
    System.setProperty("javax.ws.rs.ext.RuntimeDelegate",
        "org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl");
    // Workaround for issue while loading class javax.ws.rs.ext.RuntimeDelegate embedded in odata4j
    // https://stackoverflow.com/questions/30316829/classnotfoundexception-org-glassfish-jersey-internal-runtimedelegateimpl-cannot
  }

  protected abstract OdataContext geODataContext();

  public Publisher<CoreEvent> process(CoreEvent event, AbstractRouterInterface router)
      throws MuleException {
    OdataContext oDataContext = geODataContext();
    HttpRequestAttributes attributes = getHttpRequestAttributes(event);
    oDataContext.setMethod(attributes.getMethod());
    String path = attributes.getRelativePath();

    if (path.contains(ODATA_SVC_URI_PREFIX)) {
      return processODataRequest(attributes, router, oDataContext, event);
    } else {
      return router.processEvent(event);
    }
  }

  protected static Publisher<CoreEvent> processODataRequest(HttpRequestAttributes attributes,
      AbstractRouterInterface router, OdataContext oDataContext, CoreEvent event) {
    List<ODataPayloadFormatter.Format> formats;
    ODataRequestProcessor odataRequestProcessor;
    try {
      String listenerPath =
          attributes.getListenerPath().substring(0, attributes.getListenerPath().lastIndexOf("/*"));
      String path = attributes.getRawRequestPath().replaceAll(listenerPath, "");
      String query = attributes.getQueryString();

      // URIParser
      odataRequestProcessor =
          ODataUriParser.parse(oDataContext, path, query, attributes.getQueryParams());

      // Validate format
      formats = ODataFormatHandler.getFormats(attributes);
    } catch (Exception ex) {
      return Mono.just(ODataErrorHandler.handle(event, ex));
    }

    // Request processor
    return odataRequestProcessor.process(event, router, formats).flatMap(odataPayload -> {
      try {
        // Response transformer
        Message message = ODataResponseTransformer.transform(odataPayload, formats);

        return Mono.just(CoreEvent.builder(odataPayload.getMuleEvent()).message(message)
            .addVariable("httpStatus", odataPayload.getStatus()).build());
      } catch (Exception ex) {
        return Mono.error(ex);
      }
    }).onErrorResume(ex -> Mono.just(ODataErrorHandler.handle(event, ex, formats)));
  }

}
