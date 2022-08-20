/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

/**
 * Identifies the type of quotation mark found while parsing prose.
 */
enum TokenType {
  QUOTE_OPENING_SINGLE( "opening-single" ),
  QUOTE_OPENING_DOUBLE( "opening-double" ),
  QUOTE_CLOSING_SINGLE( "closing-single" ),
  QUOTE_CLOSING_DOUBLE( "closing-double" ),
  QUOTE_APOSTROPHE( "apostrophe" ),
  QUOTE_STRAIGHT_SINGLE,
  QUOTE_STRAIGHT_DOUBLE,
  QUOTE_PRIME_SINGLE,
  QUOTE_PRIME_DOUBLE,
  QUOTE_PRIME_TRIPLE,
  QUOTE_PRIME_QUADRUPLE,
  QUOTE_AMBIGUOUS_LEADING( "leading-ambiguous" ),
  QUOTE_AMBIGUOUS_LAGGING( "lagging-ambiguous" ),
  AMBIGUOUS( "ambiguous" ),
  NONE;

  private final String mName;

  TokenType() {
    this( "token" );
  }

  TokenType( final String name ) {
    mName = name;
  }

  @Override
  public String toString() {
    return mName;
  }
}
