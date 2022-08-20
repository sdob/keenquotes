/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Denotes what filtering to apply when scanning for quotation marks.
 */
public enum ParserType {
  /**
   * Curls all quotation marks.
   */
  PARSER_PLAIN( filter -> false ),

  /**
   * Suppresses curling quotation marks within certain XML elements.
   */
  PARSER_XML( new XmlFilter() );

  private final LexerFilter mFilter;

  ParserType( final LexerFilter filter ) {
    mFilter = filter;
  }

  LexerFilter filter() {
    return mFilter;
  }
}
