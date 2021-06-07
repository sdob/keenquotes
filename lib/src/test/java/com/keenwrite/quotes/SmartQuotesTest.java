/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test that English straight quotes are converted to curly quotes and
 * apostrophes.
 */
public class SmartQuotesTest {
  @Test
  public void test_parse_SingleLine_Parsed() {
    out.println( SmartQuotes.replace( "Took place in '04, yes'm!"));

    out.println( "-------" );
    out.println( SmartQuotes.replace( "'Bout that time I says, 'Boys! I been " +
                                  "thinkin' 'bout th' Universe.'"));

    out.println( "-------" );

    out.println( SmartQuotes.replace( "\"John asked, 'What are you, beyond " +
                                  "\"somethin' shiny?\"'\" said Fred.\n" ) );
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

        System.out.println( "EXPECT:" + expected );

        final var actual = parser.apply( testLine );
        System.out.println( "ACTUAL:" + actual );
        assertEquals( expected, actual );

        testLine = "";
        expected = "";
      }
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private BufferedReader openResource( final String filename ) {
    final var is = getClass().getResourceAsStream( filename );
    assertNotNull( is );

    return new BufferedReader( new InputStreamReader( is ) );
  }
}
