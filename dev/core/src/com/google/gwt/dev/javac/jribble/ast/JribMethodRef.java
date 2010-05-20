/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.javac.jribble.ast;

import java.io.Serializable;

/**
 * A reference to a method.
 */
public class JribMethodRef implements Serializable {
  private final String methodJsniSignature;
  private final String typeName;

  public JribMethodRef(String typeName, String methodJsniSignature) {
    this.typeName = typeName;
    this.methodJsniSignature = methodJsniSignature;
  }

  public String getMethodJsniSignature() {
    return methodJsniSignature;
  }

  public String getTypeName() {
    return typeName;
  }

  @Override
  public String toString() {
    return typeName + "." + methodJsniSignature;
  }
}
