/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.pattern.parser.ScanException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;


abstract public class PatternLayoutBase<E> extends LayoutBase<E> {

  Converter<E> head;
  String pattern;
  protected PostCompileProcessor<E> postCompileProcessor;
  
  Map<String, String> instanceConverterMap = new HashMap<String, String>();
  
  /**
   * Concrete implementations of this class are responsible for elaborating the
   * mapping between pattern words and converters.
   * 
   * @return A map associating pattern words to the names of converter classes
   */
  abstract public Map<String, String> getDefaultConverterMap();

  /**
   * Returns a map where the default converter map is merged with the map
   * contained in the context.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> getEffectiveConverterMap() {
    
    Map<String, String> effectiveMap = new HashMap<String, String>();
    

    // add the least specific map fist
    Map<String, String> defaultMap = getDefaultConverterMap();
    if (defaultMap != null) {
      effectiveMap.putAll(defaultMap);
    }
    
    // contextMap is more specific than the default map
    Context context = getContext();
    if (context != null) {
      Map<String, String> contextMap = (Map) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
      if (contextMap != null) {
        effectiveMap.putAll(contextMap);
      }
    }
    
    // set the most specific map last
    effectiveMap.putAll(instanceConverterMap);
    
    return effectiveMap;
  }

  public void start() {
    try {
      Parser<E> p = new Parser<E>(pattern);
      if (getContext() != null) {
        p.setContext(getContext());
      }
      Node t = p.parse();
      this.head = p.compile(t, getEffectiveConverterMap());
      if(postCompileProcessor != null) {
        postCompileProcessor.process(head);
      }
      setContextForConverters(head);
      ConverterUtil.startConverters(this.head);
      super.start();
    } catch (ScanException sce) {
      StatusManager sm = getContext().getStatusManager();
      sm.add(new ErrorStatus("Failed to parse pattern \"" + getPattern()
          + "\".", this, sce));
    }
  }

  public void setPostCompileProcessor(
      PostCompileProcessor<E> postCompileProcessor) {
    this.postCompileProcessor = postCompileProcessor;
  }
  
  protected void setContextForConverters(Converter<E> head) {
    Context context = getContext();
    Converter c = head;
    while (c != null) {
      if (c instanceof ContextAware) {
        ((ContextAware) c).setContext(context);
      }
      c = c.getNext();
    }
  }

  protected String writeLoopOnConverters(E event) {
    StringBuffer buf = new StringBuffer(128);
    Converter<E> c = head;
    while (c != null) {
      c.write(buf, event);
      c = c.getNext();
    }
    return buf.toString();
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public String toString() {
    return this.getClass().getName() + "(" + getPattern() + ")";
  }

  public Map<String, String> getInstanceConverterMap() {
    return instanceConverterMap;
  }
}
