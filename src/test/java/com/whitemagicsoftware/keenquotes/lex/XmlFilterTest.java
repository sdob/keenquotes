/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.lex;

import org.junit.jupiter.api.Test;

import static com.whitemagicsoftware.keenquotes.lex.LexemeType.*;

/**
 * Test that parsing XML documents ignores elements.
 */
final class XmlFilterTest {

  @Test
  void test_Lexing_Xml_EmitTags() {
    final var actual =
      "A <em>world's</em> aflame <pre><code>ch = '\\''</code></pre>.";
    LexerTest.testType(
      actual,
      createXmlFilter(),
      // A        world       '         s          aflame          .
      WORD, SPACE, WORD, QUOTE_SINGLE, WORD, SPACE, WORD, SPACE, PERIOD
    );
  }

  @Test
  void test_Lexing_XmlAttribute_EmitTags() {
    final var actual = "<a href=\"http://x.org\">X11</a>";
    LexerTest.testType( actual, createXmlFilter(), WORD );
  }

  static LexerFilter createXmlFilter() {
    return new XmlFilter();
  }
}
