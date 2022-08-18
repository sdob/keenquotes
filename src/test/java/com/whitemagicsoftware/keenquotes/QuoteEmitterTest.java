/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.whitemagicsoftware.keenquotes.Converter.ENTITIES;
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
        tokenConsumer( output, offset )
      );

      final var actual = output.toString();
      assertEquals( expected, actual );
    } );
  }

  @Test
  void test_Emit_SingleInput_QuotesEmitted() {
    final var input = "Computer says, \"'It is mysteries---'\"";
    final var output = new StringBuilder( input );
    final var offset = new AtomicInteger( 0 );

    QuoteEmitter.analyze(
      input,
      CONTRACTIONS,
      tokenConsumer( output, offset )
    );
  }

  private static Consumer<Token> tokenConsumer(
    final StringBuilder output,
    final AtomicInteger offset
  ) {
    return token -> {
      if( !token.isAmbiguous() ) {
        final var entity = token.toString( ENTITIES );

        output.replace(
          token.began() + offset.get(),
          token.ended() + offset.get(),
          entity
        );

        offset.addAndGet( entity.length() - (token.ended() - token.began()) );
      }
    };
  }
}
