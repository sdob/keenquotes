/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

/**
 * Represents a high-level token read from the text.
 */
public class Token implements Comparable<Token> {
  private final TokenType mType;
  final int mBegan;
  final int mEnded;

  /**
   * Convenience constructor to create a token that uses the lexeme's offsets.
   *
   * @param type   The type of {@link Token} to create.
   * @param lexeme Container for beginning and ending text offsets.
   */
  Token( final TokenType type, final Lexeme lexeme ) {
    this( type, lexeme.began(), lexeme.ended() );
  }

  Token( final TokenType type, final int began, final int ended ) {
    assert type != null;
    assert began >= 0;
    assert ended > began;

    mType = type;
    mBegan = began;
    mEnded = ended;
  }

  TokenType getType() {
    return mType;
  }

  int began() {
    return mBegan;
  }

  int ended() {
    return mEnded;
  }

  @Override
  public int compareTo( final Token that ) {
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
}
