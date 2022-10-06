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

  public LexemeType() {
    this( LEX_NONE );
  }

  public LexemeType( final LexemeGlyph glyph ) {
    setGlyph( glyph );
  }

  public LexemeType with( final LexemeGlyph glyph ) {
    setGlyph( glyph );
    return this;
  }

  public LexemeGlyph glyph() {
    return mGlyph;
  }

  private void setGlyph( final LexemeGlyph glyph ) {
    mGlyph = glyph;
  }
}
