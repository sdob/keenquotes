/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.Map;

import static com.whitemagicsoftware.keenquotes.TokenType.*;

/**
 * Represents a high-level token read from a text document.
 */
final class Token implements Comparable<Token>, Stem {
  /**
   * Denotes that the token does not represent a value in the parsed document.
   */
  public static final Token NONE = new Token( TokenType.NONE, Lexeme.NONE );

  private TokenType mTokenType;
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
   * single character. Almost all tokens represent a single character, only
   * the double-prime sequence ({@code ''}) is more than one character.
   *
   * @param tokenType The type of {@link Token} to create.
   * @param began     Beginning offset into text where token is found.
   * @param ended     Ending offset into text where token is found.
   */
  Token( final TokenType tokenType, final int began, final int ended ) {
    assert tokenType != null;
    assert began >= 0;
    assert ended >= began;

    mTokenType = tokenType;
    mBegan = began;
    mEnded = ended;
  }

  /**
   * Answers whether this {@link Token} appears before the given {@link Token}
   * in the document. If they overlap, this will return {@code false}.
   *
   * @param token The {@link Token} to compare against.
   * @return {@code true} iff this {@link Token} sequence ends
   * <em>before</em> the given {@link Token} sequence begins.
   */
  boolean isBefore( final Token token ) {
    assert token != null;
    assert token != NONE;

    return mEnded <= token.mBegan;
  }

  /**
   * Answers whether this {@link Token} appears after the given {@link Token}
   * in the document. If they overlap, this will return {@code false}.
   *
   * @param token The {@link Token} to compare against.
   * @return {@code true} iff this {@link Token} sequence starts <em>after</em>
   * the given {@link Token} sequence ends.
   */
  @SuppressWarnings( "unused" )
  boolean isAfter( final Token token ) {
    assert token != null;
    assert token != NONE;

    return mBegan > token.mEnded;
  }

  TokenType getType() {
    return mTokenType;
  }

  boolean isType( final TokenType tokenType ) {
    assert tokenType != null;

    return mTokenType == tokenType;
  }

  int began() {
    return mBegan;
  }

  int ended() {
    return mEnded;
  }

  boolean isAmbiguous() {
    return mTokenType == AMBIGUOUS ||
      mTokenType == QUOTE_AMBIGUOUS_LEADING ||
      mTokenType == QUOTE_AMBIGUOUS_LAGGING;
  }

  /**
   * Allows mutating an ambiguous token into a non-ambiguous token while
   * preserving the indexes into the document. This also prevents having to
   * create additional tokens when resolving.
   *
   * @param tokenType The new state, must not be ambiguous.
   */
  void setTokenType( final TokenType tokenType ) {
    assert isAmbiguous();
    assert tokenType != null;

    mTokenType = tokenType;

    assert !isAmbiguous();
  }

  @Override
  public int compareTo( final Token that ) {
    return this.mBegan - that.mBegan;
  }

  @Override
  public String toXml() {
    return "<" +
      mTokenType +
      " type='" + getType().name() + "'" +
      " began='" + began() + "'" +
      " ended='" + ended() + "' />";
  }

  public String toString( final Map<TokenType, String> entities ) {
    return entities.get( getType() );
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + '[' +
      "mType=" + mTokenType +
      ", mBegan=" + mBegan +
      ", mEnded=" + mEnded +
      ']';
  }
}
