/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Placeholder for various types of contractions.
 */
public class Contractions {

  private final Builder mBuilder;

  private Contractions( final Builder builder ) {
    assert builder != null;
    mBuilder = builder;
  }

  /**
   * Allows constructing a list of custom contractions.
   */
  @SuppressWarnings( "unused" )
  public static class Builder {
    private final Set<String> mBeganUnambiguous = new HashSet<>();
    private final Set<String> mEndedUnambiguous = new HashSet<>();
    private final Set<String> mBeganAmbiguous = new HashSet<>();
    private final Set<String> mEndedAmbiguous = new HashSet<>();

    public void withBeganUnambiguous( final Set<String> words ) {
      mBeganUnambiguous.addAll( words );
    }

    public void withEndedUnambiguous( final Set<String> words ) {
      mEndedUnambiguous.addAll( words );
    }

    public void withBeganAmbiguous( final Set<String> words ) {
      mBeganAmbiguous.addAll( words );
    }

    public void withEndedAmbiguous( final Set<String> words ) {
      mEndedAmbiguous.addAll( words );
    }

    /**
     * Constructs a new set of {@link Contractions} that can be configured
     * using this {@link Builder} instance.
     *
     * @return {@link Contractions} suitable for use with parsing text.
     */
    public Contractions build() {
      mBeganUnambiguous.addAll( from( mBeganUnambiguous, BEGAN_UNAMBIGUOUS ) );
      mEndedUnambiguous.addAll( from( mEndedUnambiguous, ENDED_UNAMBIGUOUS ) );
      mBeganAmbiguous.addAll( from( mBeganAmbiguous, BEGAN_AMBIGUOUS ) );
      mEndedAmbiguous.addAll( from( mEndedAmbiguous, ENDED_AMBIGUOUS ) );

      return new Contractions( this );
    }

    /**
     * This returns the {@code fallback} {@link Set} if {@code src} is empty;
     * otherwise, this returns the empty {@link Set}.
     *
     * @param src      A set of contractions, possibly empty.
     * @param fallback The default values to use if {@code src} is empty.
     * @param <T>      The type of data used by both {@link Set}s.
     * @return An empty {@link Set} if the {@code src} contains at least one
     * element; otherwise, this will return {@code fallback}.
     */
    private static <T> Set<T> from( final Set<T> src, final Set<T> fallback ) {
      assert src != null;
      assert fallback != null;
      return src.isEmpty() ? fallback : emptySet();
    }
  }

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
  public boolean beganUnambiguously( final String word ) {
    assert word != null;
    return getBeganUnambiguous().contains( word.toLowerCase() );
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
  public boolean beganAmbiguously( final String word ) {
    assert word != null;
    return getBeganAmbiguous().contains( word.toLowerCase() );
  }

  public boolean endedUnambiguously( final String word ) {
    assert word != null;
    return getEndedUnambiguous().contains( word.toLowerCase() );
  }

  public boolean endedAmbiguously( final String word ) {
    assert word != null;
    final var check = word.toLowerCase();

    // Ensure that 'n' isn't matched for ambiguity by enforcing length, yet
    // allow o' to match because 'a sentence can end with the letter o'.
    return getEndedAmbiguous().contains( check ) ||
      check.endsWith( "s" ) || check.endsWith( "z" ) ||
      check.endsWith( "x" ) || (check.length() > 1 && check.endsWith( "n" ));
  }

  private Set<String> getBeganUnambiguous() {
    return mBuilder.mBeganUnambiguous;
  }

  private Set<String> getEndedUnambiguous() {
    return mBuilder.mEndedUnambiguous;
  }

  private Set<String> getBeganAmbiguous() {
    return mBuilder.mBeganAmbiguous;
  }

  private Set<String> getEndedAmbiguous() {
    return mBuilder.mEndedAmbiguous;
  }

  /**
   * Words having a straight apostrophe that cannot be mistaken for an
   * opening single quote.
   */
  private static final Set<String> BEGAN_UNAMBIGUOUS = Set.of(
    "aporth",
    "boutcha",
    "boutchu",
    "cept",
    "dillo",
    "em",
    "fraid",
    "gainst",
    "n",
    "neath",
    "nother",
    "onna",
    "onna'",
    "pon",
    "s",
    "sblood",
    "scuse",
    "sfar",
    "sfoot",
    "t",
    "taint",
    "tain",
    "til",
    "tis",
    "tisn",
    "tshall",
    "twas",
    "twasn",
    "tween",
    "twere",
    "tweren",
    "twixt",
    "twon",
    "twou",
    "twould",
    "twouldn",
    "ve"
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
    // give|martial arts garment
    "gi",
    // in|I
    "i",
    // of|letter o
    "o"
  );

  private static final Set<String> ENDED_UNAMBIGUOUS = Set.of(
    // and
    "an",
    // for/before
    "fo",
    // friend
    "frien",
    // just
    "jus",
    // lord
    "lor",
    // myself
    "masel",
    // old
    "ol",
    // San (Francisco)
    "Sa",
    // shift
    "shif",
    // the
    "th",
    // what
    "wha",
    // world
    "worl",
    // Top ~500 common -ing words as English contractions.
    "acceptin",
    "accompanyin",
    "accordin",
    "accountin",
    "achievin",
    "acquirin",
    "actin",
    "addin",
    "addressin",
    "adjoinin",
    "adoptin",
    "advancin",
    "advertisin",
    "affectin",
    "agin",
    "allowin",
    "amazin",
    "analyzin",
    "answerin",
    "anythin",
    "appearin",
    "applyin",
    "approachin",
    "arguin",
    "arisin",
    "arrivin",
    "askin",
    "assessin",
    "assumin",
    "attackin",
    "attemptin",
    "attendin",
    "avoidin",
    "bankin",
    "bargainin",
    "bearin",
    "beatin",
    "becomin",
    "beginnin",
    "bein",
    "believin",
    "belongin",
    "bendin",
    "bindin",
    "bleedin",
    "blessin",
    "blowin",
    "boilin",
    "borrowin",
    "breakin",
    "breathin",
    "breedin",
    "bringin",
    "broadcastin",
    "buildin",
    "burnin",
    "buyin",
    "calculatin",
    "callin",
    "carryin",
    "castin",
    "causin",
    "ceilin",
    "challengin",
    "changin",
    "checkin",
    "choosin",
    "claimin",
    "cleanin",
    "clearin",
    "climbin",
    "closin",
    "clothin",
    "collectin",
    "combinin",
    "comin",
    "commandin",
    "comparin",
    "compellin",
    "competin",
    "computin",
    "concernin",
    "concludin",
    "conditionin",
    "conductin",
    "conflictin",
    "connectin",
    "considerin",
    "consistin",
    "constructin",
    "consultin",
    "consumin",
    "containin",
    "continuin",
    "contractin",
    "contributin",
    "controllin",
    "convincin",
    "cookin",
    "coolin",
    "copin",
    "correspondin",
    "counselin",
    "countin",
    "couplin",
    "coverin",
    "creatin",
    "crossin",
    "cryin",
    "cuttin",
    "dancin",
    "darlin",
    "datin",
    "dealin",
    "decidin",
    "declarin",
    "declinin",
    "decreasin",
    "definin",
    "demandin",
    "denyin",
    "dependin",
    "descendin",
    "describin",
    "designin",
    "destroyin",
    "determinin",
    "developin",
    "differin",
    "dinin",
    "directin",
    "discussin",
    "distinguishin",
    "disturbin",
    "dividin",
    "doin",
    "drawin",
    "dressin",
    "drinkin",
    "drivin",
    "droppin",
    "dryin",
    "durin",
    "dwellin",
    "dyin",
    "eatin",
    "editin",
    "emergin",
    "employin",
    "enablin",
    "encouragin",
    "endin",
    "engagin",
    "engineerin",
    "enjoyin",
    "enterin",
    "establishin",
    "evaluatin",
    "evenin",
    "everythin",
    "examinin",
    "exceedin",
    "excitin",
    "excludin",
    "existin",
    "expandin",
    "expectin",
    "experiencin",
    "explainin",
    "explorin",
    "expressin",
    "extendin",
    "facin",
    "failin",
    "fallin",
    "farmin",
    "fascinatin",
    "feedin",
    "feelin",
    "fightin",
    "filin",
    "fillin",
    "financin",
    "findin",
    "firin",
    "fishin",
    "fittin",
    "fixin",
    "floatin",
    "flowin",
    "flyin",
    "focusin",
    "followin",
    "forcin",
    "foregoin",
    "formin",
    "forthcomin",
    "foundin",
    "freezin",
    "fuckin",
    "functionin",
    "fundin",
    "gainin",
    "gatherin",
    "generatin",
    "gettin",
    "givin",
    "goin",
    "governin",
    "grantin",
    "growin",
    "hackin",
    "handlin",
    "hangin",
    "happenin",
    "havin",
    "headin",
    "healin",
    "hearin",
    "heatin",
    "helpin",
    "hidin",
    "holdin",
    "hopin",
    "housin",
    "huntin",
    "identifyin",
    "imagin",
    "implementin",
    "imposin",
    "improvin",
    "includin",
    "increasin",
    "indicatin",
    "interestin",
    "interpretin",
    "introducin",
    "involvin",
    "joinin",
    "judgin",
    "keepin",
    "killin",
    "knowin",
    "lackin",
    "landin",
    "lastin",
    "laughin",
    "layin",
    "leadin",
    "leanin",
    "learnin",
    "leavin",
    "lettin",
    "liftin",
    "lightin",
    "lightnin",
    "limitin",
    "listenin",
    "listin",
    "livin",
    "loadin",
    "lookin",
    "losin",
    "lovin",
    "lowerin",
    "lyin",
    "maintainin",
    "makin",
    "managin",
    "manufacturin",
    "mappin",
    "marketin",
    "markin",
    "matchin",
    "meanin",
    "measurin",
    "meetin",
    "meltin",
    "minin",
    "misleadin",
    "missin",
    "mixin",
    "modelin",
    "monitorin",
    "mornin",
    "movin",
    "neighborin",
    "nothin",
    "notin",
    "notwithstandin",
    "nursin",
    "observin",
    "obtainin",
    "occurrin",
    "offerin",
    "offsprin",
    "ongoin",
    "openin",
    "operatin",
    "opposin",
    "orderin",
    "organizin",
    "outstandin",
    "overwhelmin",
    "packin",
    "paintin",
    "parkin",
    "participatin",
    "passin",
    "payin",
    "pendin",
    "performin",
    "pickin",
    "pissin",
    "placin",
    "plannin",
    "plantin",
    "playin",
    "pleasin",
    "pointin",
    "possessin",
    "preachin",
    "precedin",
    "preparin",
    "presentin",
    "preservin",
    "pressin",
    "prevailin",
    "preventin",
    "pricin",
    "printin",
    "proceedin",
    "processin",
    "producin",
    "programmin",
    "promisin",
    "promotin",
    "protectin",
    "providin",
    "provin",
    "publishin",
    "pullin",
    "purchasin",
    "pursuin",
    "pushin",
    "puttin",
    "questionin",
    "rangin",
    "ratin",
    "reachin",
    "readin",
    "reasonin",
    "receivin",
    "recognizin",
    "recordin",
    "reducin",
    "referrin",
    "reflectin",
    "refusin",
    "regardin",
    "regulatin",
    "relatin",
    "remainin",
    "rememberin",
    "removin",
    "renderin",
    "repeatin",
    "replacin",
    "reportin",
    "representin",
    "requirin",
    "respectin",
    "respondin",
    "restin",
    "resultin",
    "returnin",
    "revealin",
    "ridin",
    "risin",
    "rulin",
    "runnin",
    "sailin",
    "samplin",
    "satisfyin",
    "savin",
    "sayin",
    "scatterin",
    "schoolin",
    "screenin",
    "searchin",
    "securin",
    "seein",
    "seekin",
    "selectin",
    "sellin",
    "sendin",
    "separatin",
    "servin",
    "settin",
    "settlin",
    "sewin",
    "shakin",
    "shapin",
    "sharin",
    "shiftin",
    "shinin",
    "shippin",
    "shittin",
    "shootin",
    "shoppin",
    "showin",
    "singin",
    "sinkin",
    "sittin",
    "sleepin",
    "smilin",
    "smokin",
    "spankin",
    "solvin",
    "somethin",
    "speakin",
    "spellin",
    "spendin",
    "spinnin",
    "spittin",
    "spreadin",
    "standin",
    "starin",
    "startin",
    "statin",
    "stayin",
    "stealin",
    "sterlin",
    "stimulatin",
    "stirrin",
    "stoppin",
    "strengthenin",
    "stretchin",
    "strikin",
    "strugglin",
    "studyin",
    "succeedin",
    "sufferin",
    "suggestin",
    "supplyin",
    "supportin",
    "surprisin",
    "surroundin",
    "survivin",
    "sweepin",
    "swellin",
    "swimmin",
    "switchin",
    "takin",
    "talkin",
    "teachin",
    "tellin",
    "testin",
    "thinkin",
    "threatenin",
    "throwin",
    "timin",
    "touchin",
    "tradin",
    "trainin",
    "travelin",
    "treatin",
    "tremblin",
    "tryin",
    "turnin",
    "underlyin",
    "understandin",
    "undertakin",
    "unwillin",
    "usin",
    "varyin",
    "viewin",
    "visitin",
    "votin",
    "waitin",
    "walkin",
    "wanderin",
    "wantin",
    "warnin",
    "washin",
    "watchin",
    "wearin",
    "weddin",
    "whackin",
    "willin",
    "windin",
    "winnin",
    "wishin",
    "wonderin",
    "workin",
    "writin",
    "yieldin"
  );
}
