/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.lex;

/**
 * Denotes what filtering to apply when scanning for quotation marks.
 */
public enum FilterType {
  /**
   * Curls all quotation marks.
   */
  FILTER_PLAIN( filter -> false ),

  /**
   * Suppresses curling quotation marks within certain XML elements.
   */
  FILTER_XML( new XmlFilter() );

  private final LexerFilter mFilter;

  FilterType( final LexerFilter filter ) {
    mFilter = filter;
  }

  public LexerFilter filter() {
    return mFilter;
  }
}
