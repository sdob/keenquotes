/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Responsible for creating new {@link Parser} instances based on the
 * {@link ParserType}. The document content format must be known in advance.
 */
public class ParserFactory {
  public enum ParserType {
    PARSER_PLAIN,
    PARSER_XML
  }

  private final ParserType mParserType;

  public ParserFactory( final ParserType parserType ) {
    mParserType = parserType;
  }

  public Parser createParser(
    final String text, final Contractions contractions ) {

    return mParserType == ParserType.PARSER_PLAIN
      ? new Parser( text, contractions )
      : new XmlParser( text, contractions );
  }
}
