/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

/**
 * Represents the type of a {@link Lexeme} parsed by the {@link Lexer}.
 */
public enum LexemeType {
  QUOTE_SINGLE,
  QUOTE_DOUBLE,
  ESC_SINGLE,
  ESC_DOUBLE,
  EOL,
  EOP,
  SPACE,
  WORD,
  NUMBER,
  PUNCT,
  OPENING_GROUP,
  CLOSING_GROUP,
  HYPHEN,
  DASH,
  PERIOD,
  ELLIPSIS,
  FLAG
}
