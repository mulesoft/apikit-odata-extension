/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata;

import org.mule.module.apikit.odata.formatter.ODataPayloadFormatter.Format;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;

import java.util.List;

public class ODataResponseTransformer {
	public static Message transform(ODataPayload payload, List<Format> formats) throws Exception {
		String formatted = null ;
		MediaType mediaType = null;

	    if (payload.getValue() instanceof String) {
	    	mediaType = MediaType.TEXT;
	    	formatted = (String) payload.getValue();
	    } else {
			boolean isJson = formats.contains(Format.Json) && !formats.contains(Format.Atom);
            formatted = payload.getFormatter().format(isJson ? Format.Json : Format.Atom);

			if (isJson) {
				mediaType = MediaType.APPLICATION_JSON;
			} else {
				if (payload.getFormatter().supportsAtom()) {
					mediaType = MediaType.ATOM;

				} else {
					mediaType = MediaType.APPLICATION_XML;
				}
			}
		}

	    return Message.builder().value(formatted).mediaType(mediaType).build();
	}

}
