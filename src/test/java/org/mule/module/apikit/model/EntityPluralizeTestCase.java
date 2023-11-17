/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.model;

import static org.junit.Assert.assertEquals;
import static org.mule.module.apikit.model.Entity.pluralizeName;
import org.junit.Test;

public class EntityPluralizeTestCase {
  @Test
  public void testPluralizeRegularSingular() {
    assertEquals("cars", pluralizeName("car"));
  }

  @Test
  public void testPluralizeEndsWithS() {
    assertEquals("dogs", pluralizeName("dog"));
  }

  @Test
  public void testPluralizeEndsWithSS() {
    assertEquals("kisses", pluralizeName("kiss"));
  }

  @Test
  public void testPluralizeEndsWithY() {
    assertEquals("parties", pluralizeName("party"));
  }

  @Test
  public void testPluralizeEndsWithYException() {
    assertEquals("buys", pluralizeName("buy"));
  }

  @Test
  public void testPluralizeEndsWithYAny() {
    assertEquals("any", pluralizeName("any"));
  }

  @Test
  public void testPluralizeEndsWithYYes() {
    assertEquals("yes", pluralizeName("yes"));
  }

  @Test
  public void testPluralizeEndsWithList() {
    assertEquals("shoppingList", pluralizeName("shoppingList"));
  }

  @Test
  public void testPluralizeEndsWithSSPlural() {
    assertEquals("passes", pluralizeName("pass"));
  }
}
