package com.whitemagicsoftware.keenquotes;

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
public class Converter implements Function<String, String> {
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

  private final Consumer<Lexeme> mUnresolved;
  private final Contractions mContractions;

  public Converter( final Consumer<Lexeme> unresolved ) {
    this( unresolved, new Contractions.Builder().build() );
  }

  public Converter( final Consumer<Lexeme> unresolved, final Contractions c ) {
    mUnresolved = unresolved;
    mContractions = c;
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
    final var parser = new Parser( text, mContractions );
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
        result.append( REPLACEMENTS.get( token.getType() ) );
      }

      position = token.ended();
    }

    return result.append( text.substring( position ) ).toString();
  }
}
