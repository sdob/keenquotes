/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

class ParserTest {
  @Test
  void test_Conversion_Straight_Curly() {
    new Parser( "\"It's the 70's jack-o'-lantern\"").parse();
    new Parser( "Fish-'n'-chips!").parse();
    new Parser( "That's a 35' x 10\" yacht!").parse();
    //new Parser( "'70s are Sams' faves.'").parse();
  }
}
