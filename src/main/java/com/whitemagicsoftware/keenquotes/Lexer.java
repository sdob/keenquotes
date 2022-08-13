/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.LexemeType.*;
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
    final Consumer<FastCharacterIterator> filter ) {
    lex( new FastCharacterIterator( text ), emitter, filter );
  }

  private static void lex(
    final FastCharacterIterator i,
    final Consumer<Lexeme> emitter,
    final Consumer<FastCharacterIterator> filter ) {
    var index = i.index();
    var length = i.length();
    var curr = ' ';

    while( index < length ) {
      // Allow filters to skip character sequences (such as XML tags).
      filter.accept( i );

      index = i.index();
      curr = i.current();

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
        final var start = i.index();

        i.skip( next -> next == '.' || next == ' ' && i.peek() == '.' );

        token = i.index() - start == 0 ? PERIOD : ELLIPSIS;
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
      else if( curr == '“' ) {
        token = QUOTE_DOUBLE_OPENING;
      }
      else if( curr == '”' ) {
        token = QUOTE_DOUBLE_CLOSING;
      }
      else if( curr == '‘' ) {
        token = QUOTE_SINGLE_OPENING;
      }
      else if( curr == '’' ) {
        token = QUOTE_SINGLE_CLOSING;
      }
      else if( curr == '\\' ) {
        i.next();
        final var next = i.current();

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
      else if( curr == DONE ) {
        continue;
      }

      assert index >= 0;
      assert curr != DONE;

      emitter.accept( new Lexeme( token, index, i.index() + 1 ) );
      i.next();
      index = i.index();
    }
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
      "¼½¾⅐⅑⅒⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞".indexOf( curr ) > -1;
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
    return
      curr == '.' || curr == ',' || curr == '-' || curr == '+' ||
        curr == '^' || curr == '⅟' || curr == '⁄';
  }
}
