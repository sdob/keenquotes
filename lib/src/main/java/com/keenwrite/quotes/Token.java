/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import static com.keenwrite.quotes.TokenType.INVALID;

/**
 * Responsible for tracking the beginning and ending offsets of a token within
 * a text string.
 */
public class Token {
  /**
   * Denotes there are no more tokens: the end of text (EOT) has been reached.
   */
  public static final Token EOT = new Token();

  private final TokenType mType;
  private final int mBegan;
  private final int mEnded;

  /**
   * Set to {@code true} if there are more tokens to parse.
   */
  private final boolean mHasNext;

  /**
   * Create an end of text token.
   */
  private Token() {
    this( INVALID, -1, -1, false );
  }

  /**
   * Create a token that indicates there are no more tokens.
   */
  private Token( final TokenType type, final int began, final int ended ) {
    this( type, began, ended, true );
  }

  /**
   * Create a token
   */
  private Token(
    final TokenType type, final int began, final int ended,
    final boolean hasNext ) {
    mType = type;
    mBegan = began;
    mEnded = ended + 1;
    mHasNext = hasNext;
  }

  /**
   * Extracts a sequence of characters from the given text at the offsets
   * captured by this token.
   *
   * @param text The text that was parsed using this class.
   * @return The character string captured by the token.
   */
  public String toString( final String text ) {
    return text.substring( mBegan, mEnded );
  }

  public boolean hasNext() {
    return mHasNext;
  }

  public boolean isType( final TokenType type ) {
    return mType == type;
  }

  TokenType getType() {
    return mType;
  }

  static Token createToken(
    final TokenType token, final int began, final int ended ) {
    return new Token( token, began, ended );
  }
}
