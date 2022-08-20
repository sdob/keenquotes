/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.whitemagicsoftware.keenquotes.Curler.ENTITIES;
import static com.whitemagicsoftware.keenquotes.TestResource.readPairs;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuoteEmitterTest {
  private final Contractions CONTRACTIONS = new Contractions.Builder().build();

  @Test
  void test_Emit_MultipleInputs_QuotesEmitted() throws IOException {
    final var couplets = readPairs(
      "unambiguous-1-pass.txt" );

    couplets.forEach( couplet -> {
      final var input = couplet.item1();
      final var output = new StringBuilder( input );
      final var expected = couplet.item2();
      final var offset = new AtomicInteger( 0 );

      QuoteEmitter.analyze(
        input,
        CONTRACTIONS,
        Curler.swap( output, offset, ENTITIES ),
        filter -> false
      );

      final var actual = output.toString();
      assertEquals( expected, actual );
    } );
  }
}
