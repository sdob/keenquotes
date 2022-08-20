/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes.util;

/**
 * Responsible for associating two values.
 *
 * @param item1 The first item to associate with the second item.
 * @param item2 The second item to associate with the first item.
 * @param <T1>  The type of the first item.
 * @param <T2>  The type of the second item.
 */
public record Tuple<T1, T2>( T1 item1, T2 item2 ) {
}
