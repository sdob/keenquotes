/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.function.Consumer;

import static com.keenwrite.quotes.Lexeme.createLexeme;
import static com.keenwrite.quotes.LexemeType.*;
import static java.lang.Character.*;
import static java.text.CharacterIterator.DONE;

/**
 * Turns text into words, numbers, punctuation, spaces, and more.
 */
public class Lexer {
  /**
   * Default constructor, no state.
   */
  public Lexer() {
  }

  /**
   * Emits a series of tokens that represent information about text that is
   * needed to convert straight quotes to curly quotes.
   *
   * @param text     The text to split into tokens.
   * @param consumer Receives each token as a separate event.
   */
  public void parse( final String text, final Consumer<Lexeme> consumer ) {
    final var iterator = new StringCharacterIterator( text );
    Lexeme lex;

    while( (lex = parse( iterator )).hasNext() ) {
      consumer.accept( lex );
    }
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

        if( !isLetterOrDigit( peek( i ) ) ) {
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
      else if( isDigit( curr ) || isNumeric( curr ) && isDigit( peek( i ) ) ) {
        // Tokenize all consecutive number characters at prevent the main
        // loop from switching back to word tokens.
        char next;

        do {
          next = i.next();
        }
        while( isDigit( next ) || isNumeric( next ) && isDigit( peek( i ) ) );

        // The loop above will overshoot the number by one character.
        i.previous();

        lexeme = createLexeme( isWord ? WORD : NUMBER, began, i.getIndex() );
      }
      else if( curr == '.' ) {
        lexeme = createLexeme( PERIOD, began, i.getIndex() );
      }
      else if( curr == '\r' ) {
        lexeme = createLexeme( NEWLINE, began, i.getIndex() );

        // Swallow the LF in CRLF; peeking won't work here.
        if( i.next() != '\n' ) {
          // Push back the non-LF char.
          i.previous();
        }
      }
      else if( curr == '\n' ) {
        lexeme = createLexeme( NEWLINE, began, i.getIndex() );
      }
      else if( isWhitespace( curr ) ) {
        lexeme = createLexeme( SPACE, began, i.getIndex() );
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

  private static boolean isNumeric( final char curr ) {
    return curr == '.' || curr == ',';
  }

  private static char peek( final CharacterIterator ci ) {
    final var ch = ci.next();
    ci.previous();
    return ch;
  }
}
