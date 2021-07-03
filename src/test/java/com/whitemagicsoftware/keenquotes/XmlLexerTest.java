/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import org.junit.jupiter.api.Test;

import static com.whitemagicsoftware.keenquotes.LexemeType.*;
import static com.whitemagicsoftware.keenquotes.LexerTest.testText;
import static com.whitemagicsoftware.keenquotes.LexerTest.testType;

/**
 * Test that parsing XML documents ignores elements.
 */
final class XmlLexerTest {

  @Test
  void test_Lexing_Xml_LexemeValues() {
    final var actual = "Hello <em>world</em>";
    testText(
      createXmlLexer( actual ), actual,
      "Hello", " ", "<em>", "world", "</em>"
    );
  }

  @Test
  void test_Lexing_Xml_EmitTags() {
    final var actual = "The <em>world's</em> aflame.";
    testType(
      createXmlLexer( actual ), actual,
      WORD, SPACE, TAG, WORD, QUOTE_SINGLE, WORD, TAG, SPACE, WORD, PERIOD
    );
  }

  @Test
  void test_Lexing_XmlAttribute_EmitTags() {
    final var actual = "<a href=\"http://x\">x</a>";
    testType( createXmlLexer( actual ), actual, TAG, WORD, TAG );
  }

  static Lexer createXmlLexer( final String text ) {
    return new XmlLexer( text );
  }
}
