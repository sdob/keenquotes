/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.keenwrite.quotes.Contractions.*;
import static com.keenwrite.quotes.Lexeme.EOT;
import static com.keenwrite.quotes.Lexeme.SOT;
import static com.keenwrite.quotes.LexemeType.*;
import static com.keenwrite.quotes.TokenType.*;

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
 *   <li>Double prime (NUMBER ") -- 2"</li>
 *   <li>Double prime (NUMBER '') -- 2''</li>
 * </ol>
 * Next, handle balanced double quotes:
 * <ol>
 *   <li>Double quotes (" (WORD (SPACE+ WORD)? (PUNCT | PERIOD))+ ")</li>
 *   <li>Single quotes (' (WORD (SPACE+ WORD)? (PUNCT | PERIOD))+ ')</li>
 * </ol>
 */
public class Parser {
  /**
   * Single quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_SINGLE =
    new LexemeType[]{SPACE, HYPHEN, QUOTE_DOUBLE};

  /**
   * Single quotes succeeded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_OPENING_SINGLE =
    new LexemeType[]{WORD, ELLIPSIS, QUOTE_SINGLE, QUOTE_DOUBLE};

  /**
   * Single quotes preceded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_CLOSING_SINGLE =
    new LexemeType[]{WORD, NUMBER, PERIOD, PUNCT, ELLIPSIS, QUOTE_DOUBLE};

  /**
   * Single quotes succeeded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_CLOSING_SINGLE =
    new LexemeType[]{SPACE, HYPHEN, QUOTE_DOUBLE, EOL};

  /**
   * Double quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{SPACE, HYPHEN, QUOTE_SINGLE};

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{WORD, NUMBER, ELLIPSIS, QUOTE_SINGLE, QUOTE_DOUBLE};

  /**
   * Double quotes preceded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{WORD, NUMBER, PERIOD, PUNCT, ELLIPSIS, QUOTE_SINGLE};

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{SPACE, HYPHEN, QUOTE_SINGLE, EOL};

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
      tokenize( lexeme, consumer );
    }

    // By loop's end, the lexemes list contains tokens for all except the
    // final two elements (from tokenizing in triplets). Tokenize the remaining
    // unprocessed lexemes.
    tokenize( EOT, consumer );
    tokenize( EOT, consumer );

    // Some non-emitted tokenized lexemes may be ambiguous.
    final var ambiguousLeadingQuotes = new ArrayList<Lexeme>( 16 );
    final var ambiguousLaggingQuotes = new ArrayList<Lexeme>( 16 );
    var resolvedLeadingQuotes = 0;
    var resolvedLaggingQuotes = 0;

    // Count the number of ambiguous and non-ambiguous open single quotes.
    for( var i = mQuotationMarks.iterator(); i.hasNext(); ) {
      final var quotes = i.next();
      final var lex1 = quotes[ 0 ];
      final var lex2 = quotes[ 1 ];
      final var lex3 = quotes[ 2 ];

      if( lex2.isType( QUOTE_SINGLE ) ) {
        final var word1 = lex1 == SOT ? "" : lex1.toString( mText );
        final var word3 = lex3 == EOT ? "" : lex3.toString( mText );

        if( contractionBeganAmbiguously( word3 ) ) {
          // E.g., ''Cause
          if( lex1.isType( QUOTE_SINGLE ) ) {
            consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
            i.remove();
          }
          else {
            // The contraction is uncertain until a closing quote is found that
            // balances this single quote.
            ambiguousLeadingQuotes.add( lex2 );
          }
        }
        else if( contractionBeganUnambiguously( word3 ) ) {
          // The quote mark forms a word that does not stand alone from its
          // contraction. For example, twas is not a word: it's 'twas.
          consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
          i.remove();
        }
        else if( contractionEndedAmbiguously( word1 ) ) {
          ambiguousLaggingQuotes.add( lex2 );
        }
        else if( contractionEndedUnambiguously( word1 ) ) {
          consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
          i.remove();
        }
        else if( (lex1.isSot() || lex1.anyType( LEADING_QUOTE_OPENING_SINGLE ))
          && lex3.anyType( LAGGING_QUOTE_OPENING_SINGLE ) ) {
          resolvedLeadingQuotes++;
          consumer.accept( new Token( QUOTE_OPENING_SINGLE, lex2 ) );
          i.remove();
        }
        else if( lex1.anyType( LEADING_QUOTE_CLOSING_SINGLE ) &&
          (lex3.isEot() || lex3.anyType( LAGGING_QUOTE_CLOSING_SINGLE )) ) {
          resolvedLaggingQuotes++;
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
          i.remove();
        }
        else if( lex3.isType( NUMBER ) ) {
          // E.g., '04
          ambiguousLeadingQuotes.add( lex2 );
        }
      }
    }

    System.out.println( "ambig leading: " + ambiguousLeadingQuotes.size() );
    System.out.println( "ambig lagging: " + ambiguousLaggingQuotes.size() );
    System.out.println( "unambig leading: " + resolvedLeadingQuotes );
    System.out.println( "unambig lagging: " + resolvedLaggingQuotes );

    if( resolvedLeadingQuotes == 1 && resolvedLaggingQuotes == 0 ) {
      if( ambiguousLeadingQuotes.size() == 0 && ambiguousLaggingQuotes.size() == 1 ) {
        consumer.accept(
          new Token( QUOTE_CLOSING_SINGLE, ambiguousLaggingQuotes.get( 0 ) )
        );
      }
    }
    else if( ambiguousLeadingQuotes.size() > 0 && ambiguousLaggingQuotes.size() == 0 ) {
      // If there are no ambiguous lagging quotes then all ambiguous leading
      // quotes must be contractions.
      ambiguousLeadingQuotes.forEach(
        lex -> consumer.accept( new Token( QUOTE_APOSTROPHE, lex ) )
      );
    }
    else if( ambiguousLeadingQuotes.size() == 0 && ambiguousLaggingQuotes.size() > 0 ) {
      // If there are no ambiguous leading quotes then all ambiguous lagging
      // quotes must be contractions.
      ambiguousLaggingQuotes.forEach(
        lex -> consumer.accept( new Token( QUOTE_APOSTROPHE, lex ) )
      );
    }
    else if( resolvedLeadingQuotes == 1 && ambiguousLaggingQuotes.size() == 1 ) {
      consumer.accept(
        new Token( QUOTE_CLOSING_SINGLE, ambiguousLaggingQuotes.get( 0 ) )
      );
    }
  }

  private void tokenize( final Lexeme lexeme, final Consumer<Token> consumer ) {
    mLexemes.add( lexeme );
    tokenize( consumer );
  }

  private void tokenize( final Consumer<Token> consumer ) {
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
      contractionBeganUnambiguously( lex2.toString( mText ) ) ) {
      // E.g., 'twas
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
    }
    else if( lex1.isType( QUOTE_SINGLE ) && lex2.isType( NUMBER ) &&
      lex3.isType( WORD ) &&
      lex3.toString( mText ).equalsIgnoreCase( "s" ) ) {
      // E.g., '70s
      // Sentences are re-written to avoid starting with numerals.
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
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      (lex1.isSot() || lex1.anyType( LEADING_QUOTE_OPENING_DOUBLE )) &&
      lex3.anyType( LAGGING_QUOTE_OPENING_DOUBLE ) ) {
      // Examples: "'Twas, "", "...
      consumer.accept( new Token( QUOTE_OPENING_DOUBLE, lex2 ) );
    }
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      lex1.anyType( LEADING_QUOTE_CLOSING_DOUBLE ) &&
      (lex3.isEot() || lex3.anyType( LAGGING_QUOTE_CLOSING_DOUBLE )) ) {
      consumer.accept( new Token( QUOTE_CLOSING_DOUBLE, lex2 ) );
    }
    else if( lex2.anyType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      mQuotationMarks.add( new Lexeme[]{lex1, lex2, lex3} );
    }
  }

  private void flush( final CircularFifoQueue<Lexeme> lexemes ) {
    lexemes.add( SOT );
    lexemes.add( SOT );
    lexemes.add( SOT );
  }
}
