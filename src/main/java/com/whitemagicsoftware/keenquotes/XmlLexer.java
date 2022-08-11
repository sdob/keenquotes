/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.text.CharacterIterator;
import java.util.Set;

import static java.text.CharacterIterator.DONE;

/**
 * Responsible for lexing text while ignoring XML elements. The document must
 * be both sane and well-formed. This is not intended to lex documents in the
 * wild where the user has injected custom HTML. The lexer will fail if the
 * angle brackets are not balanced. Additionally, any less than or greater than
 * symbols must be encoded as {@code &lt;} or {@code &gt;}, respectively.
 */
final class XmlLexer extends Lexer {

  /**
   * Referenced when skipping text, to determine whether lexing has found
   * an untouchable element, such as preformatted text.
   */
  private final String mText;

  /**
   * Elements that indicate preformatted text, intentional straight quotes.
   */
  private final Set<String> UNTOUCHABLE = Set.of(
    "pre",
    "code",
    "tt",
    "tex",
    "kbd",
    "samp",
    "var",
    "l",
    "blockcode"
  );

  /**
   * Constructs a {@link Lexer} capable of turning text int {@link Lexeme}s.
   *
   * @param text The text to lex.
   */
  XmlLexer( final String text ) {
    super( text );

    mText = text;
  }

  /**
   * Skip (do not emit) XML tags found within the prose. This effectively hides
   * the element.
   */
  @Override
  boolean skip( final CharacterIterator i ) {
    final var match = i.current() == '<';

    if( match ) {
      final var openingTag = nextTag( i );

      if( UNTOUCHABLE.contains( openingTag.toLowerCase() ) ) {
        String closingTag;

        do {
          closingTag = nextTag( i );
        }
        while( !closingTag.endsWith( openingTag ) && i.current() != DONE );
      }
    }

    return match;
  }

  /**
   * Skips to the next greater than or less than symbol.
   *
   * @param i The {@link CharacterIterator} used to scan through the text, one
   *          character at a time.
   * @return An opening/closing tag name, or the content within the element.
   */
  private String nextTag( final CharacterIterator i ) {
    final var begin = i.getIndex();

    slurp( i, ( next, ci ) -> next != '>' && next != '<' );

    // Swallow the trailing greater than symbol.
    i.next();

    // Skip to the character following the greater than symbol.
    i.next();

    return mText.substring( begin + 1, i.getIndex() - 1 );
  }
}
