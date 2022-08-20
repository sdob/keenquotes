# Build

This document describes how to build the application and standalone library.

## Requirements

Building the software requires the following third-party programs:

* [git](https://git-scm.com)
* [OpenJDK 17](https://bell-sw.com/pages/downloads)
* [Gradle](https://gradle.org)

## Application

Build the application as follows:

    git clone https://github.com/DaveJarvis/keenquotes.git
    cd keenquotes
    gradle clean build

Find the application at:

    build/lib/keenquotes.jar

## Library

To build a library for use with other software applications, run:

    gradle clean lib

Find the library at:

    build/lib/keenquotes.jar

