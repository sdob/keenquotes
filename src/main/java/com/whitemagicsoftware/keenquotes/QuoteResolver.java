/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.TokenType.*;
import static com.whitemagicsoftware.keenquotes.TokenType.QUOTE_OPENING_DOUBLE;

/**
 * Responsible for resolving ambiguous quotes that could not be resolved during
 * a first-pass parse of a document. Creates an ordered sequence of tokens that
 * can be used to resolve certain ambiguities due to the fact that nested
 * quotations must alternate between double and single quotes. This means that
 * a string such as, "Is Iris' name Greek?" can have its single quote resolved
 * (because the apostrophe is alone within double quotes).
 */
public final class QuoteResolver implements Consumer<Token> {

  private final Consumer<Token> mConsumer;
  private final List<Token> mTokens = new ArrayList<>();
  private Tree<Token> mTree = new Tree<>();

  public QuoteResolver( final Consumer<Token> consumer ) {
    assert consumer != null;
    mConsumer = consumer;
  }

  public static void analyze(
    final String text,
    final Contractions contractions,
    final Consumer<Token> consumer ) {
    final var resolver = new QuoteResolver( consumer );

    QuoteEmitter.analyze( text, contractions, resolver );
    resolver.resolve();
  }

  /**
   * Accepts only opening, closing, and ambiguous {@link Token} types.
   *
   * @param token the input argument
   */
  @Override
  public void accept( final Token token ) {
    if( token.isType( QUOTE_OPENING_SINGLE ) ||
      token.isType( QUOTE_OPENING_DOUBLE ) ) {
      mTree = mTree.opening( token );
    }
    else if(
      token.isType( QUOTE_CLOSING_SINGLE ) &&
        mTree.isOpeningTokenType( QUOTE_OPENING_SINGLE ) ||
        token.isType( QUOTE_CLOSING_DOUBLE ) &&
          mTree.isOpeningTokenType( QUOTE_OPENING_DOUBLE ) ) {
      mTree = mTree.closing( token );
    }
    else if( token.isAmbiguous() ) {
      mTree.add( token );
    }
  }

  /**
   * Traverse the tree and resolve as many ambiguous tokens as possible.
   */
  private void resolve() {
    System.out.println( mTree.toXml() );

    for( final var token : mTokens ) {
      mConsumer.accept( token );
    }
  }
}
