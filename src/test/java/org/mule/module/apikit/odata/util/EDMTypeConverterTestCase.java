/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mule.module.apikit.odata.exception.ODataInvalidFormatException;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.UnsignedByte;
import org.odata4j.edm.EdmSimpleType;

public class EDMTypeConverterTestCase {

  @Test
  public void dataStringConversionTest() throws ODataInvalidFormatException {
    assertTrue(compareOProperties(OProperties.string("test", String.valueOf("test")),
        EDMTypeConverter.getOProperty("test", "test", EdmSimpleType.STRING)));
    assertTrue(compareOProperties(OProperties.string("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.STRING)));
  }

  @Test
  public void dataInt16ConversionTest() throws ODataInvalidFormatException {
    Short s = 2;
    assertTrue(compareOProperties(OProperties.int16("test", s),
        EDMTypeConverter.getOProperty("test", 2, EdmSimpleType.INT16)));
    assertTrue(compareOProperties(OProperties.int16("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.INT16)));
  }

  @Test
  public void dataInt32ConversionTest() throws ODataInvalidFormatException {
    assertTrue(compareOProperties(OProperties.int32("test", 2),
        EDMTypeConverter.getOProperty("test", 2, EdmSimpleType.INT32)));
    assertTrue(compareOProperties(OProperties.int32("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.INT32)));
  }

  @Test
  public void dataInt64ConversionTest() throws ODataInvalidFormatException {
    Long s = 2L;
    assertTrue(compareOProperties(OProperties.int64("test", s),
        EDMTypeConverter.getOProperty("test", 2, EdmSimpleType.INT64)));
    assertTrue(compareOProperties(OProperties.int64("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.INT64)));
  }

  @Test
  public void dataBinaryConversionTest() throws ODataInvalidFormatException {
    byte[] b = "1".getBytes();
    assertTrue(compareArraysOProperties(OProperties.binary("test", b),
        EDMTypeConverter.getOProperty("test", "1", EdmSimpleType.BINARY)));
    assertTrue(compareArraysOProperties(OProperties.binary("test", new byte[0]),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.BINARY)));
  }

  @Test
  public void dataBooleanConversionTest() throws ODataInvalidFormatException {
    assertTrue(compareOProperties(OProperties.boolean_("test", true),
        EDMTypeConverter.getOProperty("test", true, EdmSimpleType.BOOLEAN)));
    assertTrue(compareOProperties(OProperties.boolean_("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.BOOLEAN)));
  }

  @Test
  public void dataByteConversionTest() throws ODataInvalidFormatException {
    UnsignedByte b = new UnsignedByte(244);
    assertTrue(compareOProperties(OProperties.byte_("test", b),
        EDMTypeConverter.getOProperty("test", 244, EdmSimpleType.BYTE)));
    assertTrue(compareOProperties(OProperties.byte_("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.BYTE)));
  }

  @Test
  public void dataDecimalConversionTest() throws ODataInvalidFormatException {
    double d = new Double(1.0);
    BigDecimal d1 = null;
    assertTrue(compareOProperties(OProperties.decimal("test", d),
        EDMTypeConverter.getOProperty("test", 1, EdmSimpleType.DECIMAL)));
    assertTrue(compareOProperties(OProperties.decimal("test", d1),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.DECIMAL)));
  }

  @Test
  public void dataDoubleConversionTest() throws ODataInvalidFormatException {
    double d = 1.0;
    assertTrue(compareOProperties(OProperties.double_("test", d),
        EDMTypeConverter.getOProperty("test", 1.0, EdmSimpleType.DOUBLE)));
    assertTrue(compareOProperties(OProperties.double_("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.DOUBLE)));
  }

  @Test
  public void dataSingleConversionTest() throws ODataInvalidFormatException {
    float f = 1f;
    assertTrue(compareOProperties(OProperties.single("test", f),
        EDMTypeConverter.getOProperty("test", 1, EdmSimpleType.SINGLE)));
    assertTrue(compareOProperties(OProperties.single("test", null),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.SINGLE)));
  }

  @Test
  public void dataSbyteConversionTest() throws ODataInvalidFormatException {
    byte b = 100;
    byte b1 = 0;
    assertTrue(compareOProperties(OProperties.sbyte_("test", b),
        EDMTypeConverter.getOProperty("test", 100, EdmSimpleType.SBYTE)));
    assertTrue(compareOProperties(OProperties.sbyte_("test", b1),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.SBYTE)));
  }

  @Test
  public void dataDateTimeConversionTest() throws ODataInvalidFormatException, ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
    Date parsedDate = formatter.parse("1996-07-04 00:00:00.0");
    LocalDateTime d1 = null;
    assertTrue(compareOProperties(OProperties.datetime("test", parsedDate),
        EDMTypeConverter.getOProperty("test", "1996-07-04 00:00:00.0", EdmSimpleType.DATETIME)));
    assertTrue(compareOProperties(OProperties.datetime("test", d1),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.DATETIME)));
  }

  @Test
  public void dataDateTimeOffsetConversionTest()
      throws ODataInvalidFormatException, ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
    Date parsedDate = formatter.parse("1996-07-04 00:00:00.0");
    DateTime d1 = null;
    assertTrue(compareOProperties(OProperties.datetimeOffset("test", new DateTime(parsedDate)),
        EDMTypeConverter.getOProperty("test", "1996-07-04 00:00:00.0",
            EdmSimpleType.DATETIMEOFFSET)));
    assertTrue(compareOProperties(OProperties.datetimeOffset("test", d1),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.DATETIMEOFFSET)));
  }

  @Test
  public void dataTimeConversionTest() throws ODataInvalidFormatException, ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.s");
    Date parsedDate = formatter.parse("1996-07-04 00:00:00.0");
    Date d1 = null;
    assertTrue(compareOProperties(OProperties.time("test", parsedDate),
        EDMTypeConverter.getOProperty("test", "1996-07-04 00:00:00.0", EdmSimpleType.TIME)));
    assertTrue(compareOProperties(OProperties.time("test", d1),
        EDMTypeConverter.getOProperty("test", null, EdmSimpleType.TIME)));
  }

  @SuppressWarnings("rawtypes")
  private boolean compareOProperties(OProperty a, OProperty b) {
    if (!a.getName().equals(b.getName()))
      return false;
    if (!a.getClass().equals(b.getClass()))
      return false;
    if (!a.getType().equals(b.getType()))
      return false;
    if (a.getValue() == null && b.getValue() == null)
      return true;
    if (!a.getValue().equals(b.getValue()))
      return false;
    return true;
  }

  @SuppressWarnings("rawtypes")
  private boolean compareArraysOProperties(OProperty a, OProperty b) {
    if (!a.getName().equals(b.getName()))
      return false;
    if (!a.getClass().equals(b.getClass()))
      return false;
    if (!a.getType().equals(b.getType()))
      return false;
    if (a.getValue() == null && b.getValue() == null)
      return true;
    if (!Arrays.equals((byte[]) a.getValue(), (byte[]) b.getValue()))
      return false;
    return true;
  }
}
