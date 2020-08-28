/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.processor;

import static java.lang.System.getProperty;
import static org.mule.module.apikit.api.UrlUtils.FULL_DOMAIN;
import static org.mule.runtime.core.api.util.StringUtils.isBlank;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.odata.AbstractRouterInterface;
import org.mule.module.apikit.odata.ODataPayload;
import org.mule.module.apikit.odata.context.OdataContext;
import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.module.apikit.odata.metadata.OdataMetadataManager;
import org.mule.runtime.core.api.event.CoreEvent;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ODataRequestProcessor {

  public static final Pattern LEADING_SLASH = Pattern.compile("^\\/*");
  protected OdataContext oDataContext;

  public ODataRequestProcessor(OdataContext oDataContext) {
    this.oDataContext = oDataContext;
  }

  public abstract ODataPayload process(CoreEvent event, AbstractRouterInterface router,
      List<Format> formats) throws Exception;

  protected OdataMetadataManager getMetadataManager() {
    return oDataContext.getOdataMetadataManager();
  }

  protected String getProtocol(HttpRequestAttributes attributes) {
    String protocol = attributes.getScheme();

    return protocol;
  }

  protected String getHost(HttpRequestAttributes attributes) {
    String cloudHubFullDomain = getProperty(FULL_DOMAIN);
    if (!isBlank(cloudHubFullDomain)) {
      return cloudHubFullDomain;
    }
    return attributes.getHeaders().get("host");
  }

  protected String getCompleteUrl(HttpRequestAttributes attributes) {
    String path = attributes.getRequestPath();
    String url = getProtocol(attributes) + "://" + sanitizeHost(getHost(attributes) + path);
    return url;
  }

  private static String sanitizeHost(String host) {
    return LEADING_SLASH.matcher(host).replaceFirst("");
  }
}
