/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

/**
 * Responsible for converting straight quotes into smart quotes.
 */
public class SmartQuotes {
  public String replace( final String text ) {
    final StringBuilder sb = new StringBuilder( text );

    final var parser = new Parser( text );
    parser.parse( (token) -> {

    } );

    return sb.toString();
  }
}
