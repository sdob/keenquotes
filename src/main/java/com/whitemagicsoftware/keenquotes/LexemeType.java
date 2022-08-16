/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Represents the type of {@link Lexeme} parsed by the {@link Lexer}.
 */
@SuppressWarnings( "SpellCheckingInspection" )
public enum LexemeType {
  QUOTE_SINGLE,
  QUOTE_SINGLE_OPENING,
  QUOTE_SINGLE_CLOSING,
  QUOTE_DOUBLE,
  QUOTE_DOUBLE_OPENING,
  QUOTE_DOUBLE_CLOSING,
  ESC_SINGLE,
  ESC_DOUBLE,
  SOT,
  EOL,
  EOP,
  EOT,
  SPACE,
  WORD,
  NUMBER,
  PUNCT,
  OPENING_GROUP,
  CLOSING_GROUP,
  HYPHEN,
  DASH,
  EQUALS,
  PERIOD,
  ELLIPSIS,
  ANY,
  ENDING,
  EAT
}
