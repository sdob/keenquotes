/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import static com.keenwrite.quotes.LexemeType.FLAG;

/**
 * Responsible for tracking the beginning and ending offsets of a lexeme within
 * a text string. Tracking the beginning and ending indices should use less
 * memory than duplicating the entire text of Unicode characters (i.e., using
 * a similar approach to run-length encoding).
 */
public final class Lexeme implements Comparable<Lexeme> {
  /**
   * Signifies an invalid index to help distinguish EOT/SOT.
   */
  private static final int E_INDEX = -2;

  /**
   * Denotes there are no more lexemes: the end of text (EOT) has been reached.
   * The beginning index differentiates between EOT and SOT.
   */
  public static final Lexeme EOT = new Lexeme( FLAG, -1, E_INDEX );

  /**
   * Denotes parsing at the start of text (SOT). This is useful to avoid
   * branching conditions while iterating. The beginning index differentiates
   * between EOT and SOT.
   */
  public static final Lexeme SOT = new Lexeme( FLAG, 0, E_INDEX );

  private final LexemeType mType;
  private final int mBegan;
  private final int mEnded;

  /**
   * Create a lexeme that represents a section of the text.
   *
   * @param type  Type of {@link Lexeme} to create.
   * @param began Offset into the text where this instance starts (0-based).
   * @param ended Offset into the text where this instance stops (0-based).
   */
  private Lexeme( final LexemeType type, final int began, final int ended ) {
    assert type != null;
    assert began >= 0 || ended == E_INDEX;
    assert ended >= began || ended == E_INDEX;

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
    assert text != null;
    return text.substring( mBegan, mEnded );
  }

  /**
   * Answers whether the given {@link LexemeType} is the same as this
   * instance's internal {@link LexemeType}.
   *
   * @param type The {@link LexemeType} to compare.
   * @return {@code true} if the given {@link LexemeType} is equal to the
   * internal {@link LexemeType}.
   */
  public boolean isType( final LexemeType type ) {
    assert type != null;
    return mType == type;
  }

  /**
   * Answers whether any of the given {@link LexemeType} matches this
   * instance's internal {@link LexemeType}.
   *
   * @param types The {@link LexemeType}s to compare.
   * @return {@code true} if the internal {@link LexemeType} matches any one
   * of the given {@link LexemeType}s.
   */
  public boolean anyType( final LexemeType... types ) {
    assert types != null;

    for( final var type : types ) {
      if( mType == type ) {
        return true;
      }
    }

    return false;
  }

  public int began() {
    return mBegan;
  }

  public int ended() {
    return mEnded;
  }

  boolean isSot() {
    return mBegan == 0;
  }

  boolean isEot() {
    return mBegan == -1;
  }

  LexemeType getType() {
    return mType;
  }

  /**
   * Compares the starting offset of the given {@link Lexeme} to the starting
   * offset of this {@link Lexeme} instance. This allows a list {@link Lexeme}s
   * to be sorted by order of appearance in the parsed text.
   */
  @Override
  public int compareTo( final Lexeme that ) {
    assert that != null;
    return this.mBegan - that.mBegan;
  }

  static Lexeme createLexeme(
    final LexemeType lexeme, final int began, final int ended ) {
    assert lexeme != null;
    return new Lexeme( lexeme, began, ended );
  }
}
