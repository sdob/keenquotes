/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.texts;

import com.whitemagicsoftware.keenquotes.util.Tuple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Responsible for opening a test resource file.
 */
public class TestResource {

  private TestResource() {}

  /**
   * Opens a text file for reading. Callers must close the resource.
   *
   * @param filename The file to open.
   * @return An instance of {@link BufferedReader} that can be used to
   * read all the lines in the file.
   */
  public static BufferedReader open( final String filename ) {
    return new TestResource().openResource( filename );
  }

  /**
   * Reads a set of coupled lines, which represent the input and expected
   * results after encoding the quotation marks. This loads the entire file
   * into memory.
   *
   * @param filename The name of the file to parse.
   * @return The set of input and expected results.
   * @throws IOException Could not open the file for reading.
   */
  public static Collection<Tuple<String, String>> readPairs(
    final String filename
  ) throws IOException {
    final var result = new LinkedList<Tuple<String, String>>();

    try( final var reader = open( filename ) ) {
      String line;
      String testLine = "";
      String expected = "";

      while( (line = reader.readLine()) != null ) {
        if( line.startsWith( "#" ) || line.isBlank() ) {continue;}

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

        result.add( new Tuple<>( testLine, expected ) );

        testLine = "";
        expected = "";
      }
    }

    return result;
  }

  private BufferedReader openResource( final String filename ) {
    final var is = getClass().getResourceAsStream( filename );

    // If the resource cannot be found, throw a developer-friendly exception.
    if( is == null ) {
      Exception ex;

      try {
        final var resource = getClass().getResource( "." );
        final var path = resource == null
          ? filename
          : Path.of( resource.toURI().getPath(), filename ).toString();
        ex = new FileNotFoundException( path );
      } catch( final Exception _ex ) {
        ex = _ex;
      }

      // We don't need no steenkin' checked exceptions.
      throw new RuntimeException( ex );
    }

    return new BufferedReader( new InputStreamReader( is ) );
  }

  private static String unescapeEol( final String s ) {
    return String.join( "\n", s.split( "\\\\n" ) );
  }
}
