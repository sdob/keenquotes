/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.parser;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.whitemagicsoftware.keenquotes.parser.Curler.ENTITIES;
import static com.whitemagicsoftware.keenquotes.parser.Curler.replace;
import static com.whitemagicsoftware.keenquotes.texts.TestResource.readPairs;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AmbiguityResolverTest {
  private final Contractions CONTRACTIONS = new Contractions.Builder().build();

  @Test
  @Disabled
  void test_Resolve_1Pass_QuotesReplaced() throws IOException {
    test( "unambiguous-1-pass.txt" );
  }

  @Test
  @Disabled
  void test_Resolve_2Pass_QuotesReplaced() throws IOException {
    test( "unambiguous-2-pass.txt" );
  }

  @Test
  void test_Resolve_InvalidGrammar_AmbiguousRemain() throws IOException {
    test( "invalid-grammar.txt" );
  }

  void test( final String filename ) throws IOException {
    final var couplets = readPairs( filename );

    couplets.forEach( couplet -> {
      final var input = couplet.item1();
      final var output = new StringBuilder( input );
      final var expected = couplet.item2();
      final var offset = new AtomicInteger( 0 );

      AmbiguityResolver.resolve(
        input,
        CONTRACTIONS,
        replace( output, offset, ENTITIES ),
        filter -> false
      );

      final var actual = output.toString();
      assertEquals( expected, actual );
    } );
  }
}
