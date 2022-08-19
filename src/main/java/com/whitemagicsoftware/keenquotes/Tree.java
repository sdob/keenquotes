/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static com.whitemagicsoftware.keenquotes.Token.NONE;
import static com.whitemagicsoftware.keenquotes.TokenType.*;

/**
 * Responsible for helping determine whether ambiguous quotation marks can be
 * disambiguated.
 *
 * @param <T> The type of items to add to the {@link Tree}.
 */
@SuppressWarnings( "unchecked" )
class Tree<T extends Token> implements Stem {
  /**
   * Assign to {@code null} if this is the root of the {@link Tree}.
   */
  private final Tree<T> mParent;

  /**
   * The insertion-ordered sequence of subtrees and leaves.
   */
  private final Collection<Stem> mStems = new LinkedHashSet<>( 128 );

  private final T mOpening;

  /**
   * Mutated by {@link #closing(Token)}.
   */
  private T mClosing = (T) NONE;

  /**
   * Creates a root {@link Tree} instance (no parent).
   */
  public Tree() {
    mParent = null;
    mOpening = (T) NONE;
  }

  /**
   * Creates a new {@link Tree} that branches off of the given parent. The
   * only time that a new {@link Tree} may be branched is if an opening
   * quotation mark is encountered.
   *
   * @param parent  The new subtree.
   * @param opening The opening quotation mark that requires a subtree.
   */
  private Tree( final Tree<T> parent, final T opening ) {
    assert parent != null;
    assert opening != null;
    assert opening != NONE;

    mParent = parent;
    mOpening = opening;
  }

  /**
   * Adds a new subtree to this node with the subtree's parent as this node.
   *
   * @param opening The opening quotation mark that necessitates branching.
   * @return A new subtree instance.
   */
  public Tree<T> opening( final T opening ) {
    assert opening != null;

    return add( new Tree<>( this, opening ) );
  }

  /**
   * Closes the current open/close quotation mark pair.
   *
   * @param closing The closing quotation mark that pairs the opening mark.
   * @return Parent subtree instance, or this instance if at the root,
   * never {@code null}.
   */
  public Tree<T> closing( final T closing ) {
    assert closing != NONE;
    assert mOpening.isBefore( closing );

    mClosing = closing;

    return mParent == null ? this : mParent;
  }

  /**
   * Adds any {@link Stem} implementation to the child stems of this structure.
   *
   * @param stem The child to add.
   * @return The item added to the {@link Tree}.
   */
  public <S extends Stem> S add( final S stem ) {
    assert stem != null;

    mStems.add( stem );

    return stem;
  }

  /**
   * Answers whether this level in the {@link Tree} contains both an opening
   * quotation mark and a closing quotation mark of the same type.
   *
   * @return {@code true} iff the opening and closing quotation marks are
   * the same type (double or single).
   */
  public boolean isBalanced() {
    return
      mOpening.isType( QUOTE_OPENING_DOUBLE ) &&
        mClosing.isType( QUOTE_CLOSING_DOUBLE ) ||
        mOpening.isType( QUOTE_OPENING_SINGLE ) &&
          mClosing.isType( QUOTE_CLOSING_SINGLE );
  }

  public boolean hasOpeningSingleQuote() {
    return mOpening.isType( QUOTE_OPENING_SINGLE );
  }

  public boolean hasClosingSingleQuote() {
    return mClosing.isType( QUOTE_CLOSING_SINGLE );
  }

  public int count( final TokenType tokenType ) {
    var count = 0;

    for( final var stem : mStems ) {
      if( (stem instanceof Token token) && token.isType( tokenType ) ) {
        count++;
      }
    }

    return count;
  }

  public void replaceAll( final TokenType oldToken, final TokenType newToken ) {
    System.out.printf( "replace %s with %s%n", oldToken, newToken);
    for( final var stem : mStems ) {
      if( stem instanceof Token token && token.isType( oldToken ) ) {
        token.setTokenType( newToken );
      }
    }
  }

  public Tree<T> parent() {
    return mParent;
  }

  public List<Tree<T>> subtrees() {
    final var result = new ArrayList<Tree<T>>();

    for( final var stem : mStems ) {
      if( stem instanceof Tree tree ) {
        result.add( tree );
      }
    }

    return result;
  }

  public String toXml() {
    final var sb = new StringBuilder( 128 );
    final var name = parent() == null ? "root" : "tree";

    sb.append( '<' );
    sb.append( name );
    sb.append( '>' );

    if( !mOpening.isType( TokenType.NONE ) ) {
      sb.append( mOpening.toXml() );
    }

    mStems.forEach( stem -> sb.append( stem.toXml() ) );

    if( !mClosing.isType( TokenType.NONE ) ) {
      sb.append( mClosing.toXml() );
    }

    sb.append( "</" );
    sb.append( name );
    sb.append( '>' );

    return sb.toString();
  }
}
