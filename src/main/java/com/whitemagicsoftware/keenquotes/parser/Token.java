/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import com.whitemagicsoftware.keenquotes.lex.Lexeme;
import com.whitemagicsoftware.keenquotes.lex.LexemeGlyph;

import java.util.Map;

import static com.whitemagicsoftware.keenquotes.parser.TokenType.*;

/**
 * Represents a high-level token read from a text document.
 */
final class Token implements Comparable<Token>, Stem {
  /**
   * Denotes that the token does not represent a value in the parsed document.
   */
  public static final Token NONE = new Token( TokenType.NONE, Lexeme.NONE );

  private TokenType mTokenType;
  private final Lexeme mLexeme;

  /**
   * Convenience constructor to create a token that uses the lexeme's
   * beginning and ending offsets to represent a complete token.
   *
   * @param tokenType The type of {@link Token} to create.
   * @param lexeme    Container for text offsets and i18n glyphs.
   */
  Token( final TokenType tokenType, final Lexeme lexeme ) {
    assert tokenType != null;
    assert lexeme.began() >= 0;
    assert lexeme.ended() >= lexeme.began();

    mTokenType = tokenType;
    mLexeme = lexeme;
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

    return mLexeme.ended() <= token.began();
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

    return mLexeme.began() > token.ended();
  }

  TokenType getType() {
    return mTokenType;
  }

  boolean isType( final TokenType tokenType ) {
    assert tokenType != null;

    return mTokenType == tokenType;
  }

  int began() {
    return mLexeme.began();
  }

  int ended() {
    return mLexeme.ended();
  }

  boolean isAmbiguous() {
    return mTokenType == QUOTE_AMBIGUOUS_SINGLE ||
      mTokenType == QUOTE_AMBIGUOUS_DOUBLE ||
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
    return this.began() - that.began();
  }

  @Override
  public String toXml() {
    return "<" +
      mTokenType +
      " type='" + getType().name() + "'" +
      " began='" + began() + "'" +
      " ended='" + ended() + "' />";
  }

  public String toString(
    final Map<TokenType, String> entities,
    final Map<LexemeGlyph, String> i18n ) {
    return i18n.getOrDefault(
      mLexeme.getType().glyph(),
      entities.get( getType() )
    );
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + '[' +
      "mType=" + getType() +
      ", mBegan=" + began() +
      ", mEnded=" + ended() +
      ']';
  }
}
