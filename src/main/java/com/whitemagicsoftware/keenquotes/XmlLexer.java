/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.text.CharacterIterator;

import static com.whitemagicsoftware.keenquotes.Lexeme.createLexeme;
import static com.whitemagicsoftware.keenquotes.LexemeType.TAG;

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

  @Override
  Lexeme preprocess( final CharacterIterator i ) {
    final var began = i.getIndex();
    final var curr = i.current();
    Lexeme lexeme = null;

    if( curr == '<' ) {
      slurp( i, ( next, ci ) -> next != '>' );
      i.next();
      lexeme = createLexeme( TAG, began, i.getIndex() );
      i.next();
    }

    return lexeme;
  }
}
