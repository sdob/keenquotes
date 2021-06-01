/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import static com.keenwrite.quotes.Contractions.beginsUnambiguously;
import static com.keenwrite.quotes.LexemeType.*;
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
public class Parser implements Consumer<Lexeme> {
  private final String mText;
  private final CircularFifoQueue<Lexeme> mTokens =
    new CircularFifoQueue<>( 3 );

  private final Deque<Lexeme> mStack = new ArrayDeque<>();
  private final Consumer<Token> mConsumer;

  public Parser( final String text, final Consumer<Token> consumer ) {
    mText = text;

    // Allow consuming the very first token without checking the queue size.
    mTokens.add( Lexeme.EOT );
    mTokens.add( Lexeme.EOT );
    mTokens.add( Lexeme.EOT );

    mConsumer = consumer;
  }

  public void parse() {
    final var tokenizer = new Lexer();
    tokenizer.parse( mText, this );
    System.out.println(" DUH DONE!" );
  }

  @Override
  public void accept( final Lexeme token ) {
    mTokens.add( token );

    final var token1 = mTokens.get( 0 );
    final var token2 = mTokens.get( 1 );
    final var token3 = mTokens.get( 2 );

    if( token2.isType( QUOTE_SINGLE ) && token3.isType( WORD ) &&
      token1.anyType( WORD, PERIOD, NUMBER ) ) {
      mConsumer.accept( new Token( QUOTE_APOSTROPHE, token2 ) );
    }
    else if( token1.isType( QUOTE_SINGLE ) && token3.isType( QUOTE_SINGLE ) &&
      "n".equalsIgnoreCase( token2.toString( mText ) ) ) {
      mConsumer.accept( new Token( QUOTE_APOSTROPHE, token1 ) );
      mConsumer.accept( new Token( QUOTE_APOSTROPHE, token3 ) );
    }
    else if( token1.isType( NUMBER ) && token2.isType( QUOTE_SINGLE ) ) {
      mConsumer.accept( new Token( QUOTE_PRIME_SINGLE, token2 ) );
    }
    else if( token1.isType( NUMBER ) && token2.isType( QUOTE_DOUBLE ) ) {
      mConsumer.accept( new Token( QUOTE_PRIME_DOUBLE, token2 ) );
    }
    else if( token1.isType( QUOTE_SINGLE ) && token2.isType( WORD ) &&
      beginsUnambiguously( token2.toString( mText ) ) ) {
      mConsumer.accept( new Token( QUOTE_APOSTROPHE, token1 ) );
    }
    else if( token.anyType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      mStack.push( token );

      if( mStack.isEmpty() ) {
        System.out.println( "EMPTY STACK?!" );
      }
    }
  }
}
