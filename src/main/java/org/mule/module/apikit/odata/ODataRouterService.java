/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.apache.log4j.Logger;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.error.ODataErrorHandler;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.odata.util.CoreEventUtils;
import org.mule.module.apikit.spi.AbstractRouter;
import org.mule.module.apikit.spi.RouterService;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ODataRouterService implements RouterService {

	private static final String ODATA_SVC_URI_PREFIX = "odata.svc";

	private static  Logger logger = Logger.getLogger(ODataRouterService.class);

	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	static { 
		System.setProperty("javax.ws.rs.ext.RuntimeDelegate","org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl"); // Workaround for issue while loading class javax.ws.rs.ext.RuntimeDelegate embedded in odata4j
	}																									  	  // https://stackoverflow.com/questions/30316829/classnotfoundexception-org-glassfish-jersey-internal-runtimedelegateimpl-cannot
	
	@Override
	public Publisher<CoreEvent> process(CoreEvent event, AbstractRouter router, String raml) throws MuleException {
		logger.debug("Handling odata enabled request.");

		String ramlPath = router.getRaml().getUri();
		HttpRequestAttributes attributes = CoreEventUtils.getHttpRequestAttributes(event);
        OdataContext oDataContext = getOdataContext(ramlPath);
		oDataContext.setMethod(attributes.getMethod());
		String path = attributes.getRelativePath();
			
		if (path.contains(ODATA_SVC_URI_PREFIX)) {
			return ODataRouterService.processODataRequest(attributes, router, oDataContext,event);
		} else {
			return router.processEvent(event);
		}
	}
	
	private OdataContext getOdataContext(String ramlPath) throws ApikitRuntimeException {
		OdataContext oDataContext;

		try {
			oDataContext = initializeModel(ramlPath);
		} catch (OdataMetadataFormatException e) {
			logger.error(e.getMessage(),e);
			throw new ApikitRuntimeException(e);
		}		
		return oDataContext;
	}
	
	private static OdataContext initializeModel(String ramlPath) throws OdataMetadataFormatException {
		final OdataMetadataManager odataMetadataManager = new OdataMetadataManager(ramlPath);
		return new OdataContext(odataMetadataManager);
	}

	
	private static Publisher<CoreEvent> processODataRequest(HttpRequestAttributes attributes, AbstractRouter router ,OdataContext oDataContext, CoreEvent event) throws MuleException {
		List<Format> formats = null;
		try {
			String listenerPath = attributes.getListenerPath().substring( 0,attributes.getListenerPath().lastIndexOf("/*"));
			String path = attributes.getRelativePath().replaceAll(listenerPath, "");
			String query = attributes.getQueryString();

			// URIParser
			ODataRequestProcessor odataRequestProcessor = ODataUriParser.parse(oDataContext, path, query);

			// Validate format
			formats = ODataFormatHandler.getFormats(attributes);

			// Request processor
			ODataPayload odataPayload = odataRequestProcessor.process(event, router, formats);

			// Response transformer
			Message message = ODataResponseTransformer.transform( odataPayload, formats);

			CoreEvent newEvent  = CoreEvent.builder(event).message(message).addVariable("httpStatus", odataPayload.getStatus()).build();
			return Mono.just(newEvent);
		} catch (Exception ex) {
			return Mono.just(ODataErrorHandler.handle(event, ex, formats));
		}
	}
	
}
