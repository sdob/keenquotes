/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.whitemagicsoftware.keenquotes.lex.FilterType.FILTER_PLAIN;
import static com.whitemagicsoftware.keenquotes.lex.FilterType.FILTER_XML;

/**
 * Resolves straight quotes into curly quotes throughout a document.
 */
@SuppressWarnings( "unused" )
public class Curler implements Function<String, String> {

  private final Contractions mContractions;
  private final boolean mEntities;

  /**
   * Maps quotes to curled character equivalents.
   *
   * @param c        Contractions listings.
   * @param entities {@code true} to convert quotation marks to HTML entities.
   */
  public Curler(
    final Contractions c,
    final boolean entities
  ) {
    assert c != null;

    mContractions = c;
    mEntities = entities;
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
      replace( output, offset, mEntities ),
      (mEntities ? FILTER_XML : FILTER_PLAIN).filter()
    );

    return output.toString();
  }

  /**
   * Replaces non-ambiguous tokens with their equivalent string representation.
   *
   * @param output   Continuously updated result document.
   * @param offset   Accumulating index where {@link Token} is replaced.
   * @param entities {@code true} to convert quotation marks to HTML entities.
   * @return Instructions to replace a {@link Token} in the result document.
   */
  public static Consumer<Token> replace(
    final StringBuilder output,
    final AtomicInteger offset,
    final boolean entities
  ) {
    return token -> {
      if( !token.isAmbiguous() ) {
        final var text = token.toString( entities );

        output.replace(
          token.began() + offset.get(),
          token.ended() + offset.get(),
          text
        );

        offset.addAndGet( text.length() - (token.ended() - token.began()) );
      }
    };
  }
}
