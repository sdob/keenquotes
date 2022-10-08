/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.lex;

import static com.whitemagicsoftware.keenquotes.lex.LexemeGlyph.*;

/**
 * Represents the type of {@link Lexeme} parsed by the {@link Lexer}.
 */
@SuppressWarnings( "SpellCheckingInspection" )
public final class LexemeType {
  // @formatter:off
  public static final LexemeType QUOTE_SINGLE = new LexemeType( LEX_SINGLE_QUOTE );
  public static final LexemeType QUOTE_SINGLE_OPENING = new LexemeType( LEX_SINGLE_QUOTE_OPENING );
  public static final LexemeType QUOTE_SINGLE_CLOSING = new LexemeType( LEX_SINGLE_QUOTE_CLOSING );
  public static final LexemeType QUOTE_DOUBLE = new LexemeType( LEX_DOUBLE_QUOTE );
  public static final LexemeType QUOTE_DOUBLE_OPENING = new LexemeType( LEX_DOUBLE_QUOTE_OPENING );
  public static final LexemeType QUOTE_DOUBLE_CLOSING = new LexemeType( LEX_DOUBLE_QUOTE_CLOSING );
  public static final LexemeType ESC_SINGLE = new LexemeType();
  public static final LexemeType ESC_DOUBLE = new LexemeType();
  public static final LexemeType PRIME_DOUBLE = new LexemeType();
  public static final LexemeType SOT = new LexemeType();
  public static final LexemeType EOL = new LexemeType();
  public static final LexemeType EOP = new LexemeType();
  public static final LexemeType EOT = new LexemeType();
  public static final LexemeType SPACE = new LexemeType();
  public static final LexemeType WORD = new LexemeType();
  public static final LexemeType NUMBER = new LexemeType();
  public static final LexemeType PUNCT = new LexemeType();
  public static final LexemeType OPENING_GROUP = new LexemeType();
  public static final LexemeType CLOSING_GROUP = new LexemeType();
  public static final LexemeType HYPHEN = new LexemeType();
  public static final LexemeType DASH = new LexemeType();
  public static final LexemeType EQUALS = new LexemeType();
  public static final LexemeType PERIOD = new LexemeType();
  public static final LexemeType ELLIPSIS = new LexemeType();
  public static final LexemeType ENDING = new LexemeType();
  public static final LexemeType ANY = new LexemeType();
  public static final LexemeType NONE = new LexemeType();
  // @formatter:on

  private LexemeGlyph mGlyph;

  /**
   * Constructs an instance of {@link LexemeType} using
   * {@link LexemeGlyph#LEX_OTHER} to indicate that this type of lexeme isn't
   * a quotation mark glyph.
   */
  public LexemeType() {
    this( LEX_OTHER );
  }

  /**
   * Constructs an instance of {@link LexemeType} using a particular glyph.
   *
   * @param glyph Typically represents an internationalized quotation mark
   *              character.
   */
  public LexemeType( final LexemeGlyph glyph ) {
    setGlyph( glyph );
  }

  /**
   * Creates a new instance with a copy of {@link LexemeGlyph} to prevent
   * mutations by calling {@link #with(LexemeGlyph)} from affecting ambiguous
   * quotes resolution.
   *
   * @return A semi-deep copy of this instance.
   */
  LexemeType copy() {
    return new LexemeType( glyph() );
  }

  /**
   * Changes the type of glyph associated with this type of lexeme. This
   * is useful for passing along different glyphs represented by the same
   * lexeme (such as different opening quotation marks).
   *
   * @param glyph The new {@link LexemeGlyph} to associate, often an
   *              internationalized quotation mark.
   * @return {@code this} to allow chaining.
   */
  public LexemeType with( final LexemeGlyph glyph ) {
    setGlyph( glyph );
    return this;
  }

  /**
   * Provides the glyph used to identify international quotation marks.
   *
   * @return The glyph set either at construction time or after calling
   * {@link #with(LexemeGlyph)}.
   */
  public LexemeGlyph glyph() {
    return mGlyph;
  }

  private void setGlyph( final LexemeGlyph glyph ) {
    mGlyph = glyph;
  }

  /**
   * Provides useful debugging information.
   *
   * @return The class name and encodable glyph.
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() +
      '[' + glyph() + ']';
  }
}
