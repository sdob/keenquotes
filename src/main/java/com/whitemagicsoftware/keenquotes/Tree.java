/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.whitemagicsoftware.keenquotes.Token.NONE;
import static com.whitemagicsoftware.keenquotes.TokenType.*;

/**
 * Responsible for helping determine whether ambiguous quotation marks can be
 * disambiguated.
 * <p>
 * Represents a tree-like structure that provides O(1) determinate for whether
 * a branch in the tree contains an ambiguous opening or closing quotation
 * mark. The payload for each branch includes the opening and closing quotation
 * marks as well as a {@link Set} of ambiguous {@link Token} instances.
 * </p>
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
   * The insertion-ordered sequence of subtrees and leaves. Must provide
   * O(1) lookup time.
   */
  private final Collection<Stem> mStems = new LinkedHashSet<>( 128 );

  private T mOpening = (T) NONE;
  private T mClosing = (T) NONE;

  /**
   * Creates a root {@link Tree} instance (no parent).
   */
  public Tree() {
    mParent = null;
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

    final var tree = new Tree<>( this, opening );
    mStems.add( tree );

    return tree;
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
   */
  public void add( final Stem stem ) {
    assert stem != null;

    mStems.add( stem );
  }

  /**
   * Answers whether the given {@link Stem} is contained within the child
   * stems of this structure. This can be used to determine whether the
   * {@link Stem} is found within single/double quotes in O(1) time.
   *
   * @param stem The child to compare.
   * @return {@code true} if the given stem is
   */
  public boolean isNested( final Stem stem ) {
    return mStems.contains( stem );
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

  public Tree<T> parent() {
    return mParent;
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
