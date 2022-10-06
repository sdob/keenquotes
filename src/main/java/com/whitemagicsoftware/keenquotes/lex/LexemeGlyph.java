/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.lex;

/**
 * Common international quotation mark symbols to allow for re-encoding when
 * exporting back to text.
 */
public enum LexemeGlyph {
  /**
   * Any other character that's not a quotation mark.
   */
  LEX_OTHER( (char) 0 ),

  LEX_SINGLE_QUOTE( '\'' ),
  LEX_SINGLE_QUOTE_OPENING( '‘' ),
  LEX_SINGLE_QUOTE_CLOSING( '’' ),

  LEX_DOUBLE_QUOTE( '"' ),
  LEX_DOUBLE_QUOTE_OPENING( '“' ),
  LEX_DOUBLE_QUOTE_CLOSING( '”' ),
  LEX_DOUBLE_QUOTE_OPENING_LOW( '„' ),

  LEX_DOUBLE_CHEVRON_LEFT( '«' ),
  LEX_DOUBLE_CHEVRON_RIGHT( '»' ),
  LEX_SINGLE_CHEVRON_LEFT( '‹' ),
  LEX_SINGLE_CHEVRON_RIGHT( '›' );

  private final char mGlyph;

  LexemeGlyph( final char glyph ) {
    mGlyph = glyph;
  }

  /**
   * Answers whether the given character matches the internal glyph.
   *
   * @param glyph The character to compare against the internal character.
   * @return {@code true} iff the characters are equal.
   */
  public boolean equals( final char glyph ) {
    return mGlyph == glyph;
  }
}
