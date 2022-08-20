# KeenQuotes

KeenQuotes converts straight quotes into curly quotes.

# Requirements

Download and install Standard JRE 17:

* [Java 17](https://bell-sw.com/pages/downloads/#/java-17-lts) or newer.

# Download

Download the application:

* [keenquotes.jar](https://github.com/DaveJarvis/keenquotes/releases/latest/download/keenquotes.jar)

# Run

Run the software from the command-line as follows:

    java -jar keenquotes.jar < src.txt > dst.txt 2> err.txt

Where:

* `src.txt` -- Input document file that contains straight quotes.
* `dst.txt` -- Output document file that'll contain curled quotes.
* `err.txt` -- Error file that will note ambiguous conversion errors.

For help, run the software as follows:

    java -jar keenquotes.jar -h

# Software Design

The software models a lightweight natural language parser that performs a
multi-stage traversal through prose:

1. Tokenize lexemes (e.g., words, numbers, and periods).
1. Emit all quotation mark characters (single, double, primes, etc.).
1. Build an abstract syntax tree representing nested quotations.
1. Resolve as many ambiguous straight single quotes as possible.

See the source code for details.

# Build

See [BUILD.md](BUILD.md) for detailed build instructions.

