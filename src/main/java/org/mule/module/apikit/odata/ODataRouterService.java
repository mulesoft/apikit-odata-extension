/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.util.List;

import org.apache.log4j.Logger;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.transport.PropertyScope;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.HttpRestRequest;
import org.mule.module.apikit.Router;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.error.ODataErrorHandler;
import org.mule.module.apikit.odata.exception.ODataException;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.spi.RouterService;
import org.mule.processor.AbstractRequestResponseMessageProcessor;

public class ODataRouterService implements RouterService {

	private static final String ODATA_SVC_URI_PREFIX = "odata.svc";
	private static final String CONTEXT_INITIALIZED = "contextInitialized";

	public boolean isExecutable(MuleEvent event) {
		return (event.getMessage().getOutboundProperty(CONTEXT_INITIALIZED) == null);
	}

	public MuleEvent processBlockingRequest(MuleEvent event, AbstractRequestResponseMessageProcessor abstractRouter) throws MuleException {
		Logger.getLogger(ODataRouterService.class).info("Handling odata enabled request.");

		Router router = (Router) abstractRouter;
		OdataContext oDataContext = null;

		if (event.getMessage().getOutboundProperty(CONTEXT_INITIALIZED) == null) {

			try {
				oDataContext = initializeModel(event, router);
			} catch (ODataException e) {
				Logger.getLogger(ODataRouterService.class).error(e);
				return ODataErrorHandler.handle(event, e);
			}

			event.getMessage().setProperty(CONTEXT_INITIALIZED, true, PropertyScope.OUTBOUND);
		}

		String path = event.getMessage().getInboundProperty("http.relative.path");
	
		if (path.contains(ODATA_SVC_URI_PREFIX)) {
			return ODataRouterService.processODataRequest(event, router, oDataContext);
		} else {
			event.getMessage().removeProperty(CONTEXT_INITIALIZED, PropertyScope.OUTBOUND);
			return router.processBlockingRequest(event);
		}
	}

	protected static OdataContext initializeModel(MuleEvent event, Router router) throws OdataMetadataFieldsException, OdataMetadataResourceNotFound,
			OdataMetadataFormatException, OdataMetadataEntityNotFoundException {
		Logger.getLogger(ODataRouterService.class).info("Init model.");
		OdataContextInitializer contextInitializer = new OdataContextInitializer();
		return contextInitializer.initializeContext(event, router.getConfig());
	}

	protected static MuleEvent processODataRequest(MuleEvent event, Router router, OdataContext oDataContext) throws MuleException {
		List<Format> formats = null;

		try {

			Configuration config = router.getConfig();

			HttpRestRequest request = new HttpRestRequest(event, config);
			String path = request.getResourcePath();
			String query = event.getMessage().getInboundProperty("http.query.string");

			// Metadata manager setup
			OdataMetadataManager odataMetadataManager = oDataContext.getOdataMetadataManager();

			// URIParser
			ODataRequestProcessor odataRequestProcessor = ODataUriParser.parse(oDataContext, path, query);

			// Validate format
			formats = ODataFormatHandler.getFormats(event);

			// Request processor
			ODataPayload odataPayload = odataRequestProcessor.process(event, router, formats);

			// Response transformer
			return ODataResponseTransformer.transform(event, odataPayload, formats);

		} catch (Exception ex) {
			return ODataErrorHandler.handle(event, ex, formats);
		}
	}

}
