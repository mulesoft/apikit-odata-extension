/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriBuilderImpl;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class UriInfoImpl implements UriInfo {
  private static final Logger LOG = LogUtils.getL7dLogger(UriInfoImpl.class);

  private String url;

  public UriInfoImpl(String url) {
    this.url = url;
  }

  @Override
  public URI getAbsolutePath() {
    String path = getAbsolutePathAsString();
    return URI.create(path);
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    return new UriBuilderImpl(getAbsolutePath());
  }

  @Override
  public URI getBaseUri() {
    try {
      return new URL(url).toURI();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public UriBuilder getBaseUriBuilder() {
    return new UriBuilderImpl(getBaseUri());
  }

  @Override
  public String getPath() {
    return getPath(true);
  }

  @Override
  public String getPath(boolean decode) {
    String value = doGetPath(decode, true);
    if (value.length() > 1 && value.startsWith("/")) {
      return value.substring(1);
    } else {
      return value;
    }
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return getPathSegments(true);
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    return JAXRSUtils.getPathSegments(getPath(false), decode);
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters() {
    return getQueryParameters(true);
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
    return new MetadataMap<>();
  }

  private String getQueryString() {
    return StringUtils.EMPTY;
  }

  @Override
  public URI getRequestUri() {
    String path = getAbsolutePathAsString();
    String queries = getQueryString();
    if (queries != null) {
      path += "?" + queries;
    }
    return URI.create(path);
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    return new UriBuilderImpl(getRequestUri());
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters() {
    return getPathParameters(true);
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    return new MetadataMap<>();
  }

  @Override
  public List<Object> getMatchedResources() {
    LOG.fine("No resource stack information, returning empty list");
    return Collections.emptyList();
  }

  @Override
  public List<String> getMatchedURIs() {
    return getMatchedURIs(true);
  }

  @Override
  public List<String> getMatchedURIs(boolean decode) {
    LOG.fine("No resource stack information, returning empty list");
    return Collections.emptyList();
  }

  private String doGetPath(boolean decode, boolean addSlash) {
    return StringUtils.EMPTY;
  }

  private String getAbsolutePathAsString() {
    return StringUtils.EMPTY;
  }

  @Override
  public URI relativize(URI arg0) {
    return null;
  }

  @Override
  public URI resolve(URI arg0) {
    return null;
  }
}
