/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;

import static java.lang.System.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class SmartQuotesTest {
  @Test
  public void test_parse_SingleLine_Parsed() {
    out.println( SmartQuotes.replace(
      "'What's this -5.5'' thing?'"
    ) );
  }

  @Test
  public void test_Parse_StraightQuotes_CurlyQuotes() throws IOException {
    testParser( SmartQuotes::replace );
  }

  public void testParser( final Function<String, String> parser )
    throws IOException {
    try( final var reader = openResource( "smartypants.txt" ) ) {
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

  private static String unescapeEol( final String s) {
    return String.join( "\n", s.split( "\\\\n" ) );
  }

  @SuppressWarnings( "SameParameterValue" )
  private BufferedReader openResource( final String filename ) {
    final var is = getClass().getResourceAsStream( filename );
    assertNotNull( is );

    return new BufferedReader( new InputStreamReader( is ) );
  }
}
