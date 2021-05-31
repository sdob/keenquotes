/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import static com.keenwrite.quotes.TokenType.*;

/**
 * Converts straight double/single quotes and apostrophes to curly equivalents.
 * First, handle single quotes as apostrophes, which include:
 * <ol>
 *   <li>Inner contractions (WORD ' WORD) -- you'd've</li>
 *   <li>Inner contractions (PERIOD ' WORD) -- Ph.d.'ll</li>
 *   <li>Numeric contractions (NUMBER ' WORD) -- 70's</li>
 *   <li>Outer contractions (' WORD ') -- 'n'</li>
 *   <li>Unambiguous beginning contractions (' WORD) -- 'Twas</li>
 * </ol>
 * Next, handle single and double quotes as primes and double primes:
 * <ol>
 *   <li>Single prime (NUMBER ') -- 2'</li>
 *   <li>Double prime (NUMBER ") -- 7.5"</li>
 * </ol>
 * Next, handle balanced double quotes:
 * <ol>
 *   <li>Double quotes (" (WORD (SPACE+ WORD)? (PUNCT | PERIOD))+ ")</li>
 *   <li>Single quotes (' (WORD (SPACE+ WORD)? (PUNCT | PERIOD))+ ')</li>
 * </ol>
 */
public class Parser implements Consumer<Token> {
  private final String mText;
  private final CircularFifoQueue<Token> mTokens = new CircularFifoQueue<>( 3 );

  private final Deque<Token> mStack = new ArrayDeque<>();

  public Parser( final String text ) {
    mText = text;
    mTokens.add( Token.EOT );
    mTokens.add( Token.EOT );
    mTokens.add( Token.EOT );
  }

  public void parse() {
    final var tokenizer = new Tokenizer();
    tokenizer.tokenize( mText, this );
  }

  @Override
  public void accept( final Token token ) {
    mTokens.add( token );

    final var token1 = mTokens.get( 0 );
    final var token2 = mTokens.get( 1 );
    final var token3 = mTokens.get( 2 );

    if( token2.isType( QUOTE_SINGLE ) &&
      token3.isType( WORD ) &&
      token1.anyType( WORD, PERIOD, NUMBER ) ) {
      System.out.println( "APOSTROPHE: " + token2 );
    }
    else if( token1.isType( QUOTE_SINGLE ) &&
      "n".equalsIgnoreCase( token2.toString( mText ) ) &&
      token3.isType( QUOTE_SINGLE ) ) {
      System.out.printf( "APOSTROPHES: %s %s%n", token1, token3 );
    }
    else if( token1.isType( NUMBER ) && token2.isType( QUOTE_SINGLE ) ) {
      System.out.println( "PRIME: " + token2 );
    }
    else if( token1.isType( NUMBER ) && token2.isType( QUOTE_DOUBLE ) ) {
      System.out.println( "DOUBLE PRIME: " + token2 );
    }
    else if( token.anyType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      mStack.push( token );

      if( mStack.isEmpty() ) {
        System.out.println( "EMPTY STACK?!" );
      }
    }
  }
}

/*
  private enum TokenType {
    QUOTE_OPENING_SINGLE,
    QUOTE_OPENING_DOUBLE,
    QUOTE_CLOSING_SINGLE,
    QUOTE_CLOSING_DOUBLE,
    QUOTE_APOSTROPHE,
    QUOTE_PRIME_SINGLE,
    QUOTE_PRIME_DOUBLE,
    TEXT
  }
*/
