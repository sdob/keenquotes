/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.function.Consumer;

public enum ParserType {
  PARSER_PLAIN( filter -> {} ),
  PARSER_XML( filter -> new XmlFilter() );

  private final Consumer<FastCharacterIterator> mFilter;

  ParserType( final Consumer<FastCharacterIterator> filter ) {
    mFilter = filter;
  }

  Consumer<FastCharacterIterator> filter() {
    return mFilter;
  }
}
