/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

class ParserTest {
  @Test
  void test_Conversion_Straight_Curly() {
    parse( "\"It's the 70's jack-o'-lantern\"" );
    parse( "Fish-'n'-chips!" );
    parse( "That's a 35' x 10\" yacht!" );
    parse( "He's a kinda' cat ya'll couldn't've known!" );
    parse( "'70s are Sams' faves.'" );
    parse( "'Twas and 'tis whate'er lay 'twixt dawn and dusk 'n River Styx." );
    parse( """
             But I must leave the proofs to those who 've seen 'em;
             But this I heard her say, and can't be wrong
             And all may think which way their judgments lean 'em,
             ''T is strange—the Hebrew noun which means "I am,"
             The English always use to govern d--n.'
             """ );
  }

  private void parse( final String text ) {
    final var parser = new Parser( text, System.out::println );
    parser.parse();
  }
}
