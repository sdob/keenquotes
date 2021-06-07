/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.Set;

/**
 * Placeholder for various types of contractions.
 */
public class Contractions {
  /**
   * Words having a straight apostrophe that cannot be mistaken for an
   * opening single quote.
   */
  private static final Set<String> BEGAN_UNAMBIGUOUS = Set.of(
//    "aporth", "boutcha", "boutchu", "cept", "dillo", "e", "e'll", "e's",
//    "fraid", "gainst", "n", "neath", "nother", "onna", "onna'", "pon", "s",
//    "sblood", "scuse", "sfar", "sfoot", "t", "taint", "tain't", "til", "tis",
//    "tisn't", "tshall", "twas", "twasn't", "tween", "twere", "tweren't",
//    "twixt", "twon't", "twou'd", "twou'dn't", "twould", "twouldn't", "ve"

    "aporth", "boutcha", "boutchu", "cept", "dillo", "fraid", "gainst",
    "n", "neath", "nother", "onna", "onna'", "pon", "s", "sblood", "scuse",
    "sfar", "sfoot", "t", "taint", "tain", "til", "tis", "tisn", "tshall",
    "twas", "twasn", "tween", "twere", "tweren", "twixt", "twon", "twou",
    "twould", "twouldn", "ve"
  );

  /**
   * Words having a straight apostrophe that may be either part of a
   * contraction or a word that stands alone beside an opening single quote.
   */
  private static final Set<String> BEGAN_AMBIGUOUS = Set.of(
    // about|boxing match
    "bout",
    // because|causal
    "cause",
    // what you|choo choo train
    "choo",
    // he|e pluribus unum
    "e",
    // them|emily
    "em",
    // here|earlier
    "ere",
    // afro|to and fro
    "fro",
    // whore|ho ho!
    "ho",
    // okay|letter K
    "kay",
    // lo|lo and behold
    "lo",
    // are|regarding
    "re",
    // what's up|to sup
    "sup",
    // it will|twill fabric
    "twill",
    // them|utterance
    "um",
    // is that|Iranian village
    "zat"
  );

  private static final Set<String> ENDED_AMBIGUOUS = Set.of(
    // he|a
    "a",
    // and|an
    "an",
    // give|martial arts garment
    "gi",
    // in|I
    "i",
    // of|letter o
    "o"
  );

  private static final Set<String> ENDED_UNAMBIGUOUS = Set.of(
    // old
    "ol",
    // the
    "th"
  );

  /**
   * Answers whether the given word is a contraction that always starts
   * with an apostrophe. The comparison is case insensitive. This must
   * only be called when a straight quote is followed by a word.
   *
   * @param word The word to compare against the list of known unambiguous
   *             contractions.
   * @return {@code true} when the given word is in the set of unambiguous
   * contractions.
   */
  public static boolean contractionBeganUnambiguously( final String word ) {
    assert word != null;
    return BEGAN_UNAMBIGUOUS.contains( word.toLowerCase() );
  }

  /**
   * Answers whether the given word could be a contraction but is also a
   * valid word in non-contracted form.
   *
   * @param word The word to compare against the list of known ambiguous
   *             contractions.
   * @return {@code true} when the given word is in the set of ambiguous
   * contractions.
   */
  public static boolean contractionBeganAmbiguously( final String word ) {
    assert word != null;
    return BEGAN_AMBIGUOUS.contains( word.toLowerCase() );
  }

  public static boolean contractionEndedAmbiguously( final String word ) {
    assert word != null;
    final var check = word.toLowerCase();

    return ENDED_AMBIGUOUS.contains( check ) || check.endsWith( "s" ) ||
      check.endsWith( "n" ) || check.endsWith( "z" ) ||
      check.endsWith( "x" ) || check.endsWith( "ch" );
  }

  public static boolean contractionEndedUnambiguously( final String word ) {
    assert word != null;
    return ENDED_UNAMBIGUOUS.contains( word.toLowerCase() );
  }
}
