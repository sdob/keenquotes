/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Represents a high-level token read from the text.
 */
final class Token implements Comparable<Token> {
  private final TokenType mType;
  private final int mBegan;
  private final int mEnded;

  /**
   * Convenience constructor to create a token that uses the lexeme's
   * beginning and ending offsets to represent a complete token.
   *
   * @param type   The type of {@link Token} to create.
   * @param lexeme Container for beginning and ending text offsets.
   */
  Token( final TokenType type, final Lexeme lexeme ) {
    this( type, lexeme.began(), lexeme.ended() );
  }

  /**
   * This constructor can be used to create tokens that span more than a
   * single character.  Almost all tokens represent a single character, only
   * the double-prime sequence ({@code ''}) is more than one character.
   *
   * @param type  The type of {@link Token} to create.
   * @param began Beginning offset into text where token is found.
   * @param ended Ending offset into text where token is found.
   */
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
}
