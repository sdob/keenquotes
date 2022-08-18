/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.whitemagicsoftware.keenquotes;

/**
 * Responsible for identifying a {@link Tree}'s subcomponent. This marker
 * interface helps unify a {@link Tree}'s child elements, allowing the elements
 * to be added and maintained in insertion order.
 */
public interface Stem {
  /**
   * Returns this object as a well-formed XML document fragment.
   *
   * @return Part of an XML document.
   */
  String toXml();
}
