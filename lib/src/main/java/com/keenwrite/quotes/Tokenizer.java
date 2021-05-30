/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.function.Consumer;

import static com.keenwrite.quotes.Token.createToken;
import static com.keenwrite.quotes.TokenType.*;
import static java.lang.Character.*;
import static java.text.CharacterIterator.DONE;

/**
 * Tokenizes text into words, numbers, punctuation, spaces, and more.
 */
public class Tokenizer {
  /**
   * Default constructor, no state.
   */
  public Tokenizer() {
  }

  /**
   * Emits a series of tokens that
   *
   * @param text     The text to split into tokens.
   * @param consumer Receives each token as a separate event.
   */
  public void tokenize( final String text, final Consumer<Token> consumer ) {
    final var iterator = new StringCharacterIterator( text );
    Token token;

    while( (token = tokenize( iterator )).hasNext() ) {
      consumer.accept( token );
    }
  }

  private Token tokenize( final CharacterIterator i ) {
    int began = i.getIndex();
    boolean isWord = false;
    Token token = null;

    do {
      final var curr = i.current();

      if( curr == DONE ) {
        return Token.EOT;
      }

      if( isLetter( curr ) ) {
        isWord = true;

        if( !isLetterOrDigit( peek( i ) ) ) {
          token = createToken( WORD, began, i.getIndex() );
        }
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

        token = createToken( isWord ? WORD : NUMBER, began, i.getIndex() );
      }
      else if( curr == '\r' ) {
        token = createToken( NEWLINE, began, i.getIndex() );

        // Swallow the LF in CRLF; peeking won't work here.
        if( i.next() != '\n' ) {
          // Push back the non-LF char.
          i.previous();
        }
      }
      else if( curr == '\n' ) {
        token = createToken( NEWLINE, began, i.getIndex() );
      }
      else if( isWhitespace( curr ) ) {
        token = createToken( SPACE, began, i.getIndex() );
      }
      else if( curr == '\'' ) {
        token = createToken( QSINGLE, began, i.getIndex() );
      }
      else if( curr == '"' ) {
        token = createToken( QDOUBLE, began, i.getIndex() );
      }
      else if( curr == '.' ) {
        token = createToken( PERIOD, began, i.getIndex() );
      }
      else {
        token = createToken( PUNCT, began, i.getIndex() );
      }

      i.next();
    }
    while( token == null );

    return token;
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
