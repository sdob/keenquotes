/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.keenwrite.quotes.Contractions.beginsUnambiguously;
import static com.keenwrite.quotes.Lexeme.EOT;
import static com.keenwrite.quotes.LexemeType.*;
import static com.keenwrite.quotes.TokenType.*;
import static java.util.Comparator.comparingInt;

/**
 * Converts straight double/single quotes and apostrophes to curly equivalents.
 * First, handle single quotes as apostrophes, which include:
 * <ol>
 *   <li>Escaped single quote (BACKSLASH ' ) -- \'</li>
 *   <li>Escaped double quote (BACKSLASH " ) -- \"</li>
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
public class Parser {
  /**
   * The text to parse. A reference is required as a minor optimization in
   * memory and speed: the lexer records integer offsets, rather than new
   * {@link String} instances, to track parsed lexemes.
   */
  private final String mText;

  private final Lexer mLexer;

  private final List<Lexeme[]> mQuotationMarks = new ArrayList<>();
  private final CircularFifoQueue<Lexeme> mLexemes =
    new CircularFifoQueue<>( 3 );

  public Parser( final String text ) {
    mText = text;
    mLexer = new Lexer( mText );

    // Allow consuming the very first token without checking the queue size.
    flush( mLexemes );
  }

  /**
   * Iterates over the entire text provided at construction, emitting
   * {@link Token}s that can be used to convert straight quotes to curly
   * quotes.
   *
   * @param consumer Receives emitted {@link Token}s.
   */
  public void parse( final Consumer<Token> consumer ) {
    // Create/convert a list of all unambiguous quote characters.
    Lexeme lexeme;

    while( (lexeme = mLexer.next()) != EOT ) {
      parse( lexeme, consumer );
    }

    // Parse the remaining lexemes because the EOT lexeme will terminate the
    // loop above without having examined the last lexemes.
    for( int i = 0; i < mLexemes.size(); i++ ) {
      parse( mLexemes.get( i ), consumer );
    }

    mQuotationMarks.sort( comparingInt( lexemes -> lexemes[ 0 ].began() ) );

    for( final var unparsed : mQuotationMarks ) {
      System.out.println( "unparsed: " + unparsed[ 0 ] + " " + unparsed[ 1 ] + " " + unparsed[ 2 ] );
    }

    // Create/convert a list of all unambiguous quotations.
    // Let TERM ::= (, | ; | ! | ? | .)
    // Find unambiguous quotations by searching for:
    //   ' WORD ('* SPACE+ WORD)* TERM '
    // In other words, when a ' WORD is encountered, push the ' onto a stack.
    // If ' WORD is encountered, pop the stack and push the new ' onto it.
    // If TERM ' is encountered, push the new ' onto it.
    // This algorithm may have to push " WORD and " TERM as well, to account
    // for nested sentences.

    // Convert remaining single quotes to apostrophes.
  }

  private void parse( final Lexeme lexeme, final Consumer<Token> consumer ) {
    mLexemes.add( lexeme );

    final var lex1 = mLexemes.get( 0 );
    final var lex2 = mLexemes.get( 1 );
    final var lex3 = mLexemes.get( 2 );

    if( lex2.isType( QUOTE_SINGLE ) && lex3.isType( WORD ) &&
      lex1.anyType( WORD, PERIOD, NUMBER ) ) {
      // Examples: y'all, Ph.D.'ll, 20's
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
      flush( mLexemes );
    }
    else if( lex1.isType( QUOTE_SINGLE ) && lex3.isType( QUOTE_SINGLE ) &&
      "n".equalsIgnoreCase( lex2.toString( mText ) ) ) {
      // I.e., 'n'
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex3 ) );
    }
    else if( lex1.isType( NUMBER ) && lex2.isType( QUOTE_SINGLE ) ) {
      if( lex3.isType( QUOTE_SINGLE ) ) {
        // E.g., 2''
        consumer.accept(
          new Token( QUOTE_PRIME_DOUBLE, lex2.began(), lex3.ended() ) );
      }
      else {
        // E.g., 2'
        consumer.accept( new Token( QUOTE_PRIME_SINGLE, lex2 ) );
      }
    }
    else if( lex1.isType( NUMBER ) && lex2.isType( QUOTE_DOUBLE ) ) {
      // E.g., 2"
      consumer.accept( new Token( QUOTE_PRIME_DOUBLE, lex2 ) );
    }
    else if( lex1.isType( QUOTE_SINGLE ) && lex2.isType( WORD ) &&
      // E.g., 'twas
      beginsUnambiguously( lex2.toString( mText ) ) ) {
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
    }
    else if( lex1.isType( ESC_SINGLE ) ) {
      // E.g., \'
      consumer.accept( new Token( QUOTE_STRAIGHT_SINGLE, lex1 ) );
    }
    else if( lex1.isType( ESC_DOUBLE ) ) {
      // E.g., \"
      consumer.accept( new Token( QUOTE_STRAIGHT_DOUBLE, lex1 ) );
    }
    else if( lex2.anyType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      mQuotationMarks.add( new Lexeme[]{lex1, lex2, lex3} );
    }
  }

  private void flush( final CircularFifoQueue<Lexeme> lexemes ) {
    lexemes.add( Lexeme.SOT );
    lexemes.add( Lexeme.SOT );
    lexemes.add( Lexeme.SOT );
  }
}
