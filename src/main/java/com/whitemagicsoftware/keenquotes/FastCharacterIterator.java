/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.function.Function;

import static java.text.CharacterIterator.DONE;

/**
 * Iterates over a string, much like {@link CharacterIterator}, but faster.
 * This class gets 53 ops/s vs. 49 ops/s for {@link StringCharacterIterator}.
 * In comparison, using unconstrained {@link String#charAt(int)} calls yields
 * 57 ops/s.
 * <p>
 * <strong>Caution:</strong> This class offers minimal bounds checking to eke
 * out some efficiency.
 * </p>
 */
final class FastCharacterIterator {

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
    mS = s;
    mLen = s.length();
  }

  /**
   * Returns the total number of characters in the string to iterate.
   *
   * @return The string length.
   */
  public int length() {
    return mLen;
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
   * This method performs bounds checking.
   *
   * @return {@link CharacterIterator#DONE} if there are no more characters.
   */
  public char current() {
    final var pos = mPos;
    return pos < mLen ? mS.charAt( pos ) : DONE;
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
    --mPos;
  }

  /**
   * Returns the next character in the string without consuming it. Multiple
   * consecutive calls to this method will return the same value. This method
   * performs bounds checking.
   *
   * @return {@link CharacterIterator#DONE} if there are no more characters.
   */
  public char peek() {
    final var pos = mPos;
    return pos + 1 < mLen ? mS.charAt( pos + 1 ) : DONE;
  }

  /**
   * Parse all characters that match a given function.
   *
   * @param f The function that determines when skipping stops.
   */
  public void skip( final Function<Character, Boolean> f ) {
    assert f != null;

    do {
      next();
    }
    while( f.apply( current() ) );

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
