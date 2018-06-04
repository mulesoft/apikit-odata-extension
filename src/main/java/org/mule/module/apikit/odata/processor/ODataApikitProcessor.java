/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import static reactor.core.publisher.Mono.fromFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpRequestAttributesBuilder;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.ClientErrorException;
import org.mule.module.apikit.odata.exception.ODataInvalidFlowResponseException;
import org.mule.module.apikit.odata.exception.ODataUnsupportedMediaTypeException;
import org.mule.module.apikit.odata.formatter.ODataApiKitFormatter;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataEntityNotFoundException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFieldsException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataFormatException;
import org.mule.module.apikit.odata.metadata.exception.OdataMetadataResourceNotFound;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinition;
import org.mule.module.apikit.odata.metadata.model.entities.EntityDefinitionProperty;
import org.mule.module.apikit.odata.model.Entry;
import org.mule.module.apikit.odata.util.CoreEventUtils;
import org.mule.module.apikit.odata.util.Helper;
import org.mule.module.apikit.spi.EventProcessor;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.event.CoreEvent;

public class ODataApikitProcessor extends ODataRequestProcessor {
	private String path;
	private String query;
	private String entity;
	private boolean entityCount;
	private static final String[] methodsWithBody = { "POST", "PUT" };

	// TODO this attr is not used in project
	private Map<String, Object> keys;

	public ODataApikitProcessor(OdataContext odataContext, String entity, String query, Map<String, Object> keys, boolean count) {
		super(odataContext);
		this.query = query;
		this.entity = entity;
		this.entityCount = count;
		this.keys = keys;
		this.path = generatePath(entity, keys);
	}

	private String generatePath(String entity, Map<String, Object> keys) {
		String path = "";
		if (keys.size() == 0) {
			path = "/" + entity;
		} else if (keys.size() == 1) {
			String key = (String) keys.keySet().toArray()[0];
			String id = (String) keys.get(key);
			path = "/" + entity + "/" + id;
		} else if (keys.size() > 1) {
			path = "/" + entity + "/";

			String uriKeys = "";

			String delimiter = "";
			for (Map.Entry<String, Object> entry : keys.entrySet()) {
				uriKeys += delimiter + entry.getKey() + "_" + entry.getValue();
				delimiter = " :: ";
			}
			List<String> parsedKeys = Arrays.asList(uriKeys.split(" :: "));
			Collections.sort(parsedKeys);

			path += StringUtils.join(parsedKeys, "-");
		}
		return path;
	}

	@Override
	public ODataPayload process(CoreEvent event, EventProcessor eventProcessor, List<Format> formats) throws Exception {
		String oDataURL = getCompleteUrl(CoreEventUtils.getHttpRequestAttributes(event));

		// truncate the URL at the entity
		oDataURL = oDataURL.substring(0, oDataURL.indexOf(entity));

		List<Entry> entries = processEntityRequest(event, eventProcessor, formats);
		ODataPayload oDataPayload;

		if (isEntityCount()) {
			if (formats.contains(Format.Plain) || formats.contains(Format.Default)) {
				String count = String.valueOf(entries.size());
				oDataPayload = new ODataPayload(count);
			} else {
				throw new ODataUnsupportedMediaTypeException("Unsupported media type requested.");
			}
		} else {
			oDataPayload = new ODataPayload(entries);
			oDataPayload.setFormatter(new ODataApiKitFormatter(getMetadataManager(), entries, entity, oDataURL));
		}

		return oDataPayload;
	}

	public List<Entry> processEntityRequest(CoreEvent event, EventProcessor eventProcessor, List<Format> formats) throws Exception {
		List<Entry> entries = new ArrayList<Entry>();
		HttpRequestAttributes attributes =CoreEventUtils.getHttpRequestAttributes(event);
		String uri = attributes.getRelativePath();;
		String basePath = uri.substring(0, uri.toLowerCase().indexOf("/odata.svc"));
		String httpRequest = basePath + this.path + "?" + this.query;
		String httpRequestPath = basePath + this.path;

		
		HttpRequestAttributesBuilder httpRequestAttributesBuilder =  new HttpRequestAttributesBuilder(attributes);
		httpRequestAttributesBuilder.requestPath(httpRequestPath);
		httpRequestAttributesBuilder.requestUri(httpRequest);
		httpRequestAttributesBuilder.queryString(this.query);
		httpRequestAttributesBuilder.relativePath(this.path);
		
		MultiMap<String, String> httpQueryParams = Helper.queryToMap(query);
		httpRequestAttributesBuilder.queryParams(httpQueryParams);

		Message message = null;
		if (Arrays.asList(methodsWithBody).contains(attributes.getMethod().toUpperCase())) {
			String payloadAsString = CoreEventUtils.getPayloadAsString(event);
			if(payloadAsString == null){
				payloadAsString = "";
			}
			boolean isXMLFormat = !formats.contains(Format.Json);
			payloadAsString = BodyToJsonConverter.convertPayload(entity, isXMLFormat, payloadAsString);
			message = Message.builder().value(payloadAsString).mediaType(MediaType.APPLICATION_JSON).attributesValue(httpRequestAttributesBuilder.build()).build();
		} else {
			message = Message.builder(event.getMessage()).attributesValue(httpRequestAttributesBuilder.build()).build();
		}
		
		
//		String httpQueryString = this.query;
//		event.getMessage().setProperty("http.request", httpRequest, PropertyScope.INBOUND);
//		event.getMessage().setProperty("http.request.path", httpRequestPath, PropertyScope.INBOUND);
//		event.getMessage().setProperty("http.query.string", httpQueryString, PropertyScope.INBOUND);
//		event.getMessage().setProperty("http.query.params", httpQueryParams, PropertyScope.INBOUND);
//		event.getMessage().setProperty("http.relative.path", this.path, PropertyScope.INBOUND);
//		event.getMessage().setProperty("accept", "application/json", PropertyScope.INBOUND);

//		String httpMethod = event.getMessage().getInboundProperty("http.method").toString().toLowerCase();
//
//		if (Arrays.asList(methodsWithBody).contains(httpMethod.toUpperCase())) {
//			String payloadAsString = event.getMessage().getPayloadAsString();
//			if(event.getMessage().getPayload() instanceof NullPayload){
//				payloadAsString = "";
//			}
//			boolean isXMLFormat = !formats.contains(Format.Json);
//			event.getMessage().setPayload(BodyToJsonConverter.convertPayload(entity, isXMLFormat, payloadAsString));
//			// Setting again encoding and mimetype. For some reason encoding is set to null and mimetype to */* after setPayload
//			event.getMessage().getDataType().setEncoding(event.getEncoding());
//			event.getMessage().getDataType().setMimeType("application/json");
//			event.getMessage().setProperty("content-type", "application/json", PropertyScope.INBOUND);
//		}

		CompletableFuture<Event> response = eventProcessor.processEvent(CoreEvent.builder(event).message(message).build());

		entries = verifyFlowResponse(response);

		return entries;
	}

	private List<Entry> verifyFlowResponse(CompletableFuture<Event> response) throws OdataMetadataEntityNotFoundException, OdataMetadataFieldsException,
			OdataMetadataResourceNotFound, OdataMetadataFormatException, ODataInvalidFlowResponseException, ClientErrorException {
		

		try {
			CoreEvent event;
//			event = fromFuture(response).cast(CoreEvent.class);
			event = (CoreEvent) response.get();

//			checkResponseHttpStatus(event); TODO:Check how to validate the response
			
			OdataMetadataManager metadataManager = getMetadataManager();
			EntityDefinition entityDefinition = metadataManager.getEntityByName(entity);
			List<Entry> entries;

//			String message = response.getMessageAsString();
//			Object payload = response.getMessage().getPayload();
//			if (payload instanceof NullPayload) {
//				entries = new ArrayList<Entry>();
//			} else if (message != null ) {
//				entries = Helper.transformJsonToEntryList(message);
			String payload = CoreEventUtils.getPayloadAsString(event);
			if(payload != null || payload != "") {
				entries = Helper.transformJsonToEntryList(payload);
			} else {
				throw new ODataInvalidFlowResponseException("The payload of the response should be a valid json.");
			}

			int entryNumber = 1;
			for (Entry entry : entries) {
				// verifies the # of properties matches the definition
				if (entry.getProperties().entrySet().size() > entityDefinition.getProperties().size())
					throw new ODataInvalidFlowResponseException("There are absent properties in flow response (entry #" + (entryNumber++) + ")");

				// verifies each property against the definition
				for (String propertyName : entry.getProperties().keySet()) {
					EntityDefinitionProperty entityDefinitionProperty = entityDefinition.findPropertyDefinition(propertyName);
					if (entityDefinitionProperty == null)
						throw new ODataInvalidFlowResponseException("Property '" + propertyName + "' was not expected for entity '" + entityDefinition.getName() + "'");
				}
			}

			return entries;
		} catch (InterruptedException | ExecutionException e) {
			throw new ODataInvalidFlowResponseException(e.getMessage());
		}
	}

	protected static void checkResponseHttpStatus(CoreEvent response) throws ClientErrorException {
		
		HttpResponseAttributes attributes = CoreEventUtils.getHttpResponseAttributes(response);
		
		int httpStatus = attributes.getStatusCode();



		if(httpStatus >= 400){
			// If there was an error, it makes no sense to return a list of entry
			// just raise an exception
			String payload = CoreEventUtils.getPayloadAsString(response);
			throw new ClientErrorException(payload != null ? payload.toString() : "", httpStatus);
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isEntityCount() {
		return entityCount;
	}

	public Map<String, Object> getKeys() {
		return keys;
	}

	public void setKeys(Map<String, Object> keys) {
		this.keys = keys;
	}
}
