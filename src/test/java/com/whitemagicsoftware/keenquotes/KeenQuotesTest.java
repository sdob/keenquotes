/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import com.whitemagicsoftware.keenquotes.ParserFactory.ParserType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.whitemagicsoftware.keenquotes.ParserFactory.ParserType.PARSER_PLAIN;
import static com.whitemagicsoftware.keenquotes.ParserFactory.ParserType.PARSER_XML;
import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class KeenQuotesTest {
  /**
   * This is a single-use test that is useful for debugging.
   */
  @Test
  @Disabled
  public void test_parse_SingleLine_Parsed() {
    final var converter = createConverter( out::println );
    out.println( converter.apply(
      "'A', 'B', and 'C' are letters."
    ) );
  }

  /**
   * Tests that straight quotes are converted to curly quotes.
   *
   * @throws IOException Error opening file full of fun.
   */
  @Test
  public void test_Parse_StraightQuotes_CurlyQuotes() throws IOException {
    testConverter( createConverter( ( lex ) -> {} ) );
  }

  @ParameterizedTest
  @MethodSource( "param_XmlParse_StraightQuotes_CurlyQuotes" )
  public void test_XmlParse_StraightQuotes_CurlyQuotes(
    final String input, final String expected ) {
    final var converter = createConverter( out::println, PARSER_XML );
    final var actual = converter.apply( input );
    assertEquals( expected, actual );
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

    final var converter = createConverter( out::println );
    System.out.println( converter.apply( sb.toString() ) );
  }

  @SuppressWarnings( "unused" )
  static Stream<Arguments> param_XmlParse_StraightQuotes_CurlyQuotes() {
    return Stream.of(
      arguments(
        "<em>'twas</em>",
        "<em>&apos;twas</em>"
      ),
      arguments(
        "<strong>'Twas</strong> <kbd>'</kbd> in <tex>Knuth's TeX</tex>",
        "<strong>&apos;Twas</strong> <kbd>'</kbd> in <tex>Knuth's TeX</tex>"
      ),
      arguments(
        "<bold>'twas</bold> redeemed for the <em>cat</em>'s eye",
        "<bold>&apos;twas</bold> redeemed for the <em>cat</em>&apos;s eye"
      ),
      arguments(
        "<a href=\"https://x.org\" title=\"X's Homepage\">X11's bomb</a>",
        "<a href=\"https://x.org\" title=\"X's Homepage\">X11&apos;s bomb</a>"
      ),
      arguments(
        "''<em>Twas</em> happening!'",
        "&lsquo;&apos;<em>Twas</em> happening!&rsquo;"
      )
    );
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
  private void testConverter( final Function<String, String> parser )
    throws IOException {
    try( final var reader = open( "smartypants.txt" ) ) {
      String line;
      String testLine = "";
      String expected = "";

      while( ((line = reader.readLine()) != null) ) {
        if( line.startsWith( "#" ) || line.isBlank() ) { continue; }

        // Read the first line of the couplet.
        if( testLine.isBlank() ) {
          testLine = line;
          continue;
        }

        // Read the second line of the couplet.
        if( expected.isBlank() ) {
          expected = line;
        }

        testLine = unescapeEol( testLine );
        expected = unescapeEol( expected );

        final var actual = parser.apply( testLine );
        assertEquals( expected, actual );

        testLine = "";
        expected = "";
      }
    }
  }

  private static String unescapeEol( final String s ) {
    return String.join( "\n", s.split( "\\\\n" ) );
  }

  /**
   * Opens a text file for reading. Callers are responsible for closing.
   *
   * @param filename The file to open.
   * @return An instance of {@link BufferedReader} that can be used to
   * read all the lines in the file.
   */
  private BufferedReader open( final String filename ) {
    final var is = getClass().getResourceAsStream( filename );
    assertNotNull( is );

    return new BufferedReader( new InputStreamReader( is ) );
  }

  private Function<String, String> createConverter(
    final Consumer<Lexeme> unresolved ) {
    return createConverter( unresolved, PARSER_PLAIN );
  }

  private Function<String, String> createConverter(
    final Consumer<Lexeme> unresolved, final ParserType parserType ) {
    return new Converter( unresolved, parserType );
  }
}
