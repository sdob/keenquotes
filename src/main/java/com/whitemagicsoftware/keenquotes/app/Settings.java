/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.app;

import com.whitemagicsoftware.keenquotes.lex.XmlFilter;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

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

  /**
   * List of unambiguous contractions having leading apostrophes.
   */
  @CommandLine.Option(
    names = {"-ub", "--unamb-began"},
    description =
      "Contraction to treat as unambiguous (e.g., cause, bout)",
    paramLabel = "word"
  )
  private String[] mBeganUnambiguous;

  /**
   * List of unambiguous contractions having lagging apostrophes.
   */
  @CommandLine.Option(
    names = {"-ue", "--unamb-ended"},
    description =
      "Contraction to treat as unambiguous (e.g., frien, thinkin)",
    paramLabel = "word"
  )
  private String[] mEndedUnambiguous;

  /**
   * List of ambiguous contractions having leading apostrophes.
   */
  @CommandLine.Option(
    names = {"-ab", "--amb-began"},
    description =
      "Contraction to treat as ambiguous (e.g., sup, kay)",
    paramLabel = "word"
  )
  private String[] mBeganAmbiguous;

  /**
   * List of ambiguous contractions having lagging apostrophes.
   */
  @CommandLine.Option(
    names = {"-ae", "--amb-ended"},
    description =
      "Contraction to treat as ambiguous (e.g., gi, o)",
    paramLabel = "word"
  )
  private String[] mEndedAmbiguous;

  /**
   * Display default values.
   */
  @CommandLine.Option(
    names = {"-l", "--list"},
    description = "List all ambiguous and unambiguous contractions"
  )
  private boolean mDisplayList;

  /**
   * Encode quotation marks using HTML entities.
   */
  @CommandLine.Option(
    names = {"-e", "--entities"},
    description = "Encode quotation marks using HTML entities"
  )
  private boolean mEntities;

  /**
   * Enable the {@link XmlFilter}.
   */
  @CommandLine.Option(
    names = {"-x", "--xml", "--html", "--xhtml"},
    description = "Convert quotation marks within XML or HTML documents"
  )
  private boolean mFilterXml;

  public Settings( final KeenQuotes main ) {
    assert main != null;
    mMain = main;
  }

  /**
   * Answers whether the contractions listings should be displayed.
   *
   * @return {@code true} to list the contractions.
   */
  boolean displayList() { return mDisplayList; }

  /**
   * Answers whether quotation marks within XML elements are ignored.
   *
   * @return {@code true} to honour quotation marks inside XML elements.
   */
  boolean filterXml() { return mFilterXml; }

  /**
   * Answers whether entities must be encoded using HTML entities.
   *
   * @return {@code true} to encode quotation marks using HTML entities.
   */
  boolean entities() { return mEntities; }

  List<String> getBeganUnambiguous() {
    return nullSafe( mBeganUnambiguous );
  }

  List<String> getEndedUnambiguous() {
    return nullSafe( mEndedUnambiguous );
  }

  List<String> getBeganAmbiguous() {
    return nullSafe( mBeganAmbiguous );
  }

  List<String> getEndedAmbiguous() {
    return nullSafe( mEndedAmbiguous );
  }

  private List<String> nullSafe( final String[] words ) {
    return words == null ? emptyList() : asList( words );
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
