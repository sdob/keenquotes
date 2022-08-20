/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import com.whitemagicsoftware.keenquotes.lex.FilterType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.function.Function;

import static com.whitemagicsoftware.keenquotes.lex.FilterType.FILTER_PLAIN;
import static com.whitemagicsoftware.keenquotes.lex.FilterType.FILTER_XML;
import static com.whitemagicsoftware.keenquotes.texts.TestResource.open;
import static com.whitemagicsoftware.keenquotes.texts.TestResource.readPairs;
import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class CurlerTest {
  private static final Contractions CONTRACTIONS =
    new Contractions.Builder().build();

  /**
   * This is a single-use test that is useful for debugging.
   */
  @Test
  public void test_parse_SingleLine_Parsed() {
    final var converter = createCurler( FILTER_PLAIN );
    out.println( converter.apply(
      "\"---retroactively!\""
    ) );
  }

  /**
   * Tests that straight quotes are converted to curly quotes.
   *
   * @throws IOException Error opening file full of fun.
   */
  @Test
  public void test_Parse_StraightQuotes_CurlyQuotes() throws IOException {
    testCurler( createCurler( FILTER_PLAIN ), "unambiguous-2-pass.txt" );
  }

  @Test
  public void test_XmlParse_StraightQuotes_CurlyQuotes() throws IOException {
    testCurler( createCurler( FILTER_XML ), "xml.txt" );
  }

  /**
   * Re-enable using <a href="https://www.gutenberg.org/">Project Gutenberg</a>
   * texts.
   *
   * @param filename A plain text file to convert.
   * @throws IOException Could not find, open, or read from text file.
   */
  @ParameterizedTest
  @ValueSource( strings = {"habberton"} )
  @Disabled
  void test_Parse_Story_Converted( final String filename ) throws IOException {
    final var sb = new StringBuilder( 2 ^ 20 );

    try( final var reader = open( filename + ".txt" ) ) {
      String line;
      final var sep = System.lineSeparator();

      while( (line = reader.readLine()) != null ) {
        sb.append( line ).append( sep );
      }
    }

    final var converter = createCurler( FILTER_PLAIN );
    System.out.println( converter.apply( sb.toString() ) );
  }

  /**
   * Reads a file full of couplets. The first of the pair is the input,
   * the second of the pair is the expected result. Couplets may include
   * newline characters to indicate end of lines and end of paragraphs.
   * Lines that start with {@code #} are ignored.
   *
   * @param parser The text processor capable of straight quote conversion.
   * @throws IOException Error opening file full of fun.
   */
  private void testCurler(
    final Function<String, String> parser,
    final String filename
  ) throws IOException {
    final var couplets = readPairs( filename );

    couplets.forEach( couplet -> {
      final var actual = parser.apply( couplet.item1() );
      final var expected = couplet.item2();

      assertEquals( expected, actual );
    } );
  }

  private Function<String, String> createCurler( final FilterType parserType ) {
    return new Curler( CONTRACTIONS, parserType );
  }
}
