/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.function.Consumer;

/**
 * Converts straight double/single quotes and apostrophes to curly equivalents.
 */
public class Parser implements Consumer<Token> {
  private final String mText;

  public Parser( final String text ) {
    mText = text;
  }

  public void parse() {
    final var tokenizer = new Tokenizer();
    tokenizer.tokenize( mText, this );
  }

  @Override
  public void accept( final Token token ) {
    System.out.print( token.getType() + " " );
  }
}
