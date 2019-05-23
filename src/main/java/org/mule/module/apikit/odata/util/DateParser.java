/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright law. All
 * use of this software is subject to MuleSoft's Master Subscription Agreement (or other master
 * license agreement) separately entered into in writing between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser {

  public static Date parse(String dateString) {
    try {
      DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      String parsedDate =
          getYear(dateString) + "/" + getMonth(dateString) + "/" + getDay(dateString) + " "
              + getHours(dateString) + ":" + getMinutes(dateString) + ":" + getSeconds(dateString);
      return formatter.parse(parsedDate);
    } catch (Exception e) {
      return null;
    }
  }

  private static int getYear(String dateString) {
    Pattern pattern = Pattern.compile("(\\d{4})");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }

  private static int getMonth(String dateString) {
    Pattern pattern = Pattern.compile("\\D(\\d{2})\\D");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }

  private static int getDay(String dateString) {
    Pattern pattern = Pattern.compile("\\D\\d{2}\\D(\\d{2})");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }

  private static int getHours(String dateString) {
    Pattern pattern = Pattern.compile("(\\d{2}):\\d{2}:\\d{2}");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }

  private static int getMinutes(String dateString) {
    Pattern pattern = Pattern.compile("\\d{2}:(\\d{2}):\\d{2}");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }

  private static int getSeconds(String dateString) {
    Pattern pattern = Pattern.compile("\\d{2}:\\d{2}:(\\d{2})");
    Matcher matcher = pattern.matcher(dateString);
    if (matcher.find()) {
      return Integer.valueOf(matcher.group(1));
    }
    return 0;
  }
}
