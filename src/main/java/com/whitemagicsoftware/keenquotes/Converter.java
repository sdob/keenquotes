/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import com.whitemagicsoftware.keenquotes.ParserFactory.ParserType;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.whitemagicsoftware.keenquotes.TokenType.*;
import static java.util.Collections.sort;

/**
 * Responsible for converting curly quotes to HTML entities throughout a
 * text string.
 */
@SuppressWarnings( "unused" )
public class Converter implements Function<String, String> {
  public static final Map<TokenType, String> ENTITIES = Map.of(
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

  /**
   * Used by external applications to initialize the replacement map.
   */
  public static final Map<TokenType, String> CHARS = Map.of(
    QUOTE_OPENING_SINGLE, "‘",
    QUOTE_CLOSING_SINGLE, "’",
    QUOTE_OPENING_DOUBLE, "“",
    QUOTE_CLOSING_DOUBLE, "”",
    QUOTE_STRAIGHT_SINGLE, "'",
    QUOTE_STRAIGHT_DOUBLE, "\"",
    QUOTE_APOSTROPHE, "’",
    QUOTE_PRIME_SINGLE, "′",
    QUOTE_PRIME_DOUBLE, "″"
  );

  private final Consumer<Lexeme> mUnresolved;
  private final Contractions mContractions;
  private final Map<TokenType, String> mReplacements;
  private final ParserFactory mFactory;

  /**
   * Maps quotes to HTML entities.
   *
   * @param unresolved Consumes {@link Lexeme}s that could not be converted
   *                   into HTML entities.
   * @param parserType Creates a parser based on document content structure.
   */
  public Converter(
    final Consumer<Lexeme> unresolved, final ParserType parserType ) {
    this( unresolved, new Contractions.Builder().build(), parserType );
  }

  /**
   * Maps quotes to HTML entities.
   *
   * @param unresolved Consumes {@link Lexeme}s that could not be converted
   *                   into HTML entities.
   * @param parserType Creates a parser based on document content structure.
   */
  public Converter(
    final Consumer<Lexeme> unresolved,
    final Map<TokenType, String> replacements,
    final ParserType parserType ) {
    this(
      unresolved, new Contractions.Builder().build(), replacements, parserType
    );
  }

  /**
   * Maps quotes to HTML entities.
   *
   * @param unresolved Consumes {@link Lexeme}s that could not be converted
   *                   into HTML entities.
   * @param c          Contractions listings.
   * @param parserType Creates a parser based on document content structure.
   */
  public Converter(
    final Consumer<Lexeme> unresolved,
    final Contractions c,
    final ParserType parserType ) {
    this( unresolved, c, ENTITIES, parserType );
  }

  /**
   * Maps quotes to curled equivalents.
   *
   * @param unresolved   Consumes {@link Lexeme}s that could not be converted
   *                     into HTML entities.
   * @param c            Contractions listings.
   * @param replacements Map of recognized quotes to output types (entity or
   *                     Unicode character).
   */
  public Converter(
    final Consumer<Lexeme> unresolved,
    final Contractions c,
    final Map<TokenType, String> replacements,
    final ParserType parserType ) {
    mUnresolved = unresolved;
    mContractions = c;
    mReplacements = replacements;
    mFactory = new ParserFactory( parserType );
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
    final var parser = mFactory.createParser( text, mContractions );
    final var tokens = new ArrayList<Token>();

    // Parse the tokens and consume all unresolved lexemes.
    parser.parse( tokens::add, mUnresolved );

    // The parser may emit tokens in any order.
    sort( tokens );

    final var result = new StringBuilder( text.length() );
    var position = 0;

    for( final var token : tokens ) {
      if( position <= token.began() ) {
        result.append( text, position, token.began() );
        result.append( mReplacements.get( token.getType() ) );
      }

      position = token.ended();
    }

    return result.append( text.substring( position ) ).toString();
  }
}
