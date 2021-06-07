/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

public enum LexemeType {
  QUOTE_SINGLE,
  QUOTE_DOUBLE,
  ESC_SINGLE,
  ESC_DOUBLE,
  EOL,
  SPACE,
  WORD,
  NUMBER,
  PUNCT,
  OPENING_GROUP,
  CLOSING_GROUP,
  HYPHEN,
  PERIOD,
  ELLIPSIS,
  FLAG
}
