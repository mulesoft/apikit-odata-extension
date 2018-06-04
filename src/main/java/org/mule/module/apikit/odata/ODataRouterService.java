/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.mule.runtime.api.exception.MuleException;

import org.apache.log4j.Logger;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.spi.EventProcessor;
import org.mule.module.apikit.spi.RouterService;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
import org.odata4j.exceptions.BadRequestException;

public class ODataRouterService implements RouterService {

	private static final String ODATA_SVC_URI_PREFIX = "odata.svc";
	private static final String CONTEXT_INITIALIZED = "contextInitialized";

	private Logger logger = Logger.getLogger(ODataRouterService.class);

	static { 
		System.setProperty("javax.ws.rs.ext.RuntimeDelegate","org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl"); // Workaround for issue while loading class javax.ws.rs.ext.RuntimeDelegate embedded in odata4j
	}																									  	  // https://stackoverflow.com/questions/30316829/classnotfoundexception-org-glassfish-jersey-internal-runtimedelegateimpl-cannot
	
	public boolean isExecutable(CoreEvent event) {
	    HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
	    return attributes.getHeaders().get(CONTEXT_INITIALIZED) == null;
	}


	@Override
	public CompletableFuture<Event> process(CoreEvent event, EventProcessor router,String ramlPath) throws MuleException {
		logger.info("Handling odata enabled request.");
		
		HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
		OdataContext oDataContext = null;
		if (isExecutable(event)) {
			try {
				oDataContext = initializeModel(ramlPath,attributes.getMethod());
			} catch (OdataMetadataFieldsException | OdataMetadataResourceNotFound | OdataMetadataFormatException
					| OdataMetadataEntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		String path = attributes.getRelativePath();
			
		if (path.contains(ODATA_SVC_URI_PREFIX)) {
			return ODataRouterService.processODataRequest(attributes, router, oDataContext,event);
		} else {
			return router.processEvent(event);
		}
	}
	
//	public MuleEvent processBlockingRequest(MuleEvent event, MessageProcessor abstractRouter) throws MuleException {
//		logger.info("Handling odata enabled request.");
//
//		Router router = (Router) abstractRouter;
//		OdataContext oDataContext = null;
//
//		String path = event.getMessage().getInboundProperty("http.relative.path");
//		
//		if (event.getMessage().getOutboundProperty(CONTEXT_INITIALIZED) == null) {
//			try {
//				oDataContext = initializeModel(event, router);
//			} catch (OdataMetadataEntityNotFoundException ome) {
//				if (path.contains(ODATA_SVC_URI_PREFIX)) {
//					Logger.getLogger(ODataRouterService.class).error(ome);
//					return ODataErrorHandler.handle(event, ome);
//				} else {
//					Logger.getLogger(ODataRouterService.class).error(ome);
//				}
//			} catch (ODataException oe) {
//				Logger.getLogger(ODataRouterService.class).error(oe);
//				return ODataErrorHandler.handle(event, oe);
//			}getMetadataManager()
//
//			event.getMessage().setProperty(CONTEXT_INITIALIZED, true, PropertyScope.OUTBOUND);
//		}
//	
//		if (path.contains(ODATA_SVC_URI_PREFIX)) {
//			return ODataRouterService.processODataRequest(event, router, oDataContext);
//		} else {
//			event.getMessage().removeProperty(CONTEXT_INITIALIZED, PropertyScope.OUTBOUND);
//			return router.processBlockingRequest(event);
//		}
//	}

	protected static OdataContext initializeModel(String ramlPath, String method) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
		Logger.getLogger(ODataRouterService.class).info("Init model.");
		OdataContextInitializer contextInitializer = new OdataContextInitializer();
		return contextInitializer.initializeContext(ramlPath, method);
	}

	
	protected static CompletableFuture<Event> processODataRequest(HttpRequestAttributes attributes,EventProcessor eventProcessor ,OdataContext oDataContext, CoreEvent event) throws MuleException {
		List<Format> formats = null;
		CompletableFuture<Event> completableFuture = new CompletableFuture<Event>();
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
			throw  new BadRequestException(ex);
		}

		return completableFuture;
	}
	
//	protected static MuleEvent processODataRequest(MuleEvent event, Router router, OdataContext oDataContext) throws MuleException {
//		List<Format> formats = null;
//
//		try {
//
//			Configuration config = router.getConfig();
//
//			HttpRestRequest request = new HttpRestRequest(event, config);
//			String path = request.getResourcePath();
//			String query = event.getMessage().getInboundProperty("http.query.string");
//
//			// URIParser
//			ODataRequestProcessor odataRequestProcessor = ODataUriParser.parse(oDataContext, path, query);
//
//			// Validate format
//			formats = ODataFormatHandler.getFormats(event);
//
//			// Request processor
//			ODataPayload odataPayload = odataRequestProcessor.process(event, router, formats);
//
//			// Response transformer
//			return ODataResponseTransformer.transform(event, odataPayload, formats);
//
//		} catch (Exception ex) {
//			return ODataErrorHandler.handle(event, ex, formats);
//		}
//	}




}
