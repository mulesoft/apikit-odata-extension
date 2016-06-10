/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.mule.module.apikit.model.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * 
 * @author arielsegura
 */
public class RamlGenerator {

	private static Configuration fmkCfg;
	private EntityModelParser entityModelParser;

	public RamlGenerator() {
		entityModelParser = new EntityModelParser();
	}

	private static Configuration getConfiguration() {
		if (fmkCfg == null) {
			fmkCfg = new Configuration();

			// Where do we load the templates from:
			fmkCfg.setClassForTemplateLoading(RamlGenerator.class, "/");

			// Some other recommended settings:
			fmkCfg.setIncompatibleImprovements(new Version(2, 3, 20));
			fmkCfg.setDefaultEncoding("UTF-8");
			fmkCfg.setLocale(Locale.US);
			fmkCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		}
		return fmkCfg;
	}

	public String generate(String pathToModel) throws JSONException, IOException, TemplateException, ProcessingException,
			EntityModelParsingException {
		return generate(pathToModel, entityModelParser.getEntities(pathToModel));
	}

	private String generate(String pathToModel, List<Entity> entities) throws IOException, TemplateException {

		Map<String, Object> raml = new HashMap<String, Object>();

		Configuration cfg = getConfiguration();

		// modify the raml object
		raml.put("title", "Auto-generated RAML");
		raml.put("version", "0.1");
		raml.put("ramlVersion", "1.0");

		List<Map<String, Object>> resources = new ArrayList<Map<String, Object>>();

		for (Entity entity : entities) {
			Map<String, Object> resource = new HashMap<String, Object>();
			resource.put("name", entity.getName());
			resource.put("elementName", entity.getElementName());
			resource.put("collectionName", entity.getCollectionName());
			resource.put("id", buildKeyForResource(entity));
			resources.add(resource);
		}
		raml.put("resources", resources);
		Template template = cfg.getTemplate("api-raml-template.ftl");

		Writer out = new StringWriter();
		template.process(raml, out);

		return out.toString();

	}

	/**
	 * 
	 * @param entity
	 * @return {entityId} or key1_{key1}-key2_{key2}-...-keyN_{keyN}
	 */
	private String buildKeyForResource(Entity entity) {
		List<String> keys = entity.getKeys();
		String ret = "";
		String delim = "";
		if (keys.size() > 1) {
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				ret += delim;
				ret += key + "_{" + key + "}";
				delim = "-";
			}
		} else {
			ret = "{" + keys.get(0) + "}";
		}
		return ret;
	}

}