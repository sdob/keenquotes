/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.keenwrite.quotes.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests tokenizing words, numbers, punctuation, spaces, and newlines.
 */
class TokenizerTest {
  @Test
  void test_Tokenize_Words_TokenValues() {
    testText( "abc 123", "abc", " ", "123" );
    testText( "-123 abc", "-", "123", " ", "abc" );
  }

  @Test
  void test_Tokenize_Numbers_EmitNumbers() {
    testType( ".123", NUMBER );
    testType( "-123.", PUNCT, NUMBER, PERIOD );
    testType( " 123.123.123", SPACE, NUMBER );
    testType( "123 123\"", NUMBER, SPACE, NUMBER, QDOUBLE );
    testType( "-123,123.123", PUNCT, NUMBER );
  }

  @Test
  void test_Tokenize_Words_EmitWords() {
    testType( "abc", WORD );
    testType( "abc abc", WORD, SPACE, WORD );
    testType( "abc123", WORD );
    testType( "-123abc", PUNCT, NUMBER, WORD );
    testType( "abc-o'-abc", WORD, PUNCT, WORD, QSINGLE, PUNCT, WORD );
  }

  @Test
  void test_Tokenize_PunctuationMarks_EmitPunctuationMarks() {
    testType( "!", PUNCT );
    testType( ";", PUNCT );
    testType( ".", PERIOD );
  }

  @Test
  void test_Tokenize_Quotes_EmitQuotes() {
    testType( "'", QSINGLE );
    testType( "\"", QDOUBLE );
    testType( "3 o'clock", NUMBER, SPACE, WORD, QSINGLE, WORD );
  }

  @Test
  void test_Tokenize_Newlines_EmitNewlines() {
    testType( "\r", NEWLINE );
    testType( "\n", NEWLINE );
    testType( "\r\n", NEWLINE );
    testType( "\r\n\r\n", NEWLINE, NEWLINE );
    testType( "\r\n\n\r", NEWLINE, NEWLINE, NEWLINE );
    testType( "abc \r\nabc\n", WORD, SPACE, NEWLINE, WORD, NEWLINE );
  }

  private void testType(
    final String actual, final TokenType... expected ) {
    final var tokenizer = new Tokenizer();
    final var counter = new AtomicInteger();

    tokenizer.tokenize( actual, ( token ) -> {
      final var expectedType = expected[ counter.getAndIncrement() ];
      final var actualType = token.getType();
      assertEquals( expectedType, actualType );
    } );

    // Ensure all expected tokens are matched (verify end of text reached).
    assertEquals( expected.length, counter.get() );
  }

  private void testText( final String actual, final String... expected ) {
    final var tokenizer = new Tokenizer();
    final var counter = new AtomicInteger();

    tokenizer.tokenize( actual, ( token ) -> {
      final var expectedText = expected[ counter.getAndIncrement() ];
      final var actualText = token.toString( actual );
      assertEquals( expectedText, actualText );
    } );

    // Ensure all expected tokens are matched (verify end of text reached).
    assertEquals( expected.length, counter.get() );
  }
}
