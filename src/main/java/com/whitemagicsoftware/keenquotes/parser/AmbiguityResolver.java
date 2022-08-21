/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import com.whitemagicsoftware.keenquotes.lex.LexerFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.parser.TokenType.*;

/**
 * Responsible for resolving ambiguous quotes that could not be resolved during
 * a first-pass parse of a document. Creates an ordered sequence of tokens that
 * can be used to resolve certain ambiguities due to the fact that nested
 * quotations must alternate between double and single quotes. This means that
 * a string such as, "Is Iris' name Greek?" can have its single quote resolved
 * (because the apostrophe is alone within double quotes).
 */
public final class AmbiguityResolver implements Consumer<Token> {

  private final Consumer<Token> mConsumer;
  private Tree<Token> mTree = new Tree<>();

  private AmbiguityResolver( final Consumer<Token> consumer ) {
    assert consumer != null;
    mConsumer = consumer;
  }

  /**
   * Entry point into the straight quote disambiguation algorithm.
   *
   * @param text         Document to curl.
   * @param contractions Set of known contractions (ambiguous and otherwise).
   * @param consumer     Recipient of quotation marks to curl.
   */
  public static void resolve(
    final String text,
    final Contractions contractions,
    final Consumer<Token> consumer,
    final LexerFilter filter ) {
    final var resolver = new AmbiguityResolver( consumer );

    QuoteEmitter.analyze( text, contractions, resolver, filter );
    resolver.resolve();
  }

  /**
   * Accepts opening, closing, ambiguous, and apostrophe {@link Token} types.
   *
   * @param token The {@link Token} to be resolved.
   */
  @Override
  public void accept( final Token token ) {
    // Create a new subtree when an opening quotation mark is found.
    if( token.isType( QUOTE_OPENING_SINGLE ) ||
      token.isType( QUOTE_OPENING_DOUBLE ) ) {
      mTree = mTree.opening( token );
    }
    // Close the subtree if it was open, try to close it.
    else if( token.isType( QUOTE_CLOSING_SINGLE ) ||
      token.isType( QUOTE_CLOSING_DOUBLE ) ) {
      mTree = mTree.closing( token );
    }
    // Add ambiguous tokens to be resolved; add apostrophes for later emitting.
    else {
      mTree.add( token );
    }
  }

  /**
   * Traverse the tree and resolve as many ambiguous tokens as possible. This
   * is called after the document's AST is built.
   */
  private void resolve() {
    final var tokens = new ArrayList<Token>();

    // Opening and closing quotes aren't necessarily balanced, meaning the tree
    // could be dangling anywhere below the root. We need to traverse the whole
    // structure, every token from the top-down, when visiting breadth-first.
    mTree = mTree.root();

    // Replace the tree's tokens in situ with their deduced quotation mark.
    mTree.visit( this::disambiguate );

    // The tokens are not necessarily replaced or constructed in order.
    mTree.visit( tree -> tree.iterateTokens( tokens::add ) );

    Collections.sort( tokens );

    // All laggards appearing before the first leader are apostrophes.
    resolve( tokens );

    // Replacing laggards may have made leaders resolvable.
    mTree.visit( this::disambiguate );

    // Relay the tokens, in order, for updating the parsed document.
    tokens.forEach( mConsumer );
  }

  /**
   * Converts all laggards into apostrophes up until the first leader is found.
   *
   * @param tokens The list of sorted {@link Token}s to convert.
   */
  private void resolve( final List<Token> tokens ) {
    assert tokens != null;

    for( final var token : tokens ) {
      if( token.isType( QUOTE_AMBIGUOUS_LEADING ) ) {
        // Once a leader quote is found, any laggard could be a closing quote.
        break;
      }
      else if( token.isType( QUOTE_AMBIGUOUS_LAGGING ) ) {
        token.setTokenType( QUOTE_APOSTROPHE );
      }
    }
  }

  /**
   * The workhorse logic, which replaces ambiguous quotation marks with
   * resolvable equivalents. Any unresolved quotation marks are left in the
   * data structure, marked as an ambiguous form.
   *
   * @param tree The {@link Tree} that may contain ambiguous tokens to resolve.
   */
  private void disambiguate( final Tree<Token> tree ) {
    final var countLeading = tree.count( QUOTE_AMBIGUOUS_LEADING );
    final var countLagging = tree.count( QUOTE_AMBIGUOUS_LAGGING );
    final var countUnknown = tree.count( AMBIGUOUS );

    if( tree.hasOpeningSingleQuote() && !tree.hasClosingSingleQuote() ) {
      if( countUnknown == 0 && countLeading == 0 && countLagging == 1 ) {
        tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_CLOSING_SINGLE );
      }
      else if( countUnknown == 1 && countLagging == 0 ) {
        tree.replaceAll( AMBIGUOUS, QUOTE_CLOSING_SINGLE );
      }
    }

    if( countUnknown == 0 && countLeading == 1 && countLagging == 0 &&
      !tree.hasOpeningSingleQuote() && tree.hasClosingSingleQuote() ) {
      tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_OPENING_SINGLE );
    }

    if( !tree.hasOpeningSingleQuote() && !tree.hasClosingSingleQuote() ||
      tree.isBalanced() ) {
      if( countUnknown == 0 && countLeading > 0 && countLagging == 0 ) {
        tree.replaceAll( QUOTE_AMBIGUOUS_LEADING, QUOTE_APOSTROPHE );
      }

      if( countUnknown == 0 && countLeading == 0 && countLagging > 0 ) {
        tree.replaceAll( QUOTE_AMBIGUOUS_LAGGING, QUOTE_APOSTROPHE );
      }
    }
  }
}
