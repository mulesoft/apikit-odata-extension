/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.model.EntityModelParser;
import org.mule.module.apikit.model.FileUtils;
import org.mule.module.apikit.model.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * 
 * @author arielsegura
 */
public class EntityModelParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private List<Map<String, Object>> mockEntitySet;

    @Before
    public void setUp() throws Exception {
	mockEntitySet = mockEntitySet();
    }

    private List<Map<String, Object>> mockEntitySet() {
		List<Map<String, Object>> newEntitySet = new ArrayList<Map<String, Object>>();
		Map<String, Object> entityDefinition1;
	
		entityDefinition1 = new HashMap<String, Object>();
		entityDefinition1.put("name", "MyEntity");
		entityDefinition1.put("remoteName", "RemoteEntity");
		newEntitySet.add(entityDefinition1);
	
		List<Map<String, Object>> properties1 = new ArrayList<Map<String, Object>>();
		entityDefinition1.put("properties", properties1);
	
		Map<String, Object> property1;
		property1 = new HashMap<String, Object>();
		properties1.add(property1);
		property1.put("name", "MyField");
		property1.put("sample", "22");
		property1.put("type", "Edm.Decimal");
		property1.put("nullable", "false");
		property1.put("key", "true");
		property1.put("description", "This is my field");
		property1.put("precision", "2");
		property1.put("scale", "2");
		
		Map<String, Object> entityDefinition2;
		
		entityDefinition2 = new HashMap<String, Object>();
		entityDefinition2.put("name", "MyEntity");
		entityDefinition2.put("remoteName", "RemoteEntity");
		newEntitySet.add(entityDefinition2);
	
		List<Map<String, Object>> properties2 = new ArrayList<Map<String, Object>>();
		entityDefinition2.put("properties", properties2);
	
		Map<String, Object> property2;
		property2 = new HashMap<String, Object>();
		properties2.add(property2);
		property2.put("name", "MyOtherField");
		property2.put("sample", "bla");
		property2.put("type", "Edm.String");
		property2.put("nullable", "true");
		property2.put("key", "false");
		property2.put("description", "This is another field");
		property2.put("maxLength", "255");
		property2.put("fixedLength", "false");
	
		return newEntitySet;
    }

    @Test
    public void testPositive() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException {
    	
    	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/valid.json"));
	
    	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    	Assert.assertEquals(mockEntitySet.get(0).get("name"), entities.get(0).get("name"));
    	Assert.assertEquals(mockEntitySet.get(0).get("remoteName"), entities.get(0).get("remoteName"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "name"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "sample"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "type"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "nullable"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "key"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "description"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "precision"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "scale"));
    }

    private boolean equalProp(List<Map<String, Object>> entityA,
	    List<Map<String, Object>> entityB, String prop) {
	String propA = (String) ((List<Map<String, Object>>) entityA.get(0).get("properties")).get(0).get(prop);
	String propB = (String) ((List<Map<String, Object>>) entityA.get(0).get("properties")).get(0).get(prop);
	return propA.equals(propB);
    }

    @Test
    public void schemaMissmatchType() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-type.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void schemaMissmatchName() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-name.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void schemaMissmatchEntity() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-entity.json"));
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"properties\",\"remoteName\"])");
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void invalidJson() throws JSONException, FileNotFoundException,
	    IOException, ProcessingException, EntityModelParsingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/invalid.json"));
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"entities\"])");
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

}
