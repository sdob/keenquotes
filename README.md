# KeenQuotes

A Java library to convert straight quotes into curly quotes.

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

