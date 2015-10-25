/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.util.Iterator;

import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class ValidationErrorsHandler {

	public static String handle(ProcessingReport report) {
		String msg = "";
		Iterator<ProcessingMessage> iterator = report.iterator();
		
		while (iterator.hasNext()) {
			ProcessingMessage message = iterator.next();
			
			String pointer = message.asJson().get("instance").get("pointer").toString().replace("\"", "");
			String keyword = message.asJson().get("keyword").toString().replace("\"", "");
			
			if (pointer.endsWith("/entity/name") 
					&& keyword.equals("minLength")) {
				msg += "there are entities with empty names, please fix the model.";
			} else if (keyword.equals("oneOf")) {
					msg += "some of the properties are invalid and cannot be matched against the schema.";
			} else {
				msg += message.getMessage();
			}
		}
		
		if (msg.isEmpty())
			msg = "There are errors in the model, please validate it against the schema.";
		
		return msg;
	}
}
