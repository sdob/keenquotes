# KeenQuotes

A Java library to convert straight quotes into curly quotes.

# Requirements

Download and install OpenJDK 16 or greater.

# Download

Download the `.jar` file from this repository.

# Run

Run the software as follows:

    java -jar keenquotes.jar < src.txt > dst.txt 2> err.txt

Where:

* `src.txt` -- Input document file that contains straight quotes.
* `dst.txt` -- Output document file that'll contain curled quotes.
* `err.txt` -- Error file that will note ambiguous conversion errors.

For help, run the software as follows:

    java -jar keenquotes.jar -h

# Software Design

The code models a lightweight natural language parser that performs a
one-pass traversal through prose to emit single quotes as curly quotes.
The algorithm follows:

1. Produce lexemes (e.g., words, numbers, and periods) using lexer.
1. Add lexemes to a 3-element circular queue, capturing either side of a
quotation mark.
1. Tokenize all unambiguous single/double quotes, primes, and apostrophes.
1. Resolve as many ambiguous quotation marks as possible.
1. Emit unresolved conversions.

See the Lexer and Parser classes for details.

# Build

Clone the repository and change to the root directory as usual, then run:

    gradle clean build

