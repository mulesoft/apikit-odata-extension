/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.error.ODataErrorHandler;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.odata.util.CoreEventUtils;
import org.mule.module.apikit.spi.EventProcessor;
import org.mule.module.apikit.spi.RouterService;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;

public class ODataRouterService implements RouterService {

	private static final String ODATA_SVC_URI_PREFIX = "odata.svc";

	private  Logger logger = Logger.getLogger(ODataRouterService.class);

	static { 
		System.setProperty("javax.ws.rs.ext.RuntimeDelegate","org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl"); // Workaround for issue while loading class javax.ws.rs.ext.RuntimeDelegate embedded in odata4j
	}																									  	  // https://stackoverflow.com/questions/30316829/classnotfoundexception-org-glassfish-jersey-internal-runtimedelegateimpl-cannot
	
	@Override
	public CompletableFuture<Event> process(CoreEvent event, EventProcessor router) throws MuleException {
		logger.info("Handling odata enabled request.");
		
		HttpRequestAttributes attributes = CoreEventUtils.getHttpRequestAttributes(event);
		OdataContext oDataContext = getOdataContext(router.getRamlHandler().getRamlV1());
		oDataContext.setMethod(attributes.getMethod());
		String path = attributes.getRelativePath();
			
		if (path.contains(ODATA_SVC_URI_PREFIX)) {
			return ODataRouterService.processODataRequest(attributes, router, oDataContext,event);
		} else {
			return router.processEvent(event);
		}
	}
	
	private OdataContext getOdataContext(String ramlPath) throws ApikitRuntimeException {
		OdataContext oDataContext = null;

		try {
			oDataContext = initializeModel(ramlPath);
		} catch (OdataMetadataFieldsException | OdataMetadataResourceNotFound | OdataMetadataFormatException
				| OdataMetadataEntityNotFoundException e) {
			logger.error(e.getMessage(),e);
			throw new ApikitRuntimeException(e);
		}		
		return oDataContext;
	}
	
	protected static OdataContext initializeModel(String ramlPath) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
		Logger.getLogger(ODataRouterService.class).info("Init model.");
		OdataContextInitializer contextInitializer = new OdataContextInitializer();
		return contextInitializer.initializeContext(ramlPath);
	}

	
	protected static CompletableFuture<Event> processODataRequest(HttpRequestAttributes attributes,EventProcessor eventProcessor ,OdataContext oDataContext, CoreEvent event) throws MuleException {
		CompletableFuture<Event> completableFuture = new CompletableFuture<Event>();
		
		Executors.newCachedThreadPool().submit(()->{ 
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
				ODataPayload odataPayload = odataRequestProcessor.process(event, eventProcessor, formats);
	
				// Response transformer
				Message message = ODataResponseTransformer.transform( odataPayload, formats);			
				
				CoreEvent newEvent  = CoreEvent.builder(event).message(message).build();
				completableFuture.complete(newEvent);
			} catch (Exception ex) {
				completableFuture.complete( ODataErrorHandler.handle(event, ex, formats));
			}
		});

		return completableFuture;
	}
	
}
