/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.Lexeme.EOT;
import static com.whitemagicsoftware.keenquotes.Lexeme.SOT;
import static com.whitemagicsoftware.keenquotes.LexemeType.*;
import static com.whitemagicsoftware.keenquotes.TokenType.*;
import static java.lang.Math.abs;
import static java.util.Collections.sort;

/**
 * Converts straight double/single quotes and apostrophes to curly equivalents.
 */
public class Parser {
  /**
   * Single quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_SINGLE =
    new LexemeType[]{SPACE, DASH, QUOTE_DOUBLE, OPENING_GROUP, EOL, EOP};

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
    new LexemeType[]{SPACE, HYPHEN, DASH,
      QUOTE_DOUBLE, CLOSING_GROUP, EOL, EOP};

  /**
   * Double quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{SPACE, DASH, EQUALS, QUOTE_SINGLE, OPENING_GROUP, EOL,
      EOP};

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{WORD, NUMBER, DASH, ELLIPSIS, OPENING_GROUP,
      QUOTE_SINGLE, QUOTE_SINGLE_OPENING, QUOTE_SINGLE_CLOSING, QUOTE_DOUBLE};

  /**
   * Double quotes preceded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{WORD, NUMBER, PERIOD, PUNCT, DASH, ELLIPSIS, CLOSING_GROUP,
      QUOTE_SINGLE, QUOTE_SINGLE_CLOSING, QUOTE_SINGLE_OPENING};

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{SPACE, PUNCT, PERIOD, EQUALS, HYPHEN, DASH,
      QUOTE_SINGLE, CLOSING_GROUP, EOL, EOP};

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
   * Contains each emitted opening single quote per paragraph.
   */
  private final List<Lexeme> mOpeningSingleQuotes = new ArrayList<>();

  /**
   * Contains each emitted closing single quote per paragraph.
   */
  private final List<Lexeme> mClosingSingleQuotes = new ArrayList<>();

  /**
   * Contains each emitted opening double quote per paragraph.
   */
  private final List<Lexeme> mOpeningDoubleQuotes = new ArrayList<>();

  /**
   * Contains each emitted closing double quote per paragraph.
   */
  private final List<Lexeme> mClosingDoubleQuotes = new ArrayList<>();

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
    mLexer = createLexer( mText );
    sContractions = contractions;
  }

  /**
   * Iterates over the entire text provided at construction, emitting
   * {@link Token}s that can be used to convert straight quotes to curly
   * quotes.
   *
   * @param tokenConsumer Receives emitted {@link Token}s.
   */
  public void parse(
    final Consumer<Token> tokenConsumer,
    final Consumer<Lexeme> lexemeConsumer ) {
    final var lexemes = new CircularFifoQueue<Lexeme>( 3 );

    // Allow consuming the very first token without needing a queue size check.
    flush( lexemes );

    final var unresolved = new ArrayList<Lexeme[]>();
    Lexeme lexeme;

    // Create and convert a list of all unambiguous quote characters.
    while( (lexeme = mLexer.next()) != EOT ) {
      // Reset after tokenizing a paragraph.
      if( tokenize( lexeme, lexemes, tokenConsumer, unresolved ) ) {
        // Attempt to resolve any remaining unambiguous quotes.
        resolve( unresolved, tokenConsumer );

        // Notify of any unambiguous quotes that could not be resolved.
        unresolved.forEach( lex -> lexemeConsumer.accept( lex[ 1 ] ) );
        unresolved.clear();
        mOpeningSingleQuotes.clear();
        mClosingSingleQuotes.clear();
        mOpeningDoubleQuotes.clear();
        mClosingDoubleQuotes.clear();
      }
    }

    // By loop's end, the lexemes list contains tokens for all except the
    // final two elements (from tokenizing in triplets). Tokenize the remaining
    // unprocessed lexemes.
    tokenize( EOT, lexemes, tokenConsumer, unresolved );
    tokenize( EOT, lexemes, tokenConsumer, unresolved );

    // Attempt to resolve any remaining unambiguous quotes.
    resolve( unresolved, tokenConsumer );

    // Notify of any unambiguous quotes that could not be resolved.
    unresolved.forEach( lex -> lexemeConsumer.accept( lex[ 1 ] ) );
  }

  /**
   * Converts {@link Lexeme}s identified as straight quotes into {@link Token}s
   * that represent the curly equivalent. The {@link Token}s are passed to
   * the given {@link Consumer} for further processing (e.g., replaced in
   * the original text being parsed).
   *
   * @param lexeme     A part of the text being parsed.
   * @param lexemes    A 3-element queue of lexemes that provide sufficient
   *                   context to identify curly quotes.
   * @param consumer   Recipient of equivalent quotes.
   * @param unresolved Rolling list of potentially ambiguous {@link Lexeme}s
   *                   that could not be tokenized, yet.
   * @return {@code true} if an end-of-paragraph is detected.
   */
  private boolean tokenize( final Lexeme lexeme,
                            final CircularFifoQueue<Lexeme> lexemes,
                            final Consumer<Token> consumer,
                            final List<Lexeme[]> unresolved ) {
    // Add the next lexeme to tokenize into the queue for immediate processing.
    lexemes.add( lexeme );

    final var lex1 = lexemes.get( 0 );
    final var lex2 = lexemes.get( 1 );
    final var lex3 = lexemes.get( 2 );

    if( lex2.isType( QUOTE_SINGLE ) && lex3.isType( WORD ) &&
      lex1.isType( WORD, PERIOD, NUMBER ) ) {
      // Examples: y'all, Ph.D.'ll, 20's, she's
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
    }
    else if( lex1.isType( QUOTE_SINGLE ) && lex3.isType( QUOTE_SINGLE ) &&
      "n".equalsIgnoreCase( lex2.toString( mText ) ) ) {
      // I.e., 'n'
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
      consumer.accept( new Token( QUOTE_APOSTROPHE, lex3 ) );
      flush( lexemes );
      truncate( unresolved );
    }
    else if( lex2.isType( QUOTE_SINGLE ) && lex1.isType( NUMBER ) ) {
      if( lex3.isType( QUOTE_SINGLE ) ) {
        // E.g., 2''
        consumer.accept(
          new Token( QUOTE_PRIME_DOUBLE, lex2.began(), lex3.ended() ) );
        flush( lexemes );
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
      flush( lexemes );
    }
    else if( lex2.isType( NUMBER ) && lex1.isType( QUOTE_SINGLE ) ) {
      // Sentences must re-written to avoid starting with numerals.
      if( lex3.isType( SPACE, PUNCT ) || lex3.isType( WORD ) &&
        lex3.toString( mText ).equalsIgnoreCase( "s" ) ) {
        // Examples: '20s, '02
        consumer.accept( new Token( QUOTE_APOSTROPHE, lex1 ) );
      }
      else {
        // E.g., '2''
        consumer.accept( new Token( QUOTE_OPENING_SINGLE, lex1 ) );
        mOpeningSingleQuotes.add( lex1 );
      }

      truncate( unresolved );
    }
    else if( lex2.isType( QUOTE_SINGLE ) &&
      lex1.isType( PUNCT, PERIOD, ELLIPSIS, DASH ) &&
      (lex3.isType( EOL, EOP ) || lex3.isEot()) ) {
      consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
      mClosingSingleQuotes.add( lex2 );
    }
    else if( lex1.isType( ESC_SINGLE ) ) {
      // E.g., \'
      consumer.accept( new Token( QUOTE_STRAIGHT_SINGLE, lex1 ) );
    }
    else if( lex1.isType( ESC_DOUBLE ) ) {
      // E.g., \"
      consumer.accept( new Token( QUOTE_STRAIGHT_DOUBLE, lex1 ) );

      if( lex2.isType( QUOTE_SINGLE ) &&
        (lex3.isEot() || lex3.isType( SPACE, DASH, EOL, EOP )) ) {
        consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
        mClosingSingleQuotes.add( lex2 );
      }
    }
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      (lex1.isSot() || lex1.isType( LEADING_QUOTE_OPENING_DOUBLE )) &&
      lex3.isType( LAGGING_QUOTE_OPENING_DOUBLE ) ) {
      // Examples: "", "..., "word, ---"word
      consumer.accept( new Token( QUOTE_OPENING_DOUBLE, lex2 ) );
      mOpeningDoubleQuotes.add( lex2 );
    }
    else if( lex2.isType( QUOTE_DOUBLE ) &&
      lex1.isType( LEADING_QUOTE_CLOSING_DOUBLE ) &&
      (lex3.isEot() || lex3.isType( LAGGING_QUOTE_CLOSING_DOUBLE )) ) {
      // Examples: ..."', word"', ?"', word"?
      consumer.accept( new Token( QUOTE_CLOSING_DOUBLE, lex2 ) );
      mClosingDoubleQuotes.add( lex2 );
    }
    else if( lex1.isType( WORD ) && lex2.isType( QUOTE_SINGLE ) &&
      lex3.isType( PUNCT, PERIOD ) ) {
      // E.g., word', (contraction ruled out by previous conditions)
      consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
      mClosingSingleQuotes.add( lex2 );
    }
    else if( lex1.isType( DASH ) &&
      lex2.isType( QUOTE_SINGLE ) &&
      lex3.isType( QUOTE_DOUBLE ) ) {
      // Example: ---'"
      consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
      mClosingSingleQuotes.add( lex2 );
    }
    else if( lex2.isType( QUOTE_SINGLE, QUOTE_DOUBLE ) ) {
      // After tokenizing, the parser will attempt to resolve ambiguities.
      unresolved.add( new Lexeme[]{lex1, lex2, lex3} );
    }

    // Suggest to the caller that resolution should be performed. This allows
    // the algorithm to reset the opening/closing quote balance before the
    // next paragraph is parsed.
    return lex3.isType( EOP );
  }

  private void resolve(
    final List<Lexeme[]> unresolved, final Consumer<Token> consumer ) {
    // Some non-emitted tokenized lexemes may be ambiguous.
    final var ambiguousLeadingQuotes = new ArrayList<Lexeme[]>( 16 );
    final var ambiguousLaggingQuotes = new ArrayList<Lexeme[]>( 16 );
    var resolvedLeadingQuotes = 0;
    var resolvedLaggingQuotes = 0;

    // Count the number of ambiguous and non-ambiguous open single quotes.
    for( var i = unresolved.iterator(); i.hasNext(); ) {
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
            ambiguousLeadingQuotes.add( quotes );
          }
        }
        else if( sContractions.beganUnambiguously( word3 ) ) {
          // The quote mark forms a word that does not stand alone from its
          // contraction. For example, twas is not a word: it's 'twas.
          consumer.accept( new Token( QUOTE_APOSTROPHE, lex2 ) );
          i.remove();
        }
        else if( sContractions.endedAmbiguously( word1 ) ) {
          ambiguousLaggingQuotes.add( quotes );
        }
        else if( (lex1.isSot() || lex1.isType( LEADING_QUOTE_OPENING_SINGLE )) &&
          lex3.isType( LAGGING_QUOTE_OPENING_SINGLE ) ) {
          consumer.accept( new Token( QUOTE_OPENING_SINGLE, lex2 ) );
          resolvedLeadingQuotes++;
          mOpeningSingleQuotes.add( lex2 );
          i.remove();
        }
        else if( lex1.isType( LEADING_QUOTE_CLOSING_SINGLE ) &&
          (lex3.isEot() || lex3.isType( LAGGING_QUOTE_CLOSING_SINGLE )) ) {
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, lex2 ) );
          resolvedLaggingQuotes++;
          mClosingSingleQuotes.add( lex2 );
          i.remove();
        }
        else if( lex3.isType( NUMBER ) ) {
          // E.g., '04
          ambiguousLeadingQuotes.add( quotes );
        }
      }
    }

    sort( mOpeningSingleQuotes );
    sort( mClosingSingleQuotes );
    sort( mOpeningDoubleQuotes );
    sort( mClosingDoubleQuotes );

    final var singleQuoteEmpty =
      mOpeningSingleQuotes.isEmpty() || mClosingSingleQuotes.isEmpty();
    final var doubleQuoteEmpty =
      mOpeningDoubleQuotes.isEmpty() || mClosingDoubleQuotes.isEmpty();

    final var singleQuoteDelta = abs(
      mClosingSingleQuotes.size() - mOpeningSingleQuotes.size()
    );

    final var doubleQuoteDelta = abs(
      mClosingDoubleQuotes.size() - mOpeningDoubleQuotes.size()
    );

    final var ambiguousLeadingCount = ambiguousLeadingQuotes.size();
    final var ambiguousLaggingCount = ambiguousLaggingQuotes.size();

    if( resolvedLeadingQuotes == 1 && resolvedLaggingQuotes == 0 ) {
      if( ambiguousLeadingCount == 0 && ambiguousLaggingCount == 1 ) {
        final var balanced = singleQuoteDelta == 0;
        final var quote = balanced ? QUOTE_APOSTROPHE : QUOTE_CLOSING_SINGLE;
        final var lex = ambiguousLaggingQuotes.get( 0 );
        consumer.accept( new Token( quote, lex[ 1 ] ) );
        unresolved.remove( lex );
      }
      else if( ambiguousLeadingCount == 0 && unresolved.size() == 1 ) {
        // Must be a closing quote.
        final var closing = unresolved.get( 0 );
        consumer.accept( new Token( QUOTE_CLOSING_SINGLE, closing[ 1 ] ) );
        unresolved.remove( closing );
      }
    }
    else if( ambiguousLeadingCount == 0 && ambiguousLaggingCount > 0 ) {
      // If there are no ambiguous leading quotes then all ambiguous lagging
      // quotes must be contractions.
      ambiguousLaggingQuotes.forEach(
        lex -> {
          consumer.accept( new Token( QUOTE_APOSTROPHE, lex[ 1 ] ) );
          unresolved.remove( lex );
        }
      );
    }
    else if( mOpeningSingleQuotes.size() == 0 &&
      mClosingSingleQuotes.size() == 1 && !unresolved.isEmpty() ) {
      final var opening = unresolved.get( 0 );
      consumer.accept( new Token( QUOTE_OPENING_SINGLE, opening[ 1 ] ) );
      unresolved.remove( opening );
    }
    else if( ambiguousLeadingCount == 0 ) {
      if( resolvedLaggingQuotes < resolvedLeadingQuotes ) {
        for( final var i = unresolved.iterator(); i.hasNext(); ) {
          final var closing = i.next()[ 1 ];
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, closing ) );
          i.remove();
        }
      }
      else if( singleQuoteDelta == unresolved.size() ) {
        for( final var i = unresolved.iterator(); i.hasNext(); ) {
          final var closing = i.next();
          consumer.accept( new Token( QUOTE_CLOSING_SINGLE, closing[ 1 ] ) );
          i.remove();
        }
      }
      else if( unresolved.size() == 2 ) {
        final var closing = unresolved.get( 0 );
        final var opening = unresolved.get( 1 );
        consumer.accept( new Token( QUOTE_CLOSING_SINGLE, closing[ 1 ] ) );
        consumer.accept( new Token( QUOTE_OPENING_SINGLE, opening[ 1 ] ) );

        // Doesn't affect the algorithm.
        unresolved.clear();
      }
    }
    else if( singleQuoteDelta == 0 && !singleQuoteEmpty ||
      doubleQuoteDelta == 0 && !doubleQuoteEmpty ) {
      // An apostrophe stands betwixt opening/closing single quotes.
      for( final var lexemes = unresolved.iterator(); lexemes.hasNext(); ) {
        final var quote = lexemes.next()[ 1 ];

        for( int i = 0; i < mOpeningSingleQuotes.size(); i++ ) {
          // An apostrophe must fall between an open/close pair.
          final var openingQuote = mOpeningSingleQuotes.get( i );
          final var closingQuote = mClosingSingleQuotes.get( i );

          if( openingQuote.before( quote ) && closingQuote.after( quote ) ) {
            consumer.accept( new Token( QUOTE_APOSTROPHE, quote ) );
            lexemes.remove();
          }
        }
      }

      // An apostrophe stands betwixt opening/closing double quotes.
      final var lexemes = unresolved.iterator();

      while( lexemes.hasNext() ) {
        final var quote = lexemes.next()[ 1 ];

        // Prevent an index out of bounds exception.
        final var len = Math.min(
          mOpeningDoubleQuotes.size(),
          mClosingDoubleQuotes.size()
        );

        for( int i = 0; i < len; i++ ) {
          // An apostrophe must fall between an open/close pair.
          final var openingQuote = mOpeningDoubleQuotes.get( i );
          final var closingQuote = mClosingDoubleQuotes.get( i );

          if( openingQuote.before( quote ) && closingQuote.after( quote ) ) {
            consumer.accept( new Token( QUOTE_APOSTROPHE, quote ) );

            try {
              lexemes.remove();
            } catch( final Exception ex ) {
              // Weird edge case that hasn't been tracked down. Doesn't affect
              // the unit tests.
              break;
            }
          }
        }
      }
    }
    else if( ambiguousLeadingCount == 1 && resolvedLaggingQuotes == 1 ) {
      final var opening = ambiguousLeadingQuotes.get( 0 );
      consumer.accept( new Token( QUOTE_OPENING_SINGLE, opening[ 1 ] ) );
      unresolved.remove( opening );
    }
  }

  /**
   * Allow subclasses to change the type of {@link Lexer}
   *
   * @param text The text to lex.
   * @return A {@link Lexer} that can split the text into {@link Lexeme}s.
   */
  Lexer createLexer( final String text ) {
    return new Lexer( text );
  }

  /**
   * Remove the last {@link Lexeme}s from the given list.
   *
   * @param unresolved The list of {@link Lexeme}s to modify.
   */
  private void truncate( final List<Lexeme[]> unresolved ) {
    if( !unresolved.isEmpty() ) {
      unresolved.remove( unresolved.size() - 1 );
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
