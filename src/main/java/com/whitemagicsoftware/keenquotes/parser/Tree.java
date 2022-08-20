/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.parser.Token.NONE;
import static com.whitemagicsoftware.keenquotes.parser.TokenType.*;

/**
 * Responsible for helping determine whether ambiguous quotation marks can be
 * disambiguated.
 *
 * @param <T> The type of items to add to the {@link Tree}.
 */
class Tree<T extends Token> implements Stem {
  /**
   * Assign to {@code null} if this is the root of the {@link Tree}.
   */
  private final Tree<T> mParent;

  /**
   * Subtrees and {@link Token}s.
   */
  private final Collection<Stem> mStems = new LinkedHashSet<>( 128 );

  /**
   * The opening quotation mark that necessitated a new {@link Tree} instance.
   */
  private final T mOpening;

  /**
   * Mutated by {@link #closing(Token)} when performing disambiguation.
   */
  @SuppressWarnings( "unchecked" )
  private T mClosing = (T) NONE;

  /**
   * Creates a root {@link Tree} instance (no parent).
   */
  @SuppressWarnings( "unchecked" )
  public Tree() {
    mParent = null;
    mOpening = (T) NONE;
  }

  /**
   * Constructs a new {@link Tree} that branches off of the given parent. The
   * only time that a new {@link Tree} is branched is when an opening quotation
   * mark is encountered.
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
   * Answers whether an opening single quote has been assigned. This will be
   * {@code true} for any subtree (because a subtree can only be instantiated
   * with a non-ambiguous opening quotation mark).
   *
   * @return {@code true} if this {@link Tree} has a valid opening single
   * quote.
   */
  public boolean hasOpeningSingleQuote() {
    return mOpening.isType( QUOTE_OPENING_SINGLE );
  }

  /**
   * Answers whether a closing single quote has been assigned.
   *
   * @return {@code true} if this {@link Tree} has a valid closing single
   * quote.
   */
  public boolean hasClosingSingleQuote() {
    return mClosing.isType( QUOTE_CLOSING_SINGLE );
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
        hasOpeningSingleQuote() && hasClosingSingleQuote();
  }

  /**
   * Counts the number of {@link Token}s at this level of the {@link Tree}.
   * This will not traverse to the parent or child {@link Tree}s.
   *
   * @param tokenType The {@link Token} type to tally.
   * @return The number of {@link Token}s present in this {@link Tree},
   * not including ancestors or descendants.
   */
  public int count( final TokenType tokenType ) {
    final var count = new AtomicInteger();

    iterateTokens( token -> {
      if( token.isType( tokenType ) ) {
        count.incrementAndGet();
      }
    } );

    return count.get();
  }

  /**
   * Passes all {@link Token} instances in this {@link Tree} to the given
   * {@link Consumer}. This does not traverse ancestors or descendants.
   *
   * @param consumer Receives each {@link Token} instance, including the
   *                 opening and closing {@link Token}s, if assigned.
   */
  public void iterateTokens( final Consumer<Token> consumer ) {
    if( !mOpening.isType( TokenType.NONE ) ) {
      consumer.accept( mOpening );
    }

    for( final var stem : mStems ) {
      if( stem instanceof Token token ) {
        consumer.accept( token );
      }
    }

    if( !mClosing.isType( TokenType.NONE ) ) {
      consumer.accept( mClosing );
    }
  }

  /**
   * Performs an iterative, breadth-first visit of every tree and subtree in
   * the nested hierarchy of quotations.
   *
   * @param consumer Recipient of all {@link Tree} nodes, breadth-first.
   */
  public void visit( final Consumer<Tree<T>> consumer ) {
    final var queue = new LinkedList<Tree<T>>();
    queue.add( this );

    while( !queue.isEmpty() ) {
      final var current = queue.poll();

      consumer.accept( current );

      queue.addAll( current.subtrees() );
    }
  }

  /**
   * Adds a new subtree to this node with the subtree's parent as this node.
   *
   * @param opening The opening quotation mark that necessitates branching.
   * @return A new subtree instance.
   */
  Tree<T> opening( final T opening ) {
    assert opening != null;

    return add( new Tree<>( this, opening ) );
  }

  /**
   * Closes the current open/close quotation mark pair.
   *
   * @param closing The closing quotation mark that pairs the opening mark.
   * @return Parent instance, or this instance if at the root, never
   * {@code null}.
   */
  Tree<T> closing( final T closing ) {
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
  <S extends Stem> S add( final S stem ) {
    assert stem != null;

    mStems.add( stem );

    return stem;
  }

  /**
   * Finds the {@link Tree}'s root.
   *
   * @return The earliest ancestor such that its parent is {@code null}.
   */
  Tree<T> root() {
    Tree<T> ancestor = this;

    // Search for the tree's root.
    while( ancestor.parent() != null ) {
      ancestor = ancestor.parent();
    }

    return ancestor;
  }

  void replaceAll( final TokenType oldToken, final TokenType newToken ) {
    for( final var stem : mStems ) {
      if( stem instanceof Token token && token.isType( oldToken ) ) {
        token.setTokenType( newToken );
      }
    }
  }

  private Tree<T> parent() {
    return mParent;
  }

  @SuppressWarnings( {"rawtypes", "unchecked"} )
  private List<Tree<T>> subtrees() {
    final var result = new ArrayList<Tree<T>>();

    for( final var stem : mStems ) {
      if( stem instanceof Tree tree ) {
        result.add( tree );
      }
    }

    return result;
  }

  /**
   * Helper method to convert this {@link Tree} into a well-formed XML string.
   * This is useful for debugging.
   *
   * @return The XMl representation of this object.
   */
  @Override
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
