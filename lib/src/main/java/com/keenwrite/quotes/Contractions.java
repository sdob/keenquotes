package com.keenwrite.quotes;

import java.util.Set;

public class Contractions {
  /**
   * Words having a straight apostrophe that cannot be mistaken for an
   * opening single quote.
   */
  private static final Set<String> BEGAN_UNAMBIGUOUS = Set.of(
    "aporth", "boutcha", "boutchu", "cept", "dillo", "e'll", "fraid",
    "gainst", "n", "neath", "nother", "onna", "onna'", "pon", "s", "sblood",
    "scuse", "sfar", "sfoot", "t", "taint", "tain't", "til", "tis", "tisn't",
    "tshall", "twas", "twasn't", "tween", "twere", "tweren't", "twixt",
    "twon't", "twou'd", "twou'dn't", "twould", "twouldn't", "ve"
  );

  /**
   * Words having a straight apostrophe that may be either part of a
   * contraction or a word that stands alone beside an opening single quote.
   */
  private static final Set<String> BEGAN_AMBIGUOUS = Set.of(
    "bout", "choo", "ere", "e", "e's", "fro", "ho", "kay", "lo",
    "re", "sup", "twill", "um", "zat"
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
  public static boolean beginsUnambiguously( final String word ) {
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
  public static boolean beginsAmbiguously( final String word ) {
    assert word != null;
    return BEGAN_AMBIGUOUS.contains( word.toLowerCase() );
  }
}
