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
package com.google.gwt.dev.javac.jribble;

import com.google.gwt.dev.jjs.ast.JDeclaredType;

/**
 * A compiled unit of Jribble code. It corresponds to one Java class.
 */
public class JribbleUnit {
  private final String name;
  private final JDeclaredType syntaxTree;

  public JribbleUnit(String name, JDeclaredType syntaxTree) {
    this.name = name;
    this.syntaxTree = syntaxTree;
  }

  public String getName() {
    return name;
  }

  /**
   * Return the syntax tree. The {@link com.google.gwt.dev.jjs.ast.JNode}
   * hierarchy is mostly reused, but some node types replaced by Loose Java
   * equivalents. {@link com.google.gwt.dev.jjs.impl.GenerateJavaAST} can
   * convert this syntax tree to a proper JJS syntax tree.
   * 
   * TODO(spoon) this would make more sense returning the protobuf AST.
   * The ProtobufReader could run later and use the real classes and methods,
   * rather than using the dummy program.
   */
  public JDeclaredType getSyntaxTree() {
    return syntaxTree;
  }

  @Override
  public String toString() {
    return name;
  }
}
