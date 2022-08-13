package com.whitemagicsoftware.keenquotes;

import org.openjdk.jmh.annotations.Benchmark;

import java.text.StringCharacterIterator;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.random;
import static java.text.CharacterIterator.DONE;

@SuppressWarnings( "unused" )
public class StringIterationBenchmark {
  private final static String CHARSET =
    "ABCDEFGHIJKLM NOPQRSTUVWXYZ abcdefghijklm nopqrstuvxyz 01234 5 6789";
  public final static int STRLEN = 1024 * 1024;

  private static String generateText() {
    final var sb = new StringBuilder( STRLEN );
    final var len = CHARSET.length();

    for( var i = 0; i < STRLEN; i++ ) {
      sb.append( CHARSET.charAt( (int) (len * random()) ) );
    }

    return sb.toString();
  }

  @Benchmark
  public void test_CharAtIterator() {
    final var s = generateText();
    final var length = s.length();
    var index = 0;

    while( index < length ) {
      final var ch = s.charAt( index );
      index++;
    }

    assert index == length;
  }

  @Benchmark
  public void test_FastCharacterIterator() {
    final var s = generateText();
    final var i = new FastCharacterIterator( s );

    char c = ' ';

    while( c != DONE ) {
      i.next();
      c = i.current();
    }

    assert i.index() == STRLEN;
  }

  @Benchmark
  public void test_StringCharacterIterator() {
    final var s = generateText();
    final var i = new StringCharacterIterator( s );
    var index = 0;

    char c = ' ';

    while( c != DONE ) {
      c = i.next();
      index++;
    }

    assert index == STRLEN;
  }

  @Benchmark
  public void test_CharArrayIterator() {
    final var s = generateText();
    final var i = s.toCharArray();
    var index = 0;

    for( final var ch : i ) {
      index++;
    }

    assert index == STRLEN;
  }

  @Benchmark
  public void test_StringTokenizer() {
    final var s = generateText();
    final var i = new StringTokenizer( s, " ", true );
    var index = 0;

    while( i.hasMoreTokens() ) {
      final var token = i.nextToken();
      index += token.length();
    }

    assert index == STRLEN;
  }

  @Benchmark
  public void test_StreamIterator() {
    final var s = generateText();
    final var index = new AtomicInteger();

    s.chars().forEach( codepoint -> {
      final var ch = Character.valueOf( (char) codepoint );
      index.incrementAndGet();
    } );

    assert index.get() == STRLEN;
  }
}
