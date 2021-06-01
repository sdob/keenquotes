/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

/**
 * Represents a high-level token read from the text.
 */
public class Token {
  private TokenType mType;
  private Lexeme mLexeme;

  public Token( final TokenType type, final Lexeme lexeme ) {
    assert type != null;
    assert lexeme != null;

    mType = type;
    mLexeme = lexeme;
  }

  TokenType getType() {
    return mType;
  }

  public String toString( final String text ) {
    return mLexeme.toString( text );
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
      "mType=" + mType +
      ", mLexeme=" + mLexeme +
      '}';
  }
}
