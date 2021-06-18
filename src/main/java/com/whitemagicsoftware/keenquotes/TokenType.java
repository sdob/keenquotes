/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * The {@link Parser} emits these token types.
 */
enum TokenType {
  QUOTE_OPENING_SINGLE,
  QUOTE_OPENING_DOUBLE,
  QUOTE_CLOSING_SINGLE,
  QUOTE_CLOSING_DOUBLE,
  QUOTE_APOSTROPHE,
  QUOTE_STRAIGHT_SINGLE,
  QUOTE_STRAIGHT_DOUBLE,
  QUOTE_PRIME_SINGLE,
  QUOTE_PRIME_DOUBLE,
}
