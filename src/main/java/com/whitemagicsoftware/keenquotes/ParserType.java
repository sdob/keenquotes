/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

public enum ParserType {
  PARSER_PLAIN( filter -> false ),
  PARSER_XML( new XmlFilter() );

  private final LexerFilter mFilter;

  ParserType( final LexerFilter filter ) {
    mFilter = filter;
  }

  LexerFilter filter() {
    return mFilter;
  }
}
