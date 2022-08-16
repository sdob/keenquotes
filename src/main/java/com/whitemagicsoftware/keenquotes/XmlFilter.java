/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.text.CharacterIterator;
import java.util.Set;

/**
 * Responsible for lexing text while ignoring XML elements. The document must
 * be both sane and well-formed. This is not intended to lex documents in the
 * wild where the user has injected custom HTML. The lexer will fail if the
 * angle brackets are not balanced. Additionally, any less than or greater than
 * symbols must be encoded as {@code &lt;} or {@code &gt;}, respectively.
 */
public final class XmlFilter implements LexerFilter {

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

  public XmlFilter() {}

  /**
   * Skip XML tags found within the prose, which hides the elements.
   */
  @Override
  public boolean test( final FastCharacterIterator i ) {
    final var match = i.current() == '<';

    if( match ) {
      final var openingTag = nextTag( i );

      if( UNTOUCHABLE.contains( openingTag.toLowerCase() ) ) {
        String closingTag;

        do {
          closingTag = nextTag( i );
        }
        while( !closingTag.endsWith( openingTag ) );
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
  private String nextTag( final FastCharacterIterator i ) {
    final var begin = i.index();

    i.skip( next -> next != '>' && next != '<' );

    // Swallow the trailing greater than symbol.
    i.next();

    // Skip to the character following the greater than symbol.
    i.next();

    return i.substring( begin + 1, i.index() - 1 );
  }
}
