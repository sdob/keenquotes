/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayList;
import java.util.Map;

import static com.keenwrite.quotes.TokenType.*;
import static java.util.Collections.sort;

/**
 * Responsible for replacing {@link Token} instances with equivalent smart
 * quotes (or straight quotes).
 */
public class SmartQuotes {
  private static final Map<TokenType, String> REPLACEMENTS = Map.of(
    QUOTE_OPENING_SINGLE, "&lsquo;",
    QUOTE_CLOSING_SINGLE, "&rsquo;",
    QUOTE_OPENING_DOUBLE, "&ldquo;",
    QUOTE_CLOSING_DOUBLE, "&rdquo;",
    QUOTE_STRAIGHT_SINGLE, "'",
    QUOTE_STRAIGHT_DOUBLE, "\"",
    QUOTE_APOSTROPHE, "&apos;",
    QUOTE_PRIME_SINGLE, "&prime;",
    QUOTE_PRIME_DOUBLE, "&Prime;"
  );

  public String replace( final String text ) {
    final var parser = new Parser( text );
    final var tokens = new ArrayList<Token>();

    // Store all parsed quotation marks.
    parser.parse( tokens::add );

    // The parser may emit tokens in any order.
    sort( tokens );

    final var result = new StringBuilder( text.length() );
    var position = 0;

    for( final var token : tokens ) {
      result.append( text, position, token.began() );
      result.append( REPLACEMENTS.get( token.getType() ) );

      position = token.ended();
    }

    return result.append( text.substring( position ) ).toString();
  }
}
