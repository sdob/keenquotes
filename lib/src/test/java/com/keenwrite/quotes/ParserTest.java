/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.junit.jupiter.api.Test;

class ParserTest {
  @Test
  void test_Conversion_Straight_Curly() {
    final var parser = new Parser( "Salut tout le monde!\nÇa va?");
    parser.parse();
    System.out.println();
  }
}
