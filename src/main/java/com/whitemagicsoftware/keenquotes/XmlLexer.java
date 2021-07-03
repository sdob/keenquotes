/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.text.CharacterIterator;

/**
 * Responsible for lexing text while ignoring XML elements. The document must
 * be both sane and well-formed. This is not intended to lex documents in the
 * wild where the user has injected custom HTML. The lexer will fail if the
 * angle brackets are not balanced. Additionally, any less than or greater than
 * symbols must be encoded as {@code &lt;} or {@code &gt;}, respectively.
 */
final class XmlLexer extends Lexer {
  /**
   * Constructs a {@link Lexer} capable of turning text int {@link Lexeme}s.
   *
   * @param text The text to lex.
   */
  XmlLexer( final String text ) {
    super( text );
  }

  /**
   * Skip (do not emit) XML tags found within the prose. This effectively hides
   * the element.
   *
   * @param i The {@link CharacterIterator} used to scan through the text, one
   *          character at a time.
   */
  @Override
  boolean skip( final CharacterIterator i ) {
    final boolean match = i.current() == '<';

    if( match ) {
      slurp( i, ( next, ci ) -> next != '>' );

      // Swallow the trailing greater than symbol.
      i.next();

      // Skip to the character following the greater than symbol.
      i.next();
    }

    return match;
  }
}
