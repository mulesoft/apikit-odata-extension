/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import java.net.URLDecoder;
import java.util.HashMap;

import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.exception.*;
import org.mule.module.apikit.odata.processor.ODataApikitProcessor;
import org.mule.module.apikit.odata.processor.ODataMetadataProcessor;
import org.mule.module.apikit.odata.processor.ODataRequestProcessor;
import org.mule.module.apikit.odata.processor.ODataServiceDocumentProcessor;
import org.mule.module.apikit.odata.util.ODataUriHelper;

public class ODataUriParser {

	private static final String ODATA_SVC_URI_PREFIX = "/odata.svc";

	/**
	 * Parses the URI and returns the right processor to handle the request
	 * @return
	 * @throws ODataInvalidUriException
	 * @throws ODataInvalidFormatException
	 */

	public static ODataRequestProcessor parse(OdataContext odataContext, String path, String query) throws ODataException {

		path = path.replace(ODATA_SVC_URI_PREFIX, "");
		query = decodeQuery(query);

		if (ODataUriHelper.allowedQuery(path, query)) {
			// metadata
			if (ODataUriHelper.isMetadata(path)) {
				return new ODataMetadataProcessor(odataContext);
			}

			// service document
			if (ODataUriHelper.isServiceDocument(path)) {
				return new ODataServiceDocumentProcessor(odataContext);
			}

			// resource request
			if (ODataUriHelper.isResourceRequest(path)) {

				// parse entity
				String entity = ODataUriHelper.parseEntity(path);

				// parse query
				String querystring = handleQuerystring(query);

				// parse keys
				HashMap<String, Object> keys = ODataUriHelper.parseKeys(path, odataContext.getOdataMetadataManager().getEntityKeys(entity));

				// check if $count is present
				boolean count = ODataUriHelper.isCount(path);

				if (count) {
					if (ODataUriHelper.hasFormat(query)) // requests using $count can't
																							 // have queries with $format
					{
						throw new ODataUnsupportedMediaTypeException("Unsupported media type requested.");
					}
					if (!ODataUriHelper.isCollection(path)) // $count can only be used for
																									// collections
					{
						throw new ODataInvalidUriException("The request URI is not valid, since the segment '" + entity
								+ "' refers to a singleton, and the segment '$count' can only follow a resource collection.>");
					}
				}

				return new ODataApikitProcessor(odataContext, entity, querystring, keys, count);

			} else {
				String segment = ODataUriHelper.parseEntity(path);
				throw new ODataNotFoundException("Resource not found for the segment '" + segment + "'.");
			}
		} else {
			throw new ODataInvalidUriException("Unsupported query.");
		}
	}

	private static String decodeQuery(String query) throws ODataInvalidUriException {
		try {
			return URLDecoder.decode(query, "UTF-8");
		} catch (Exception e) {
			throw new ODataInvalidUriException(e.getMessage());
		}
	}

	private static String handleQuerystring(String query) throws ODataInvalidUriException, ODataInvalidFormatException {
		String querystring = "";
		if (ODataUriHelper.validQuery(query)) {
			querystring = query.replace("?", "").replace("$", "");
		}
		return querystring;
	}
}