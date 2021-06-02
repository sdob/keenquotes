/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import static com.keenwrite.quotes.LexemeType.FLAG;

/**
 * Responsible for tracking the beginning and ending offsets of a lexeme within
 * a text string. Tracking the beginning and ending indices should use less
 * memory than duplicating the entire text of Unicode characters (i.e., using
 * a similar approach to run-length encoding).
 */
public class Lexeme implements Comparable<Lexeme> {
  /**
   * Denotes there are no more lexemes: the end of text (EOT) has been reached.
   */
  public static final Lexeme EOT = new Lexeme();

  /**
   * Denotes parsing at the start of text. This is useful to avoid branching
   * conditions while iterating.
   */
  public static final Lexeme SOT = new Lexeme();

  private final LexemeType mType;
  private final int mBegan;
  private final int mEnded;

  /**
   * Create a flag that indicates a stream marker (start or end).
   */
  private Lexeme() {
    this( FLAG, -1, -1 );
  }

  /**
   * Create a lexeme that represents a section of the text.
   */
  private Lexeme( final LexemeType type, final int began, final int ended ) {
    mType = type;
    mBegan = began;
    mEnded = ended + 1;
  }

  /**
   * Extracts a sequence of characters from the given text at the offsets
   * captured by this lexeme.
   *
   * @param text The text that was parsed using this class.
   * @return The character string captured by the lexeme.
   */
  public String toString( final String text ) {
    return text.substring( mBegan, mEnded );
  }

  public boolean isType( final LexemeType type ) {
    return mType == type;
  }

  public boolean anyType( final LexemeType... types ) {
    for( final var type : types ) {
      if( mType == type ) {
        return true;
      }
    }

    return false;
  }

  LexemeType getType() {
    return mType;
  }

  int began() {
    return mBegan;
  }

  int ended() {
    return mEnded;
  }

  @Override
  public int compareTo( final Lexeme that ) {
    return this.mBegan - that.mBegan;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
      "mType=" + mType +
      ", mBegan=" + mBegan +
      ", mEnded=" + mEnded +
      '}';
  }

  static Lexeme createLexeme(
    final LexemeType lexeme, final int began, final int ended ) {
    return new Lexeme( lexeme, began, ended );
  }
}
