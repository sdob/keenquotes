/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.util;

import java.text.CharacterIterator;
import java.util.function.Function;

import static java.text.CharacterIterator.DONE;

/**
 * Iterates over a string, much like {@link CharacterIterator}, but faster.
 * <p>
 * <strong>Caution:</strong> This class offers minimal bounds checking to eke
 * out some efficiency.
 * </p>
 */
public final class FastCharacterIterator {

  private final String mS;
  private final int mLen;

  /**
   * Starts at 0, not guaranteed to be within bounds.
   */
  private int mPos;

  /**
   * Constructs a new iterator that can advance through the given string
   * one character at a time.
   *
   * @param s The string to iterate.
   */
  public FastCharacterIterator( final String s ) {
    assert s != null;

    mS = s;
    mLen = s.length();
  }

  /**
   * Returns the iterated index. The return value is not guaranteed to be
   * within the string bounds.
   *
   * @return The iterated index.
   */
  public int index() {
    return mPos;
  }

  /**
   * Returns the character at the currently iterated position in the string.
   * This method performs bounds checking by catching an exception because
   * usually parsing is complete when there are no more characters to iterate,
   * meaning that 99.99% of the time, explicit bounds checking is superfluous.
   *
   * @return {@link CharacterIterator#DONE} if there are no more characters.
   */
  public char current() {
    try {
      return mS.charAt( mPos );
    } catch( final Exception ex ) {
      return DONE;
    }
  }

  /**
   * Returns the next character in the string and consumes it.
   *
   * @return {@link CharacterIterator#DONE} if there are no more characters.
   */
  public char advance() {
    try {
      return mS.charAt( ++mPos );
    } catch( final Exception ex ) {
      return DONE;
    }
  }

  /**
   * Returns the next character in the string without consuming it. Multiple
   * consecutive calls to this method will return the same value.
   *
   * @return {@link CharacterIterator#DONE} if there are no more characters.
   */
  public char peek() {
    try {
      return mS.charAt( mPos + 1 );
    } catch( final Exception ex ) {
      return DONE;
    }
  }

  /**
   * Advances to the next character in the string, without bounds checking.
   */
  public void next() {
    mPos++;
  }

  /**
   * Advances to the previous character in the string, without bounds checking.
   */
  public void prev() {
    mPos--;
  }

  /**
   * Answers whether {@link #next()} followed by {@link #current()} is safe.
   *
   * @return {@code true} if there are more characters to be iterated.
   */
  public boolean hasNext() {
    return mPos < mLen;
  }

  /**
   * Parse all characters that match a given function.
   *
   * @param f The function that determines when skipping stops.
   */
  @SuppressWarnings( "StatementWithEmptyBody" )
  public void skip( final Function<Character, Boolean> f ) {
    assert f != null;

    while( f.apply( advance() ) ) ;

    // The loop always overshoots by one character.
    prev();
  }

  /**
   * Creates a string from a subset of consecutive characters in the string
   * being iterated. The calling class is responsible for bounds-checking.
   *
   * @param began The starting index, must be greater than or equal to zero
   *              and less than or equal to {@code ended}.
   * @param ended The ending index, must be less than the string length.
   * @return A substring of the iterated string.
   * @throws IndexOutOfBoundsException Either or both parameters exceed the
   *                                   string's boundaries.
   */
  public String substring( final int began, final int ended ) {
    assert began >= 0;
    assert began <= ended;
    assert ended < mLen;

    return mS.substring( began, ended );
  }
}
