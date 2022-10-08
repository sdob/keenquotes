/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.function.Function;

import static com.whitemagicsoftware.keenquotes.texts.TestResource.open;
import static com.whitemagicsoftware.keenquotes.texts.TestResource.readPairs;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class CurlerTest {
  private static final String SEP = System.lineSeparator();

  /**
   * Tests that straight quotes are converted to curly quotes.
   */
  @Test
  public void test_Parse_UncurledQuotes1_CurlyQuotes() throws IOException {
    testCurler( createCurler( true ), "unambiguous-1-pass.txt" );
  }

  @Test
  public void test_Parse_UncurledQuotes2_CurlyQuotes() throws IOException {
    testCurler( createCurler( true ), "unambiguous-2-pass.txt" );
  }

  @Disabled
  @SuppressWarnings( {"unused", "JUnit3StyleTestMethodInJUnit4Class"} )
  public void test_Parse_AmbiguousQuotes_PartiallyCurled() throws IOException {
    testCurler( createCurler( false ), "ambiguous-n-pass.txt" );
  }

  @Test
  public void test_Parse_UncurledQuotesXml_CurlyQuotes() throws IOException {
    testCurler( createCurler( true ), "xml.txt" );
  }

  @Test
  public void test_Parse_UncurledQuotesI11l_CurlyQuotes() throws IOException {
    testCurler( createCurler( true ), "i18n.txt" );
  }

  /**
   * Re-enable using <a href="https://www.gutenberg.org">Project Gutenberg</a>
   * texts.
   *
   * @param filename A plain text file to convert.
   * @throws IOException Could not find, open, or read from text file.
   */
  @ParameterizedTest
  @ValueSource( strings = {"autonoma"} )
  @Disabled
  void test_Parse_Story_Converted( final String filename ) throws IOException {
    final var sb = new StringBuilder( 2 ^ 20 );

    try( final var reader = open( filename + ".html" ) ) {
      String line;

      while( (line = reader.readLine()) != null ) {
        sb.append( line ).append( SEP );
      }
    }

    final var curler = createCurler( true );
    System.out.println( curler.apply( sb.toString() ) );
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

  private Function<String, String> createCurler( final boolean entities ) {
    return new Curler( createContractions(), entities );
  }

  private Contractions createContractions() {
    return new Contractions.Builder().build();
  }
}
