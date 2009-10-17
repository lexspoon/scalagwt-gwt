package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.HasSourceInfo;
import com.google.gwt.dev.jjs.InternalCompilerException;

/**
 * This exception indicates that an unexpected node subtype was encountered.
 */
class UnknownNodeSubtype extends InternalCompilerException {
  public UnknownNodeSubtype(HasSourceInfo node) {
    super(node, "unknown node subtype", null);
  }
}