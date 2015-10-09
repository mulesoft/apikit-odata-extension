/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.apache.cxf.jaxrs.model.MethodInvocationInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfoStack;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.jaxrs.utils.HttpUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;

public class UriInfoImpl implements UriInfo {
	private static final Logger LOG = LogUtils.getL7dLogger(UriInfoImpl.class);

	private MultivaluedMap<String, String> templateParams;
	private OperationResourceInfoStack stack;
	private boolean caseInsensitiveQueries;
	private String url;

	public UriInfoImpl(String url) {
		this.url = url;
		// this( (MultivaluedMap<String, String>) m
		// .get(URITemplate.TEMPLATE_PARAMETERS));
	}

	public UriInfoImpl(MultivaluedMap<String, String> templateParams) {
		this.templateParams = templateParams;
	}

	public URI getAbsolutePath() {
		String path = getAbsolutePathAsString();
		return URI.create(path);
	}

	public UriBuilder getAbsolutePathBuilder() {
		return new UriBuilderImpl(getAbsolutePath());
	}

	public URI getBaseUri() {
		try {
			return new URL(url).toURI();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public UriBuilder getBaseUriBuilder() {
		return new UriBuilderImpl(getBaseUri());
	}

	public String getPath() {
		return getPath(true);
	}

	public String getPath(boolean decode) {
		String value = doGetPath(decode, true);
		if (value.length() > 1 && value.startsWith("/")) {
			return value.substring(1);
		} else {
			return value;
		}
	}

	public List<PathSegment> getPathSegments() {
		return getPathSegments(true);
	}

	public List<PathSegment> getPathSegments(boolean decode) {
		return JAXRSUtils.getPathSegments(getPath(false), decode);
	}

	public MultivaluedMap<String, String> getQueryParameters() {
		return getQueryParameters(true);
	}

	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {

		if (!caseInsensitiveQueries) {
			return JAXRSUtils.getStructuredParams(getQueryString(), "&", decode, decode);
		}

		MultivaluedMap<String, String> queries = new MetadataMap<String, String>(false, true);
		JAXRSUtils.getStructuredParams(queries, getQueryString(), "&", decode, decode);
		return queries;

	}

	private String getQueryString() {
		return "";
	}

	public URI getRequestUri() {
		String path = getAbsolutePathAsString();
		String queries = getQueryString();
		if (queries != null) {
			path += "?" + queries;
		}
		return URI.create(path);
	}

	public UriBuilder getRequestUriBuilder() {
		return new UriBuilderImpl(getRequestUri());
	}

	public MultivaluedMap<String, String> getPathParameters() {
		return getPathParameters(true);
	}

	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		MetadataMap<String, String> values = new MetadataMap<String, String>();
		if (templateParams == null) {
			return values;
		}
		for (Map.Entry<String, List<String>> entry : templateParams.entrySet()) {
			if (entry.getKey().equals(URITemplate.FINAL_MATCH_GROUP)) {
				continue;
			}
			values.add(entry.getKey(), decode ? HttpUtils.pathDecode(entry.getValue().get(0)) : entry.getValue().get(0));
		}
		return values;
	}

	public List<Object> getMatchedResources() {
		if (stack != null) {
			List<Object> resources = new LinkedList<Object>();
			for (MethodInvocationInfo invocation : stack) {
				resources.add(0, invocation.getRealClass());
			}
			return resources;
		}
		LOG.fine("No resource stack information, returning empty list");
		return Collections.emptyList();
	}

	public List<String> getMatchedURIs() {
		return getMatchedURIs(true);
	}

	public List<String> getMatchedURIs(boolean decode) {
		if (stack != null) {
			List<String> objects = new ArrayList<String>();
			List<String> uris = new LinkedList<String>();
			String sum = "";
			for (MethodInvocationInfo invocation : stack) {
				OperationResourceInfo ori = invocation.getMethodInfo();
				URITemplate[] paths = { ori.getClassResourceInfo().getURITemplate(), ori.getURITemplate() };
				for (URITemplate t : paths) {
					if (t != null) {
						String v = t.getValue();
						sum += "/" + (decode ? HttpUtils.pathDecode(v) : v);
					}
				}
				UriBuilder ub = UriBuilder.fromPath(sum);
				objects.addAll(invocation.getTemplateValues());
				uris.add(0, ub.build(objects.toArray()).normalize().getPath());
			}
			return uris;
		}
		LOG.fine("No resource stack information, returning empty list");
		return Collections.emptyList();
	}

	private String doGetPath(boolean decode, boolean addSlash) {
		// String path = HttpUtils.getPathToMatch(message, addSlash);
		// return decode ? HttpUtils.pathDecode(path) : path;
		return "";
	}

	private String getAbsolutePathAsString() {
		// String address = getBaseUri().toString();
		// if (MessageUtils.isRequestor(message)) {
		// return address;
		// }
		// String path = doGetPath(false, false);
		// if (path.startsWith("/") && address.endsWith("/")) {
		// address = address.substring(0, address.length() - 1);
		// }
		// return address + path;

		return "";
	}

	public URI relativize(URI arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI resolve(URI arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
