/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import com.whitemagicsoftware.keenquotes.util.FastCharacterIterator;
import org.openjdk.jmh.annotations.Benchmark;

import java.text.StringCharacterIterator;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.random;
import static java.text.CharacterIterator.DONE;

@SuppressWarnings( "unused" )
public class StringIterationBenchmark {
  private static final int STRLEN = 1024 * 1024;
  private static final String CHARSET =
    "ABCDEFGHIJKLM NOPQRSTUVWXYZ abcdefghijklm nopqrstuvxyz 01234 5 6789";

  private static final String sText;

  static {
    final var len = CHARSET.length();
    final var buffer = new StringBuilder( STRLEN );

    for( var i = 0; i < STRLEN; i++ ) {
      buffer.append( CHARSET.charAt( (int) (len * random()) ) );
    }

    sText = buffer.toString();
  }

  private static String getText() {
    return sText;
  }

  @Benchmark
  public void test_FastCharacterIterator() {
    final var s = getText();
    final var i = new FastCharacterIterator( s );
    var spaces = 0;

    char ch = ' ';

    while( (ch = i.advance()) != DONE ) {
      if( ch == ' ' ) {
        spaces++;
      }
    }

    fail( i.index(), s.length() );
  }

  //@Benchmark
  public void test_CharAtIterator() {
    final var s = getText();
    final var length = s.length();
    var index = 0;
    var spaces = 0;

    while( index < length ) {
      final var ch = s.charAt( index );

      if( ch == ' ' ) {
        spaces++;
      }

      index++;
    }

    fail( index, length );
  }

  //@Benchmark
  public void test_StringCharacterIterator() {
    final var s = getText();
    final var i = new StringCharacterIterator( s );
    var index = 0;
    var spaces = 0;

    char ch = ' ';

    while( ch != DONE ) {
      ch = i.next();

      if( ch == ' ' ) {
        spaces++;
      }

      index++;
    }

    fail( index, s.length() );
  }

  //@Benchmark
  public void test_CharArrayIterator() {
    final var s = getText();
    final var i = s.toCharArray();
    var index = 0;
    var spaces = 0;

    for( final var ch : i ) {
      if( ch == ' ' ) {
        spaces++;
      }

      index++;
    }

    fail( index, s.length() );
  }

  //@Benchmark
  public void test_StringTokenizer() {
    final var s = getText();
    final var i = new StringTokenizer( s, " ", true );
    var index = 0;
    var spaces = 0;

    while( i.hasMoreTokens() ) {
      final var token = i.nextToken();

      if( token.isBlank() ) {
        spaces++;
      }

      index += token.length();
    }

    fail( index, s.length() );
  }

  //@Benchmark
  public void test_StreamIterator() {
    final var s = getText();
    final var index = new AtomicInteger();
    final var spaces = new AtomicInteger();

    s.chars().forEach( codepoint -> {
      final var ch = Character.valueOf( (char) codepoint );

      if( ch == ' ' ) {
        spaces.incrementAndGet();
      }

      index.incrementAndGet();
    } );

    fail( index.get(), s.length() );
  }

  private static void fail( final int index, final int length ) {
    if( index != length ) {
      throw new RuntimeException( "Fail" );
    }
  }
}
