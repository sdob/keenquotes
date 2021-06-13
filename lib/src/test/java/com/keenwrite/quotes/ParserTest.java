/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.quotes.TokenType.QUOTE_APOSTROPHE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that all unambiguous apostrophes are emitted once.
 */
class ParserTest {
  private final static Map<String, Map<TokenType, Integer>> TEST_CASES =
    Map.of(
      "That's a 35'×10\" yacht!",
      Map.of( QUOTE_APOSTROPHE, 1 ),
      "It's the 80's...",
      Map.of( QUOTE_APOSTROPHE, 2 ),
      "Fish-'n'-chips!",
      Map.of( QUOTE_APOSTROPHE, 2 ),
      "She's a cat ya'll couldn't've known!",
      Map.of( QUOTE_APOSTROPHE, 4 ),
      "'Twas and 'tis whate'er lay 'twixt dawn and dusk 'n River Styx.",
      Map.of( QUOTE_APOSTROPHE, 5 ),
      """
             But I must leave the proofs to those who 've seen 'em;
             But this I heard her say, and can't be wrong
             And all may think which way their judgments lean 'em,
             ''T is strange---the Hebrew noun which means "I am,"
             The English always use to govern d--n.'
        """,
      Map.of( QUOTE_APOSTROPHE, 5 )
    );

  @Test
  void test_Conversion_Straight_Curly() {
    for( final var entry : TEST_CASES.entrySet() ) {
      parse( entry.getKey(), entry.getValue() );
    }
  }

  private void parse( final String text, final Map<TokenType, Integer> tally ) {
    final var parser = new Parser( text );
    final var actual = new HashMap<TokenType, Integer>();

    parser.parse(
      ( token ) -> actual.merge( token.getType(), 1, Integer::sum )
    );

    for( final var expectedEntry : tally.entrySet() ) {
      final var expectedCount = expectedEntry.getValue();
      final var actualCount = actual.get( expectedEntry.getKey() );

      assertEquals( expectedCount, actualCount, text );
    }
  }
}
