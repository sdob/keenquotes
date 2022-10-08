/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.app;

import com.whitemagicsoftware.keenquotes.parser.Contractions;
import com.whitemagicsoftware.keenquotes.parser.Curler;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static picocli.CommandLine.Help.Ansi.Style.*;
import static picocli.CommandLine.Help.ColorScheme;

/**
 * Responsible for replacing straight quotes with equivalent smart quotes (or
 * straight quotes). This will inform the caller when ambiguous quotes cannot
 * be reliably resolved.
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
        final var filter = settings.filterXml();
        final var c = new Curler( contractions, filter );

        out.print( convert( c ) );
      } catch( final Exception ex ) {
        ex.printStackTrace( err );
      }
    }
  }

  private Contractions createContractions( final Settings settings ) {
    return new Contractions.Builder()
      .withBeganUnambiguous( settings.getBeganUnambiguous() )
      .withEndedUnambiguous( settings.getEndedUnambiguous() )
      .withBeganAmbiguous( settings.getBeganAmbiguous() )
      .withEndedAmbiguous( settings.getEndedAmbiguous() )
      .build();
  }

  private String convert( final Curler curler ) throws IOException {
    return curler.apply( new String( in.readAllBytes(), UTF_8 ) );
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
      final var properties = loadProperties( "version.properties" );
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
