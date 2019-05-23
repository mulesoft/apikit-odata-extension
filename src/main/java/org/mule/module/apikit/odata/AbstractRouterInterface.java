/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;

public interface AbstractRouterInterface {
  Publisher<CoreEvent> processEvent(CoreEvent event) throws MuleException;
}
