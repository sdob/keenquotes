/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    new LexemeType[]{SPACE, HYPHEN, QUOTE_DOUBLE, OPENING_GROUP, EOP};

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
    new LexemeType[]{SPACE, HYPHEN, QUOTE_DOUBLE, CLOSING_GROUP, EOL};

  /**
   * Double quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{SPACE, HYPHEN, QUOTE_SINGLE, OPENING_GROUP, EOP};

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
    new LexemeType[]{SPACE, HYPHEN, QUOTE_SINGLE, CLOSING_GROUP, EOL};

  /**
   * The text to parse. A reference is required as a minor optimization in
   * memory and speed: the lexer records integer offsets, rather than new
   * {@link String} instances, to track parsed lexemes.
   */
  private final String mText;

  /**
   * Converts a string into an iterable list of {@link Lexeme} instances.
   */
  private final Lexer mLexer;

  /**
   * Sets of contractions that help disambiguate single quotes in the text.
   * These are effectively immutable while parsing.
   */
  private final Contractions sContractions;

  /**
   * Incremented for each opening single quote emitted. Used to help resolve
   * ambiguities when single quote marks are balanced.
   */
  private int mOpeningSingleQuote;

  /**
   * Incremented for each closing single quote emitted. Used to help resolve
   * ambiguities when single quote marks are balanced.
   */
  private int mClosingSingleQuote;

  /**
   * Constructs a new {@link Parser} using the default contraction sets
   * to help resolve some ambiguous scenarios.
   *
   * @param text The prose to parse, containing zero or more quotation
   *             characters.
   */
  public Parser( final String text ) {
    this( text, new Contractions.Builder().build() );
  }

  /**
   * Constructs a new {@link Parser} using the default contraction sets
   * to help resolve some ambiguous scenarios.
   *
   * @param text         The prose to parse, containing zero or more quotation
   *                     characters.
   * @param contractions Custom sets of contractions to help resolve
   *                     ambiguities.
   */
  public Parser( final String text, final Contractions contractions ) {
    mText = text;
    mLexer = new Lexer( mText );
    sContractions = contractions;
  }

  /**
   * Iterates over the entire text provided at construction, emitting
   * {@link Token}s that can be used to convert straight quotes to curly
   * quotes.
   *
   * @param consumer Receives emitted {@link Token}s.
   */
  public void parse( final Consumer<Token> consumer ) {
    final var lexemes = new CircularFifoQueue<Lexeme>( 3 );

    // Allow consuming the very first token without checking the queue size.
    flush( lexemes );

    final var unresolved = new ArrayList<Lexeme[]>();
    Lexeme lexeme;

    // Create and convert a list of all unambiguous quote characters.
    while( (lexeme = mLexer.next()) != EOT ) {
      tokenize( lexeme, lexemes, consumer, unresolved );
    }

    // By loop's end, the lexemes list contains tokens for all except the
    // final two elements (from tokenizing in triplets). Tokenize the remaining
    // unprocessed lexemes.
    tokenize( EOT, lexemes, consumer, unresolved );
    tokenize( EOT, lexemes, consumer, unresolved );

    // Attempt to resolve any remaining unambiguous quotes.
    resolve( unresolved, consumer );

    System.out.println( "UNRESOLVED: " + unresolved.size() );
  }

  private void resolve(
    final List<Lexeme[]> lexemes, final Consumer<Token> consumer ) {
    // Some non-emitted tokenized lexemes may be ambiguous.
    final var ambiguousLeadingQuotes = new ArrayList<Lexeme>( 16 );
    final var ambiguousLaggingQuotes = new ArrayList<Lexeme>( 16 );
    var resolvedLeadingQuotes = 0;
    var resolvedLaggingQuotes = 0;

    // Count the number of ambiguous and non-ambiguous open single quotes.
    for( var i = lexemes.iterator(); i.hasNext(); ) {
      final var quotes = i.next();
      final var lex1 = quotes[ 0 ];
      final var lex2 = quotes[ 1 ];
      final var lex3 = quotes[ 2 ];

      if( lex2.isType( QUOTE_SINGLE ) ) {
        final var word1 = lex1 == SOT ? "" : lex1.toString( mText );
        final var word3 = lex3 == EOT ? "" : lex3.toString( mText );

        if( sContractions.beganAmbiguously( word3 ) ) {
          // E.g., 'Cause
          if( lex1.isType( QUOTE_SINGLE ) ) {
            // E.g., ''Cause
            consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
            i.remove();
          }
          else {
            // The contraction is uncertain until a closing quote is found that
            // may balance this single quote.
            ambiguousLeadingQuotes.add( lex2 );
          }
        }
        else if( sContractions.beganUnambiguously( word3 ) ) {
          // The quote mark forms a word that does not stand alone from its
          // contraction. For example, twas is not a word: it's 'twas.
          consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
          i.remove();
        }
        else if( sContractions.endedAmbiguously( word1 ) ) {
          ambiguousLaggingQuotes.add( lex2 );
        }
        else if( (lex1.isSot() || lex1.anyType( LEADING_QUOTE_OPENING_SINGLE ))
          && lex3.anyType( LAGGING_QUOTE_OPENING_SINGLE ) ) {
          consumer.accept( new Token( QUOTE_OPENING_SINGLE, lex2 ) );
          i.remove();
          resolvedLeadingQuotes++;
          mOpeningSingleQuote++;
        }
        else if( lex1.anyType( LEADING_QUOTE_CLOSING_SINGLE ) &&
          (lex3.isEot() || lex3.anyType( LAGGING_QUOTE_CLOSING_SINGLE )) ) {
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
          i.remove();
          resolvedLaggingQuotes++;
          mClosingSingleQuote++;
        }
        else if( lex3.isType( NUMBER ) ) {
          // E.g., '04
          ambiguousLeadingQuotes.add( lex2 );
        }
      }
    }

    final var ambiguousLeadingCount = ambiguousLeadingQuotes.size();
    final var ambiguousLaggingCount = ambiguousLaggingQuotes.size();

    if( resolvedLeadingQuotes == 1 && resolvedLaggingQuotes == 0 ) {
      if( ambiguousLeadingCount == 0 && ambiguousLaggingCount == 1 ) {
        final var balanced = mClosingSingleQuote - mOpeningSingleQuote == 0;
        final var quote = balanced ? QUOTE_APOSTROPHE : QUOTE_CLOSING_SINGLE;
        consumer.accept( new Token( quote, ambiguousLaggingQuotes.get( 0 ) ) );
      }
    }
    else if( ambiguousLeadingCount == 0 && ambiguousLaggingCount > 0 ) {
      // If there are no ambiguous leading quotes then all ambiguous lagging
      // quotes must be contractions.
      ambiguousLaggingQuotes.forEach(
        lex -> consumer.accept( new Token( QUOTE_APOSTROPHE, lex ) )
      );
    }
    else if( ambiguousLeadingCount == 0 ) {
      if( resolvedLaggingQuotes < resolvedLeadingQuotes ) {
        for( final var mark : lexemes ) {
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, mark[ 1 ] ) );
        }
      }
    }
    else if( ambiguousLeadingCount == 1 && resolvedLaggingQuotes == 1 ) {
      consumer.accept(
        new Token( QUOTE_OPENING_SINGLE, ambiguousLeadingQuotes.get( 0 ) )
      );
    }
  }

  private void tokenize( final Lexeme lexeme,
                         final CircularFifoQueue<Lexeme> lexemes,
                         final Consumer<Token> consumer,
                         final List<Lexeme[]> unresolved ) {
    // Add the next lexeme to tokenize into the queue for immediate processing.
    lexemes.add( lexeme );

    final var lex1 = lexemes.get( 0 );
    final var lex2 = lexemes.get( 1 );
    final var lex3 = lexemes.get( 2 );

    if( lex2.isType( QUOTE_SINGLE ) && lex3.isType( WORD ) &&
      lex1.anyType( WORD, PERIOD, NUMBER ) ) {
      // Examples: y'all, Ph.D.'ll, 20's, she's
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
      flush( lexemes );
    }
    else if( lex1.isType( QUOTE_SINGLE ) && lex3.isType( QUOTE_SINGLE ) &&
      "n".equalsIgnoreCase( lex2.toString( mText ) ) ) {
      // I.e., 'n'
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex3 ) );
      flush( lexemes );

      // Remove the first apostrophe so that it isn't emitted twice.
      unresolved.remove( unresolved.size() - 1 );
    }
    else if( lex2.isType( QUOTE_SINGLE ) && lex1.isType( NUMBER ) ) {
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
    else if( lex2.isType( QUOTE_DOUBLE ) && lex1.isType( NUMBER ) ) {
      // E.g., 2"
      consumer.accept( new Token( QUOTE_PRIME_DOUBLE, lex2 ) );
    }
    else if( lex2.isType( WORD ) && lex3.isType( QUOTE_SINGLE ) &&
      sContractions.endedUnambiguously( lex2.toString( mText ) ) ) {
      // E.g., thinkin'
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex3 ) );
    }
    else if( lex2.isType( NUMBER ) && lex1.isType( QUOTE_SINGLE ) &&
      lex3.isType( WORD ) &&
      lex3.toString( mText ).equalsIgnoreCase( "s" ) ) {
      // Sentences must re-written to avoid starting with numerals.
      // E.g., '70s
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
    }
    else if( lex2.isType( QUOTE_SINGLE ) && lex3.isType( NUMBER ) ) {
      // E.g., '02
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
    }
    else if( lex2.isType( QUOTE_SINGLE ) &&
      lex1.anyType( PUNCT, PERIOD, ELLIPSIS, HYPHEN ) &&
      (lex3.anyType( EOL, EOP ) || lex3.isEot()) ) {
      consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
    }
    else if( lex1.isType( ESC_SINGLE ) ) {
      // E.g., \'
      consumer.accept( new Token( QUOTE_STRAIGHT_SINGLE, lex1 ) );
    }
    else if( lex1.isType( ESC_DOUBLE ) ) {
      // E.g., \"
      consumer.accept( new Token( QUOTE_STRAIGHT_DOUBLE, lex1 ) );

      if( lex2.isType( QUOTE_SINGLE ) &&
        (lex3.isEot() || lex3.anyType( SPACE, HYPHEN, EOL, EOP )) ) {
        consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
        mClosingSingleQuote++;
      }
    }
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      (lex1.isSot() || lex1.anyType( LEADING_QUOTE_OPENING_DOUBLE )) &&
      lex3.anyType( LAGGING_QUOTE_OPENING_DOUBLE ) ) {
      // Examples: "", "..., "word, ---"word
      consumer.accept( new Token( QUOTE_OPENING_DOUBLE, lex2 ) );
    }
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      lex1.anyType( LEADING_QUOTE_CLOSING_DOUBLE ) &&
      (lex3.isEot() || lex3.anyType( LAGGING_QUOTE_CLOSING_DOUBLE )) ) {
      // E.g., ..."', word"', ?"'
      consumer.accept( new Token( QUOTE_CLOSING_DOUBLE, lex2 ) );
    }
    else if( lex1.isType( QUOTE_SINGLE ) &&
      lex2.anyType( PUNCT, PERIOD ) && lex3.isType( QUOTE_DOUBLE ) ) {
      // E.g., '," (contraction ruled out from previous conditionals)
      consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex1 ) );
      mClosingSingleQuote++;
    }
    else if( lex2.anyType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      // After tokenizing, the parser will attempt to resolve ambiguities.
      unresolved.add( new Lexeme[]{lex1, lex2, lex3} );
    }
  }

  /**
   * Overwrites the {@link CircularFifoQueue}'s contents with start-of-text
   * indicators.
   *
   * @param lexemes The queue to overflow.
   */
  private void flush( final CircularFifoQueue<Lexeme> lexemes ) {
    lexemes.add( SOT );
    lexemes.add( SOT );
    lexemes.add( SOT );
  }
}
