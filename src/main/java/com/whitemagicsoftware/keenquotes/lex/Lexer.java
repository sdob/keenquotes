/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.lex;

import com.whitemagicsoftware.keenquotes.util.FastCharacterIterator;

import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.lex.LexemeGlyph.*;
import static com.whitemagicsoftware.keenquotes.lex.LexemeType.*;
import static java.lang.Character.isWhitespace;
import static java.text.CharacterIterator.DONE;

/**
 * Turns text into words, numbers, punctuation, spaces, and more.
 */
public final class Lexer {
  /**
   * Tokenizes a sequence of characters. The order of comparisons is optimized
   * towards probability of the occurrence of a character in regular English
   * prose: letters, space, quotation marks, numbers, periods, new lines,
   * then end of text.
   *
   * @param text    The sequence of characters to tokenize.
   * @param emitter Recipient of all tokenized character sequences.
   * @param filter  Tokenization preprocessor, usually empty, but can be used
   *                to skip certain character sequences (such as XML tags).
   */
  public static void lex(
    final String text,
    final Consumer<Lexeme> emitter,
    final LexerFilter filter ) {
    lex( new FastCharacterIterator( text ), emitter, filter );
  }

  @SuppressWarnings( "StatementWithEmptyBody" )
  private static void lex(
    final FastCharacterIterator i,
    final Consumer<Lexeme> consumer,
    final LexerFilter filter ) {

    // Ensure at least one lexeme precedes a quotation mark. This is because
    // the algorithm that determines how to curl a quotation mark relies on
    // the character to be the second of four lexemes. Four lexemes provides
    // sufficient context to curl most straight quotes.
    consumer.accept( Lexeme.SOT );

    while( i.hasNext() ) {
      // Allow filters to skip character sequences (such as XML tags). This
      // must allow back-to-back filtering, hence the loop.
      while( filter.test( i ) ) ;

      final var index = i.index();
      final var curr = i.current();
      var token = PUNCT;

      if( isLetter( curr ) ) {
        // T1000 is one word, not a word and a number.
        i.skip( next -> isLetter( next ) || isDigit( next ) );
        token = WORD;
      }
      else if( curr == ' ' ) {
        i.skip( next -> next == ' ' );
        token = SPACE;
      }
      else if( curr == '\r' || curr == '\n' ) {
        final var cr = new int[]{curr == '\r' ? 1 : 0};
        final var lf = new int[]{curr == '\n' ? 1 : 0};

        // Swallow all consecutive CR (Mac), CRLF (Windows), and/or LF (Unix).
        i.skip( next -> {
          cr[ 0 ] += next == '\r' ? 1 : 0;
          lf[ 0 ] += next == '\n' ? 1 : 0;

          return next == '\r' || next == '\n';
        } );

        token = cr[ 0 ] + lf[ 0 ] == 1 || cr[ 0 ] == 1 && lf[ 0 ] == 1
          ? EOL
          : EOP;
      }
      else if( isWhitespace( curr ) ) {
        i.skip( Character::isWhitespace );
        token = SPACE;
      }
      else if( isDigit( curr ) || isNumeric( curr ) && isDigit( i.peek() ) ) {
        // Parse all consecutive number characters to prevent the main loop
        // from switching back to word tokens.
        i.skip(
          next -> isDigit( next ) || isNumeric( next ) && isDigit( i.peek() )
        );
        token = NUMBER;
      }
      else if( curr == '.' ) {
        i.skip( next -> next == '.' || next == ' ' && i.peek() == '.' );

        token = i.index() - index == 0 ? PERIOD : ELLIPSIS;
      }
      else if( curr == '"' ) {
        token = QUOTE_DOUBLE;
      }
      else if( curr == '\'' ) {
        token = QUOTE_SINGLE;
      }
      else if( curr == '-' && i.peek() != '-' ) {
        token = HYPHEN;
      }
      else if( isDash( curr ) ) {
        i.skip( Lexer::isDash );
        token = DASH;
      }
      else if( curr == '(' || curr == '{' || curr == '[' ) {
        token = OPENING_GROUP;
      }
      else if( curr == ')' || curr == '}' || curr == ']' ) {
        token = CLOSING_GROUP;
      }
      else if( LEX_DOUBLE_QUOTE_OPENING.equals( curr ) ) {
        token = QUOTE_DOUBLE_OPENING.with( LEX_DOUBLE_QUOTE_OPENING );
      }
      else if( LEX_DOUBLE_QUOTE_CLOSING.equals( curr ) ) {
        token = QUOTE_DOUBLE_CLOSING.with( LEX_DOUBLE_QUOTE_CLOSING );
      }
      else if( LEX_SINGLE_QUOTE_OPENING.equals( curr ) ) {
        token = QUOTE_SINGLE_OPENING.with( LEX_SINGLE_QUOTE_OPENING );
      }
      else if( LEX_SINGLE_QUOTE_CLOSING.equals( curr ) ) {
        token = QUOTE_SINGLE_CLOSING.with( LEX_SINGLE_QUOTE_CLOSING );
      }
      else if( curr == '\\' ) {
        final var next = i.advance();

        if( next == '\'' ) {
          token = ESC_SINGLE;
        }
        else if( next == '\"' ) {
          token = ESC_DOUBLE;
        }
        else {
          // Push back the escaped character, which wasn't a straight quote.
          i.prev();
        }
      }
      else if( curr == '=' ) {
        token = EQUALS;
      }
      else if( curr == ',' && i.peek() == ',' ) {
        i.skip( next -> next == ',' );
        token = QUOTE_DOUBLE_OPENING.with( LEX_DOUBLE_QUOTE_OPENING_LOW );
      }
      else if( LEX_DOUBLE_QUOTE_OPENING_LOW.equals( curr ) ) {
        token = QUOTE_DOUBLE_OPENING.with( LEX_DOUBLE_QUOTE_OPENING_LOW );
      }
      else if( LEX_SINGLE_CHEVRON_LEFT.equals( curr ) ) {
        token = QUOTE_SINGLE_OPENING.with( LEX_SINGLE_CHEVRON_LEFT );
      }
      else if( LEX_DOUBLE_CHEVRON_LEFT.equals( curr ) ) {
        token = QUOTE_DOUBLE_OPENING.with( LEX_DOUBLE_CHEVRON_LEFT );
      }
      else if( LEX_SINGLE_CHEVRON_RIGHT.equals( curr ) ) {
        token = QUOTE_SINGLE_CLOSING.with( LEX_SINGLE_CHEVRON_RIGHT );
      }
      else if( LEX_DOUBLE_CHEVRON_RIGHT.equals( curr ) ) {
        token = QUOTE_DOUBLE_CLOSING.with( LEX_DOUBLE_CHEVRON_RIGHT );
      }
      else if( curr == DONE ) {
        continue;
      }

      assert index >= 0;
      assert curr != DONE;

      consumer.accept( new Lexeme( token, index, i.index() + 1 ) );
      i.next();
    }

    // Simulate an end of line and end of paragraph before the end of text.
    // This allows the parser to match against lexemes at the end of
    // the string to curl (without having to introduce more conditions).
    consumer.accept( new Lexeme( EOL, i.index(), i.index() ) );
    consumer.accept( new Lexeme( EOP, i.index(), i.index() ) );
    consumer.accept( Lexeme.EOT );
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

  private static boolean isDigit( final char curr ) {
    return Character.isDigit( curr ) ||
      "¼½¾⅐⅑⅒⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞".indexOf( curr ) >= 0;
  }

  /**
   * Answers whether the given character may be part of an en- or em-dash.
   * This must be called after it is known that the character isn't a lone
   * hyphen.
   *
   * @param curr The character to check as being a dash.
   * @return {@code true} if the given character is part of a dash.
   */
  private static boolean isDash( final char curr ) {
    return curr == '-' || curr == '–' || curr == '—' || curr == '―';
  }

  /**
   * Answers whether the given character can be considered part of a number
   * or not. This does not include digits, which are checked independently
   * of this method.
   *
   * @param curr The character to check as being related to a number.
   * @return {@code true} if the given character can be considered part of
   * a number (e.g., -2,000.2^2 is considered a single number).
   */
  private static boolean isNumeric( final char curr ) {
    return curr == '.' || curr == ',' || curr == '-' || curr == '+' ||
      curr == '^' || curr == '⅟' || curr == '⁄';
  }
}
