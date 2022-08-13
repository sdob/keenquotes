/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Turns text into {@link Lexeme}s, allowing XML elements to be ignored.
 */
public final class XmlParser extends Parser {
  /**
   * Constructs a new {@link Parser} using the default contraction sets
   * to help resolve some ambiguous scenarios.
   *
   * @param text         The prose to parse, containing zero or more quotation
   *                     characters.
   * @param contractions Custom sets of contractions to help resolve
   */
  public XmlParser(
    final String text, final Contractions contractions ) {
    super( text, contractions );
  }
}
