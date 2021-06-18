/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
  name = "KeenQuotes",
  mixinStandardHelpOptions = true,
  description = "Converts straight quotes to curly quotes."
)
@SuppressWarnings( {"FieldMayBeFinal", "CanBeFinal"} )
public final class Settings implements Callable<Integer> {
  /**
   * Main executable class.
   */
  private final KeenQuotes mMain;

//  /**
//   * List of unambiguous contractions having leading apostrophes.
//   */
//  @CommandLine.Option(
//    names = {"-ub", "--unamb-began"},
//    description =
//      "Contractions to treat as unambiguous (e.g., cause,bout)",
//    paramLabel = "words"
//  )
//  private String[] mUnambiguousBegan;
//
//  /**
//   * List of unambiguous contractions having lagging apostrophes.
//   */
//  @CommandLine.Option(
//    names = {"-ue", "--unamb-ended"},
//    description =
//      "Contractions to treat as unambiguous (e.g., frien,thinkin)",
//    paramLabel = "words"
//  )
//  private String[] mUnambiguousEnded;
//
//  /**
//   * List of ambiguous contractions having leading apostrophes.
//   */
//  @CommandLine.Option(
//    names = {"-ab", "--amb-began"},
//    description =
//      "Contractions to treat as ambiguous (e.g., sup,kay)",
//    paramLabel = "words"
//  )
//  private String[] mAmbiguousBegan;
//
//  /**
//   * List of ambiguous contractions having lagging apostrophes.
//   */
//  @CommandLine.Option(
//    names = {"-ae", "--amb-ended"},
//    description =
//      "Contractions to treat as ambiguous (e.g., gi,o)",
//    paramLabel = "words"
//  )
//  private String[] mAmbiguousEnded;
//
  /**
   * Display default values.
   */
  @CommandLine.Option(
    names = {"-l", "--list"},
    description = "List all ambiguous and unambiguous contractions"
  )
  private boolean mDisplayList;

  public Settings( final KeenQuotes main ) {
    assert main != null;
    mMain = main;
  }

  /**
   * Answers whether the contractions listings should be displayed.
   *
   * @return {@code true} to list the contractions.
   */
  public boolean displayList() {
    return mDisplayList;
  }

  /**
   * Invoked after the command-line arguments are parsed to launch the
   * application.
   *
   * @return Exit level zero.
   */
  @Override
  public Integer call() {
    mMain.run();
    return 0;
  }
}
