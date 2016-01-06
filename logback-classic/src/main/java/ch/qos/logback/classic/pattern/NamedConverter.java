/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class NamedConverter extends ClassicConverter {

  Abbreviator abbreviator = null;

  private static final int CACHE_SIZE = 10000;

  private final Map<String, String> abbrvCache = Collections.synchronizedMap(new LinkedHashMap<String, String>(CACHE_SIZE + 1, 1.0f) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
          return size() > CACHE_SIZE;
      }
  });

  /**
   * Gets fully qualified name from event.
   *
   * @param event
   *          The LoggingEvent to process, cannot not be null.
   * @return name, must not be null.
   */
  protected abstract String getFullyQualifiedName(final ILoggingEvent event);

  public void start() {
    String optStr = getFirstOption();
    if (optStr != null) {
      try {
        int targetLen = Integer.parseInt(optStr);
        if (targetLen == 0) {
          abbreviator = new ClassNameOnlyAbbreviator();
        } else if (targetLen > 0) {
          abbreviator = new TargetLengthBasedClassNameAbbreviator(targetLen);
        }
      } catch (NumberFormatException nfe) {
        // FIXME: better error reporting
      }
    }
  }

  public String convert(ILoggingEvent event) {
    String fqn = getFullyQualifiedName(event);

//    if (abbreviator == null) {
//      return fqn;
//    } else {
//      return abbreviator.abbreviate(fqn);
//    }

      if (abbreviator == null) {
          return fqn;
      } else {
          final String str = abbrvCache.get(fqn);
          if (str != null) {
              return str;
          } else {
              final String newStr = abbreviator.abbreviate(fqn);
              abbrvCache.put(fqn, newStr);
              return newStr;
          }
      }
  }
}
