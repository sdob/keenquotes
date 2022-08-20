/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import com.whitemagicsoftware.keenquotes.lex.FilterType;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.whitemagicsoftware.keenquotes.parser.TokenType.*;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

/**
 * Resolves straight quotes into curly quotes throughout a document.
 */
@SuppressWarnings( "unused" )
public class Curler implements Function<String, String> {
  /**
   * Provides an entity-based set of {@link Token} replacements.
   */
  public static final Map<TokenType, String> ENTITIES = ofEntries(
    entry( QUOTE_OPENING_SINGLE, "&lsquo;" ),
    entry( QUOTE_CLOSING_SINGLE, "&rsquo;" ),
    entry( QUOTE_OPENING_DOUBLE, "&ldquo;" ),
    entry( QUOTE_CLOSING_DOUBLE, "&rdquo;" ),
    entry( QUOTE_STRAIGHT_SINGLE, "'" ),
    entry( QUOTE_STRAIGHT_DOUBLE, "\"" ),
    entry( QUOTE_APOSTROPHE, "&apos;" ),
    entry( QUOTE_PRIME_SINGLE, "&prime;" ),
    entry( QUOTE_PRIME_DOUBLE, "&Prime;" ),
    entry( QUOTE_PRIME_TRIPLE, "&tprime;" ),
    entry( QUOTE_PRIME_QUADRUPLE, "&qprime;" )
  );

  /**
   * Provides a character-based set of {@link Token} replacements.
   */
  public static final Map<TokenType, String> CHARS = ofEntries(
    entry( QUOTE_OPENING_SINGLE, "‘" ),
    entry( QUOTE_CLOSING_SINGLE, "’" ),
    entry( QUOTE_OPENING_DOUBLE, "“" ),
    entry( QUOTE_CLOSING_DOUBLE, "”" ),
    entry( QUOTE_STRAIGHT_SINGLE, "'" ),
    entry( QUOTE_STRAIGHT_DOUBLE, "\"" ),
    entry( QUOTE_APOSTROPHE, "’" ),
    entry( QUOTE_PRIME_SINGLE, "′" ),
    entry( QUOTE_PRIME_DOUBLE, "″" ),
    entry( QUOTE_PRIME_TRIPLE, "‴" ),
    entry( QUOTE_PRIME_QUADRUPLE, "⁗" )
  );

  private final Contractions mContractions;
  private final Map<TokenType, String> mReplacements;
  private final FilterType mFilterType;

  /**
   * Maps quotes to HTML entities.
   *
   * @param c          Contractions listings.
   * @param parserType Creates a parser based on document content structure.
   */
  public Curler(
    final Contractions c,
    final FilterType parserType
  ) {
    this( c, ENTITIES, parserType );
  }

  /**
   * Maps quotes to curled character equivalents.
   *
   * @param c            Contractions listings.
   * @param replacements Map of recognized quotes to output types (entity or
   *                     Unicode character).
   */
  public Curler(
    final Contractions c,
    final Map<TokenType, String> replacements,
    final FilterType parserType
  ) {
    mContractions = c;
    mReplacements = replacements;
    mFilterType = parserType;
  }

  /**
   * Converts straight quotes to curly quotes and primes. Any quotation marks
   * that cannot be converted are passed to the {@link Consumer}. This method
   * is re-entrant, but not tested to be thread-safe.
   *
   * @param text The text to parse.
   * @return The given text string with as many straight quotes converted to
   * curly quotes as is feasible.
   */
  @Override
  public String apply( final String text ) {
    final var output = new StringBuilder( text );
    final var offset = new AtomicInteger( 0 );

    AmbiguityResolver.resolve(
      text,
      mContractions,
      replace( output, offset, mReplacements ),
      mFilterType.filter()
    );

    return output.toString();
  }

  /**
   * Replaces non-ambiguous tokens with their equivalent string representation.
   *
   * @param output       Continuously updated result document.
   * @param offset       Accumulating index where {@link Token} is replaced.
   * @param replacements Map of {@link TokenType}s to replacement strings.
   * @return Instructions to replace a {@link Token} in the result document.
   */
  public static Consumer<Token> replace(
    final StringBuilder output,
    final AtomicInteger offset,
    final Map<TokenType, String> replacements
  ) {
    return token -> {
      if( !token.isAmbiguous() ) {
        final var entity = token.toString( replacements );

        output.replace(
          token.began() + offset.get(),
          token.ended() + offset.get(),
          entity
        );

        offset.addAndGet( entity.length() - (token.ended() - token.began()) );
      }
    };
  }
}
