/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.whitemagicsoftware.keenquotes.ParserFactory.ParserType.PARSER_PLAIN;
import static java.lang.String.format;
import static java.lang.System.*;
import static picocli.CommandLine.Help.Ansi.Style.*;
import static picocli.CommandLine.Help.ColorScheme;

/**
 * Responsible for replacing {@link Token} instances with equivalent smart
 * quotes (or straight quotes). This will inform the caller when ambiguous
 * quotes cannot be reliably resolved.
 */
public final class KeenQuotes {
  private final Settings mSettings = new Settings( this );

  private static ColorScheme createColourScheme() {
    return new ColorScheme.Builder()
      .commands( bold )
      .options( fg_blue, bold )
      .parameters( fg_blue )
      .optionParams( italic )
      .errors( fg_red, bold )
      .stackTraces( italic )
      .build();
  }

  public void run() {
    final var settings = getSettings();
    final var contractions = createContractions( settings );

    if( settings.displayList() ) {
      out.println( contractions.toString() );
    }
    else {
      try {
        final var c = new Converter( err::println, contractions, PARSER_PLAIN );
        out.print( convert( c ) );
      } catch( final Exception ex ) {
        ex.printStackTrace( err );
      }
    }
  }

  private Contractions createContractions( final Settings settings ) {
    final var builder = new Contractions.Builder();

    builder.withBeganUnambiguous( settings.getBeganUnambiguous() );
    builder.withEndedUnambiguous( settings.getEndedUnambiguous() );
    builder.withBeganAmbiguous( settings.getBeganAmbiguous() );
    builder.withEndedAmbiguous( settings.getEndedAmbiguous() );

    return builder.build();
  }

  private String convert( final Converter converter ) throws IOException {
    return converter.apply( new String( in.readAllBytes() ) );
  }

  private Settings getSettings() {
    return mSettings;
  }

  /**
   * Returns the application version number retrieved from the application
   * properties file. The properties file is generated at build time, which
   * keys off the repository.
   *
   * @return The application version number.
   * @throws RuntimeException An {@link IOException} occurred.
   */
  private static String getVersion() {
    try {
      final var properties = loadProperties( "app.properties" );
      return properties.getProperty( "application.version" );
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  private static Properties loadProperties( final String resource )
    throws IOException {
    final var properties = new Properties();
    properties.load( getResourceAsStream( getResourceName( resource ) ) );
    return properties;
  }

  private static String getResourceName( final String resource ) {
    return format( "%s/%s", getPackagePath(), resource );
  }

  private static String getPackagePath() {
    return KeenQuotes.class.getPackageName().replace( '.', '/' );
  }

  private static InputStream getResourceAsStream( final String resource ) {
    return KeenQuotes.class.getClassLoader().getResourceAsStream( resource );
  }

  /**
   * Main application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    final var app = new KeenQuotes();
    final var parser = new CommandLine( app.getSettings() );
    parser.setColorScheme( createColourScheme() );

    final var exitCode = parser.execute( args );
    final var parseResult = parser.getParseResult();

    if( parseResult.isUsageHelpRequested() ) {
      exit( exitCode );
    }
    else if( parseResult.isVersionHelpRequested() ) {
      out.println( getVersion() );
      exit( exitCode );
    }
  }
}
