/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

import java.util.function.Predicate;

/**
 * Convenience interface definition to avoid duplication.
 */
public interface LexerFilter extends Predicate<FastCharacterIterator> {
}
