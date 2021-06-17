/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.function.BiFunction;

import static com.keenwrite.quotes.Lexeme.createLexeme;
import static com.keenwrite.quotes.LexemeType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;
import static java.text.CharacterIterator.DONE;

/**
 * Turns text into words, numbers, punctuation, spaces, and more.
 */
public class Lexer {
  /**
   * Iterates over the entire string of text to help produce lexemes.
   */
  private final CharacterIterator mIterator;

  /**
   * Default constructor, no state.
   */
  public Lexer( final String text ) {
    mIterator = new StringCharacterIterator( text );
  }

  public Lexeme next() {
    return parse( mIterator );
  }

  /**
   * Tokenizes a sequence of characters. The order of comparisons is optimized
   * towards probability of the occurrence of a character in regular English
   * prose: letters, space, quotation marks, numbers, periods, new lines,
   * then end of text.
   *
   * @param i The sequence of characters to tokenize.
   * @return The next token in the sequence.
   */
  private Lexeme parse( final CharacterIterator i ) {
    int began = i.getIndex();
    boolean isWord = false;
    Lexeme lexeme = null;

    do {
      final var curr = i.current();

      if( isLetter( curr ) ) {
        isWord = true;

        if( !isLetter( peek( i ) ) ) {
          lexeme = createLexeme( WORD, began, i.getIndex() );
        }
      }
      else if( curr == ' ' ) {
        lexeme = createLexeme( SPACE, began, i.getIndex() );
      }
      else if( curr == '\'' ) {
        lexeme = createLexeme( QUOTE_SINGLE, began, i.getIndex() );
      }
      else if( curr == '"' ) {
        lexeme = createLexeme( QUOTE_DOUBLE, began, i.getIndex() );
      }
      else if( curr == '‘') {
        lexeme = createLexeme( QUOTE_SINGLE_OPENING, began, i.getIndex() );
      }
      else if( curr == '’') {
        lexeme = createLexeme( QUOTE_SINGLE_CLOSING, began, i.getIndex() );
      }
      else if( curr == '-' && peek( i ) == '-' || curr == '—' ) {
        slurp( i, ( next, ci ) -> next == '-' || next == '—' );

        lexeme = createLexeme( DASH, began, i.getIndex() );
      }
      else if( isDigit( curr ) || isNumeric( curr ) && isDigit( peek( i ) ) ) {
        // Parse all consecutive number characters to prevent the main loop
        // from switching back to word tokens.
        slurp( i, ( next, ci ) ->
          isDigit( next ) || isNumeric( next ) && isDigit( peek( ci ) )
        );

        lexeme = createLexeme( isWord ? WORD : NUMBER, began, i.getIndex() );
      }
      else if( curr == '-' ) {
        lexeme = createLexeme( HYPHEN, began, i.getIndex() );
      }
      else if( curr == '.' ) {
        // Parse all consecutive periods into an ellipsis lexeme. This will
        // not capture space-separated ellipsis (such as ". . .").
        lexeme = createLexeme(
          slurp( i, ( next, ci ) -> next == '.' ) == 0 ? PERIOD : ELLIPSIS,
          began, i.getIndex()
        );
      }
      else if( curr == '\r' || curr == '\n' ) {
        final var cr = new int[]{curr == '\r' ? 1 : 0};
        final var lf = new int[]{curr == '\n' ? 1 : 0};

        // Swallow all consecutive CR (Mac), CRLF (Windows), and/or LF (Unix).
        slurp(
          i, ( next, ci ) -> {
            cr[ 0 ] += next == '\r' ? 1 : 0;
            lf[ 0 ] += next == '\n' ? 1 : 0;
            return next == '\r' || next == '\n';
          }
        );

        final var eol = cr[ 0 ] + lf[ 0 ] == 1 || cr[ 0 ] == 1 && lf[ 0 ] == 1;
        lexeme = createLexeme( eol ? EOL : EOP, began, i.getIndex() );
      }
      else if( isWhitespace( curr ) ) {
        lexeme = createLexeme( SPACE, began, i.getIndex() );
      }
      else if( curr == '\\' ) {
        final var next = i.next();

        if( next == '\'' ) {
          lexeme = createLexeme( ESC_SINGLE, began, i.getIndex() );
        }
        else if( next == '\"' ) {
          lexeme = createLexeme( ESC_DOUBLE, began, i.getIndex() );
        }
        else {
          // Push back the escaped character, which wasn't a straight quote.
          i.previous();
        }
      }
      else if( curr == '(' || curr == '{' || curr == '[' ) {
        lexeme = createLexeme( OPENING_GROUP, began, i.getIndex() );
      }
      else if( curr == ')' || curr == '}' || curr == ']' ) {
        lexeme = createLexeme( CLOSING_GROUP, began, i.getIndex() );
      }
      else if( curr == '=' ) {
        lexeme = createLexeme( EQUALS, began, i.getIndex() );
      }
      else if( curr != DONE ) {
        lexeme = createLexeme( PUNCT, began, i.getIndex() );
      }
      else {
        lexeme = Lexeme.EOT;
      }

      i.next();
    }
    while( lexeme == null );

    return lexeme;
  }

  /**
   * Answers whether the given character can be considered part of a word
   * or not. This will include {@code _} and {@code *} because plain text
   * formats often use those characters to emphasize a word.
   *
   * @param curr The character to check as being part of a word.
   * @return {@code true} if the given character is a letter or a formatting
   * indicator.
   */
  private static boolean isLetter( final char curr ) {
    return Character.isLetter( curr ) || curr == '_' || curr == '*';
  }

  /**
   * Answers whether the given character can be considered part of a number
   * or not. This does not include digits, which are checked independently
   * from this method.
   *
   * @param curr The character to check as being related to numbers.
   * @return {@code true} if the given character can be considered part of
   * a number (e.g., -2,000.2^2 is considered a single number).
   */
  private static boolean isNumeric( final char curr ) {
    return
      curr == '.' || curr == ',' || curr == '-' || curr == '+' || curr == '^';
  }

  private static char peek( final CharacterIterator ci ) {
    final var ch = ci.next();
    ci.previous();
    return ch;
  }

  /**
   * Parse all characters that match a given function.
   *
   * @param ci The iterator containing characters to parse.
   * @param f  The function that determines when slurping stops.
   * @return The number of characters parsed.
   */
  private static int slurp(
    final CharacterIterator ci,
    final BiFunction<Character, CharacterIterator, Boolean> f ) {
    char next;
    int count = 0;

    do {
      next = ci.next();
      count++;
    }
    while( f.apply( next, ci ) );

    // The loop above will overshoot the tally by one character.
    ci.previous();

    return --count;
  }
}
