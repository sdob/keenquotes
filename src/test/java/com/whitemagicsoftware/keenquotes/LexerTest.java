/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static com.whitemagicsoftware.keenquotes.LexemeType.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests lexing words, numbers, punctuation, spaces, newlines, etc.
 */
final class LexerTest {

  @Test
  void test_Lexing_Words_LexemeValues() {
    testText( "abc 123", "abc", " ", "123" );
    testText( "-123 abc", "-123", " ", "abc" );
  }

  @Test
  void test_Lexing_Numbers_EmitNumbers() {
    testType( ".123", NUMBER );
    testType( "-123.", NUMBER, PERIOD );
    testType( " 123.123.123", SPACE, NUMBER );
    testType( "123 123\"", NUMBER, SPACE, NUMBER, QUOTE_DOUBLE );
    testType( "-123,123.123", NUMBER );
    testType( "...1,023...", ELLIPSIS, NUMBER, ELLIPSIS );
    testType( "123-456", NUMBER );
    testType( "123-", NUMBER, HYPHEN );
    testType( "123 - 456", NUMBER, SPACE, HYPHEN, SPACE, NUMBER );
  }

  @Test
  void test_Lexing_Words_EmitWords() {
    testType( "abc", WORD );
    testType( "abc abc", WORD, SPACE, WORD );
    testType( "abc...", WORD, ELLIPSIS );
    testType( "abc123", WORD );
    testType( "-123abc", NUMBER, WORD );
    testType( "-123abc123", NUMBER, WORD );
    testType( "abc-o'-abc", WORD, HYPHEN, WORD, QUOTE_SINGLE, HYPHEN, WORD );
  }

  @Test
  void test_Lexing_PunctuationMarks_EmitPunctuationMarks() {
    testType( "!", PUNCT );
    testType( ";", PUNCT );
    testType( "\\", PUNCT );
    testType( ".", PERIOD );
    testType( "-", HYPHEN );
    testType( "--", DASH );
    testType( "---", DASH );
    testType( "–", DASH );
    testType( "―", DASH );
    testType( "—", DASH );
    testType( "—-—", DASH );
    testType( "...", ELLIPSIS );
    testType( ". .", ELLIPSIS );
    testType( ". . .", ELLIPSIS );
    testType( ".. ... ....", ELLIPSIS );
  }

  @Test
  void test_Lexing_Quotes_EmitQuotes() {
    testType( "'", QUOTE_SINGLE );
    testType( "\"", QUOTE_DOUBLE );
    testType( "‘", QUOTE_SINGLE_OPENING );
    testType( "’", QUOTE_SINGLE_CLOSING );
    testType( "“", QUOTE_DOUBLE_OPENING );
    testType( "”", QUOTE_DOUBLE_CLOSING );
    testType( "3 o'clock", NUMBER, SPACE, WORD, QUOTE_SINGLE, WORD );
  }

  @Test
  void test_Lexing_Escapes_EmitEscapedQuotes() {
    testType( "123\\'456\\\"", NUMBER, ESC_SINGLE, NUMBER, ESC_DOUBLE );
    testText( "123\\'456\\\"", "123", "\\'", "456", "\\\"" );
  }

  @Test
  void test_Lexing_Newlines_EmitNewlines() {
    testType( "\r", EOL );
    testType( "\n", EOL );
    testType( "\r\n", EOL );
    testType( "\r\n\r\n", EOP );
    testType( "\r\n\n\r", EOP );
    testType( "abc \r\nabc\n", WORD, SPACE, EOL, WORD, EOL );
  }

  @Test
  void test_Lexing_Whitespace_EmitSpace() {
    testType( "   ", SPACE );
    testType( "\n   \n", EOL, SPACE, EOL );
  }

  static void testType( final String actual, final LexemeType... expected ) {
    testType( actual, filter -> false, expected );
  }

  static void testType(
    final String actual,
    final LexerFilter filter,
    final LexemeType... expected ) {
    assert actual != null;
    assert filter != null;
    assert expected != null;

    final var list = asList( expected );

    testType( actual, ( lexeme, text ) -> lexeme.getType(), filter, list );
  }

  static void testText(
    final String actual, final String... expected ) {
    testType( actual, Lexeme::toString, asList( expected ) );
  }

  private static <A, E> void testType(
    final String text,
    final BiFunction<Lexeme, String, A> f,
    final List<E> elements ) {
    testType( text, f, filter -> false, elements );
  }

  private static <A, E> void testType(
    final String text,
    final BiFunction<Lexeme, String, A> f,
    final LexerFilter filter,
    final List<E> elements ) {
    var counter = new AtomicInteger();

    Lexer.lex( text, lexeme -> {
      // Ignore the SOT and EOT lexemes (avoids duplication).
      if( !lexeme.isType( SOT, EOT ) ) {
        final var expected = elements.get( counter.getAndIncrement() );
        final var actual = f.apply( lexeme, text );

        assertEquals( expected, actual );
      }
    }, filter );

    // Ensure all expected values are matched (verify end of text reached).
    assertEquals( elements.size(), counter.get() );
  }
}
