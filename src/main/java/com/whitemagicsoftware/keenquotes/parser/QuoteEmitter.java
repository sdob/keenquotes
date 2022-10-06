/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import com.whitemagicsoftware.keenquotes.lex.Lexeme;
import com.whitemagicsoftware.keenquotes.lex.LexemeType;
import com.whitemagicsoftware.keenquotes.lex.Lexer;
import com.whitemagicsoftware.keenquotes.lex.LexerFilter;
import com.whitemagicsoftware.keenquotes.util.CircularFifoQueue;

import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.lex.LexemeType.*;
import static com.whitemagicsoftware.keenquotes.parser.TokenType.*;

/**
 * Responsible for emitting quotation marks as logical groups. This is the
 * first pass of a two-pass parser. Each group contains enough context to
 * distinguish whether a quotation mark is unambiguous or ambiguous with
 * respect to curling. Each {@link Lexeme} is marked as such.
 */
@SuppressWarnings( "SameParameterValue" )
public final class QuoteEmitter implements Consumer<Lexeme> {
  private static final LexemeType[] WORD_PERIOD_NUMBER = {
    WORD, PERIOD, NUMBER
  };

  private static final LexemeType[] PUNCT_PERIOD_ELLIPSIS_DASH = {
    PUNCT, PERIOD, ELLIPSIS, DASH
  };

  private static final LexemeType[] PUNCT_PERIOD = {
    PUNCT, PERIOD
  };

  private static final LexemeType[] SPACE_DASH_ENDING = {
    SPACE, DASH, ENDING
  };

  private static final LexemeType[] SPACE_ENDING = {
    SPACE, ENDING
  };

  private static final LexemeType[] SPACE_HYPHEN = {
    SPACE, HYPHEN
  };

  private static final LexemeType[] SPACE_PUNCT = {
    SPACE, PUNCT
  };

  private static final LexemeType[] SPACE_SOT = {
    SPACE, SOT
  };

  /**
   * Single quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_SINGLE =
    new LexemeType[]{
      LexemeType.SOT, SPACE, DASH, QUOTE_DOUBLE, OPENING_GROUP, EOL, EOP
    };

  /**
   * Single quotes succeeded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_OPENING_SINGLE =
    new LexemeType[]{
      WORD, ELLIPSIS, QUOTE_SINGLE, QUOTE_DOUBLE
    };

  /**
   * Single quotes preceded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_CLOSING_SINGLE =
    new LexemeType[]{
      WORD, NUMBER, PERIOD, PUNCT, ELLIPSIS, QUOTE_DOUBLE
    };

  /**
   * Single quotes succeeded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_CLOSING_SINGLE =
    new LexemeType[]{
      SPACE, HYPHEN, DASH, PUNCT, PERIOD, ELLIPSIS, QUOTE_DOUBLE, CLOSING_GROUP,
      ENDING
    };

  /**
   * Double quotes preceded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{
      LexemeType.SOT, SPACE, DASH, EQUALS, OPENING_GROUP, EOL, EOP
    };

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be opening quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_OPENING_DOUBLE =
    new LexemeType[]{
      WORD, PUNCT, NUMBER, DASH, ELLIPSIS, OPENING_GROUP, QUOTE_SINGLE,
      QUOTE_SINGLE_OPENING, QUOTE_SINGLE_CLOSING, QUOTE_DOUBLE
    };

  /**
   * Double quotes preceded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LEADING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{
      WORD, NUMBER, PERIOD, PUNCT, DASH, ELLIPSIS, CLOSING_GROUP, QUOTE_SINGLE,
      QUOTE_SINGLE_CLOSING, QUOTE_SINGLE_OPENING
    };

  /**
   * Double quotes succeeded by these {@link LexemeType}s may be closing quotes.
   */
  private static final LexemeType[] LAGGING_QUOTE_CLOSING_DOUBLE =
    new LexemeType[]{
      SPACE, PUNCT, PERIOD, EQUALS, HYPHEN, DASH, QUOTE_SINGLE, CLOSING_GROUP,
      ENDING
    };

  private final CircularFifoQueue<Lexeme> mQ = new CircularFifoQueue<>( 4 );
  private final String mText;
  private final Contractions mContractions;
  private final Consumer<Token> mConsumer;

  public QuoteEmitter(
    final String text,
    final Contractions contractions,
    final Consumer<Token> consumer
  ) {
    assert text != null;
    assert contractions != null;

    mText = text;
    mContractions = contractions;
    mConsumer = consumer;
  }

  /**
   * Scans the given text document for quotation marks and passes them to the
   * given {@link Token} {@link Consumer}.
   *
   * @param text         The prose to lex.
   * @param contractions List of ambiguous and unambiguous contractions.
   * @param consumer     Receives
   */
  public static void analyze(
    final String text,
    final Contractions contractions,
    final Consumer<Token> consumer,
    final LexerFilter filter
  ) {
    final var emitter = new QuoteEmitter( text, contractions, consumer );
    Lexer.lex( text, emitter, filter );
  }

  /**
   * @param lexeme the input argument
   */
  @Override
  public void accept( final Lexeme lexeme ) {
    mQ.add( lexeme );

    if( mQ.size() == 4 ) {
      parse();
    }
  }

  private void parse() {
    final var lex1 = mQ.get( 0 );
    final var lex2 = mQ.get( 1 );
    final var lex3 = mQ.get( 2 );
    final var lex4 = mQ.get( 3 );

    // <y'all>, <Ph.D.'ll>, <20's>, <she's>
    if( match( WORD_PERIOD_NUMBER, QUOTE_SINGLE, WORD, ANY ) ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <'n'>, <'N'>, <'owlin'>
    else if(
      match( ANY, QUOTE_SINGLE, WORD, QUOTE_SINGLE ) &&
        mContractions.beganEndedUnambiguously( lex3.toString( mText ) )
    ) {
      emit( QUOTE_APOSTROPHE, lex2 );
      emit( QUOTE_APOSTROPHE, lex4 );
      mQ.set( Lexeme.NONE, 3 );
    }
    // <2''>
    else if( match( NUMBER, QUOTE_SINGLE, QUOTE_SINGLE, ANY ) ) {
      // Force double primes to conform to the same constructor usage. This
      // simplifies the tokens, reduces some memory usage,
      final var lex = new Lexeme( PRIME_DOUBLE, lex2.began(), lex3.ended() );

      emit( QUOTE_PRIME_DOUBLE, lex );
      mQ.set( Lexeme.NONE, 2 );
    }
    // <2'>
    else if( match( NUMBER, QUOTE_SINGLE, ANY, ANY ) ) {
      emit( QUOTE_PRIME_SINGLE, lex2 );
    }
    // <2">
    else if( match( NUMBER, QUOTE_DOUBLE, ANY, ANY ) ) {
      emit( QUOTE_PRIME_DOUBLE, lex2 );
    }
    // <thinkin'>
    else if(
      match( WORD, QUOTE_SINGLE, ANY, ANY ) &&
        mContractions.endedUnambiguously( lex1.toString( mText ) )
    ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <'02>
    else if( match( ANY, QUOTE_SINGLE, NUMBER, SPACE_PUNCT ) ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <'20s>
    else if(
      match( ANY, QUOTE_SINGLE, NUMBER, WORD ) &&
        "s".equalsIgnoreCase( lex4.toString( mText ) )
    ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <.'\n>
    else if( match( PUNCT_PERIOD_ELLIPSIS_DASH, QUOTE_SINGLE, ENDING, ANY ) ) {
      emit( QUOTE_CLOSING_SINGLE, lex2 );
    }
    // <\'>
    else if( match( ESC_SINGLE, ANY, ANY, ANY ) ) {
      emit( QUOTE_STRAIGHT_SINGLE, lex1 );
    }
    // <\">
    else if( match( ESC_DOUBLE, ANY, ANY, ANY ) ) {
      emit( QUOTE_STRAIGHT_DOUBLE, lex1 );

      // <\"'--->
      if( match( ESC_DOUBLE, QUOTE_SINGLE, SPACE_DASH_ENDING, ANY ) ) {
        emit( QUOTE_CLOSING_SINGLE, lex2 );
      }
    }
    // <---'" >
    else if( match( DASH, QUOTE_SINGLE, QUOTE_DOUBLE, SPACE_ENDING ) ) {
      emit( QUOTE_CLOSING_SINGLE, lex2 );
    }
    // <o’-lantern>, <o' fellow>, <O'-the>
    else if(
      match( WORD, QUOTE_SINGLE, SPACE_HYPHEN, WORD ) &&
        "o".equalsIgnoreCase( lex1.toString( mText ) )
    ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <"">, <"...>, <"word>, <---"word>
    else if(
      match(
        LEADING_QUOTE_OPENING_DOUBLE, QUOTE_DOUBLE,
        LAGGING_QUOTE_OPENING_DOUBLE, ANY
      )
    ) {
      emit( QUOTE_OPENING_DOUBLE, lex2 );
    }
    // <..."'>, <word"'>, <?"'>, <word"?>
    else if(
      match(
        LEADING_QUOTE_CLOSING_DOUBLE, QUOTE_DOUBLE,
        LAGGING_QUOTE_CLOSING_DOUBLE, ANY
      )
    ) {
      emit( QUOTE_CLOSING_DOUBLE, lex2 );
    }
    // < ''E>
    else if( match( SPACE_SOT, QUOTE_SINGLE, QUOTE_SINGLE, WORD ) ) {
      // Consume both immediately to avoid the false ambiguity <'e>.
      emit( QUOTE_OPENING_SINGLE, lex2 );
      emit( QUOTE_APOSTROPHE, lex3 );
      mQ.set( Lexeme.NONE, 1 );
      mQ.set( Lexeme.NONE, 2 );
    }
    // <'...>, <'word>, <---'word>, < 'nation>
    else if(
      match(
        LEADING_QUOTE_OPENING_SINGLE, QUOTE_SINGLE,
        LAGGING_QUOTE_OPENING_SINGLE, ANY )
    ) {
      final var word = lex3.toString( mText );

      if( mContractions.beganAmbiguously( word ) ) {
        emit( QUOTE_AMBIGUOUS_LEADING, lex2 );
      }
      else if( mContractions.beganUnambiguously( word ) ) {
        emit( QUOTE_APOSTROPHE, lex2 );
      }
      // <"'"nested>
      else if( match( QUOTE_DOUBLE, QUOTE_SINGLE, QUOTE_DOUBLE, WORD ) ) {
        emit( QUOTE_OPENING_SINGLE, lex2 );
      }
      // <"'" >
      else if( match( QUOTE_DOUBLE, QUOTE_SINGLE, QUOTE_DOUBLE, ANY ) ) {
        emit( QUOTE_AMBIGUOUS_SINGLE, lex2 );
      }
      // < '" >
      else if( match( ANY, QUOTE_SINGLE, LAGGING_QUOTE_OPENING_SINGLE, ANY ) ) {
        emit( QUOTE_OPENING_SINGLE, lex2 );
      }
      // Ambiguous
      else {
        emit( QUOTE_AMBIGUOUS_LEADING, lex2 );
      }
    }
    // <word'">, <...'--->, <"' >
    else if(
      match(
        LEADING_QUOTE_CLOSING_SINGLE, QUOTE_SINGLE,
        LAGGING_QUOTE_CLOSING_SINGLE, ANY
      )
    ) {
      final var word = lex1.toString( mText );

      if( mContractions.endedAmbiguously( word ) ) {
        emit( QUOTE_AMBIGUOUS_LAGGING, lex2 );
      }
      else {
        emit( QUOTE_CLOSING_SINGLE, lex2 );
      }
    }
    // <word';> (contraction inferred by previous matches)
    else if( match( WORD, QUOTE_SINGLE, PUNCT_PERIOD, ANY ) ) {
      emit( QUOTE_APOSTROPHE, lex2 );
    }
    // <---'">
    else if( match( DASH, QUOTE_SINGLE, QUOTE_DOUBLE, ANY ) ) {
      emit( QUOTE_CLOSING_SINGLE, lex2 );
    }
    // <'42>, <'-3.14>
    else if( match( ANY, QUOTE_SINGLE, NUMBER, ANY ) ) {
      emit( QUOTE_OPENING_SINGLE, lex2 );
    }
    // <PRE-PARSED><'---.>
    else if( match( LexemeType.NONE, QUOTE_SINGLE, ANY, ANY ) ) {
      emit( QUOTE_CLOSING_SINGLE, lex2 );
    }
    // <''Cause >
    else if( match( QUOTE_SINGLE, QUOTE_SINGLE, WORD, ANY ) ) {
      final var word = lex3.toString( mText );

      if( mContractions.beganAmbiguously( word ) ) {
        emit( QUOTE_AMBIGUOUS_LEADING, lex2 );
      }
      else if( mContractions.beganUnambiguously( word ) ) {
        emit( QUOTE_APOSTROPHE, lex2 );
      }
      else {
        emit( QUOTE_AMBIGUOUS_SINGLE, lex2 );
      }
    }
    else if( match( ANY, QUOTE_DOUBLE, ANY, ANY ) ) {
      emit( QUOTE_AMBIGUOUS_DOUBLE, lex2 );
    }
    // International opening quotation mark.
    else if( match( ANY, QUOTE_DOUBLE_OPENING, ANY, ANY ) ) {
      emit( QUOTE_OPENING_DOUBLE, lex2 );
    }
    // Ambiguous (no match)
    else if( match( ANY, QUOTE_SINGLE, ANY, ANY ) ) {
      emit( QUOTE_AMBIGUOUS_SINGLE, lex2 );
    }
  }

  private void emit( final TokenType tokenType, final Lexeme lexeme ) {
    mConsumer.accept( new Token( tokenType, lexeme ) );
  }

  private boolean match(
    final LexemeType l1,
    final LexemeType l2,
    final LexemeType l3,
    final LexemeType l4 ) {
    return mQ.get( 0 ).isType( l1 ) &&
      mQ.get( 1 ).isType( l2 ) &&
      mQ.get( 2 ).isType( l3 ) &&
      mQ.get( 3 ).isType( l4 );
  }

  private boolean match(
    final LexemeType[] l1,
    final LexemeType l2,
    final LexemeType l3,
    final LexemeType l4 ) {
    return mQ.get( 0 ).isType( l1 ) &&
      mQ.get( 1 ).isType( l2 ) &&
      mQ.get( 2 ).isType( l3 ) &&
      mQ.get( 3 ).isType( l4 );
  }

  private boolean match(
    final LexemeType l1,
    final LexemeType l2,
    final LexemeType[] l3,
    final LexemeType l4 ) {
    return mQ.get( 0 ).isType( l1 ) &&
      mQ.get( 1 ).isType( l2 ) &&
      mQ.get( 2 ).isType( l3 ) &&
      mQ.get( 3 ).isType( l4 );
  }

  private boolean match(
    final LexemeType l1,
    final LexemeType l2,
    final LexemeType l3,
    final LexemeType[] l4 ) {
    return mQ.get( 0 ).isType( l1 ) &&
      mQ.get( 1 ).isType( l2 ) &&
      mQ.get( 2 ).isType( l3 ) &&
      mQ.get( 3 ).isType( l4 );
  }

  private boolean match(
    final LexemeType[] l1,
    final LexemeType l2,
    final LexemeType[] l3,
    final LexemeType l4 ) {
    return mQ.get( 0 ).isType( l1 ) &&
      mQ.get( 1 ).isType( l2 ) &&
      mQ.get( 2 ).isType( l3 ) &&
      mQ.get( 3 ).isType( l4 );
  }
}
