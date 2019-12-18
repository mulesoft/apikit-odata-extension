/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import static org.junit.Assert.assertEquals;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

public class DateParserTestCase {

  @Test
  public void dateParserTest1() throws ParseException {
    Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-09-18 23:46:19.38");
    Date d2 = DateParser.parse("2013-09-18T23:46:19.3857256Z");
    assertEquals(d1, d2);
  }

  @Test
  public void dateParserTest2() throws ParseException {
    Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1996-07-04 00:00:00.0");
    Date d2 = DateParser.parse("1996-07-04 00:00:00.0");
    assertEquals(d1, d2);
  }

  @Test
  public void dateParserTest3() throws ParseException {
    Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1996-07-04 00:00:00.0");
    Date d2 = DateParser.parse("1996-07-04T00:00:00.0");
    assertEquals(d1, d2);
  }

  @Test
  public void dateParserInvalidFormatTest() throws ParseException {
    Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("0000-00-00 00:00:00.0");
    Date d2 = DateParser.parse("199-2-1T4:5:3.");
    assertEquals(d1, d2);
  }
}
