/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

/**
 * The {@link Parser} emits these token types.
 */
public enum TokenType {
  QUOTE_OPENING_SINGLE,
  QUOTE_OPENING_DOUBLE,
  QUOTE_CLOSING_SINGLE,
  QUOTE_CLOSING_DOUBLE,
  QUOTE_APOSTROPHE,
  QUOTE_PRIME_SINGLE,
  QUOTE_PRIME_DOUBLE,
  TEXT,
}
