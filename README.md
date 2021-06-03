# KeenQuotes

A lightweight natural language parser for converting straight quotes
into curly quotes. 

# Algorithm

The overall algorithm follows:

1. Run a lexer to produce relevant lexemes (e.g., words, numbers, and periods).
1. Add lexemes to a 3-element circular queue, capturing either side of a
straight quote.
1. Tokenize all unambiguous single/double quotes, primes, and apostrophes.
1. Resolve as many ambiguous quotation marks as possible.
1. Emit unresolved conversions.

See the Lexer and Parser classes for details.

# Build

Clone the repository and change to the root directory as usual, then run:

    gradle clean build

