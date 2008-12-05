/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.pattern.parser;

class Token {

  static final int PERCENT = 37;
  static final int LEFT_PARENTHESIS = 40;
  static final int RIGHT_PARENTHESIS = 41;
  static final int MINUS = 45;
  static final int DOT = 46;
  static final int CURLY_LEFT = 123;
  static final int CURLY_RIGHT = 125;
  static final int LITERAL = 1000;
  static final int FORMAT_MODIFIER = 1002;
  static final int KEYWORD = 1004;
  static final int OPTION = 1006;

  static final int EOF = Integer.MAX_VALUE;

  static Token EOF_TOKEN = new Token(EOF, "EOF");
  static Token RIGHT_PARENTHESIS_TOKEN = new Token(RIGHT_PARENTHESIS);
  static Token LEFT_PARENTHESIS_TOKEN = new Token(LEFT_PARENTHESIS);
  static Token PERCENT_TOKEN = new Token(PERCENT);

  private final int type;
  private final Object value;


  public Token(int type) {
    this(type, null);
  }

  public Token(int type, Object value) {
    this.type = type;
    this.value = value;
  }

  public int getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }


  public String toString() {
    String typeStr = null;
    switch (type) {

      case PERCENT:
        typeStr = "%";
        break;
      case FORMAT_MODIFIER:
        typeStr = "FormatModifier";
        break;
      case LITERAL:
        typeStr = "LITERAL";
        break;
      case OPTION:
        typeStr = "OPTION";
        break;
      case KEYWORD:
        typeStr = "KEYWORD";
        break;
      case RIGHT_PARENTHESIS:
        typeStr = "RIGHT_PARENTHESIS";
        break;
      case LEFT_PARENTHESIS:
        typeStr = "LEFT_PARENTHESIS";
        break;
     default:
        typeStr = "UNKNOWN";
    }
    if (value == null) {
      return "Token(" + typeStr + ")";

    } else {
      return "Token(" + typeStr + ", \"" + value + "\")";
    }
  }

  public int hashCode() {
    int result;
    result = type;
    result = 29 * result + (value != null ? value.hashCode() : 0);
    return result;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Token)) return false;

    final Token token = (Token) o;

    if (type != token.type) return false;
    if (value != null ? !value.equals(token.value) : token.value != null) return false;

    return true;
  }
}
