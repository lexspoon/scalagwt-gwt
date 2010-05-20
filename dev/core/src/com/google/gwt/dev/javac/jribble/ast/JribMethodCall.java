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

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A method call that uses a {@Link JribMethodRef} to refer to the called
 * method.
 */
public class JribMethodCall extends JExpression {
  private List<JExpression> arguments = new ArrayList<JExpression>();
  private JExpression instance;
  private JribMethodRef methodRef;
  private JType type;

  public JribMethodCall(SourceInfo sourceInfo, JribMethodRef methodRef,
      JExpression instance, List<JExpression> arguments, JType type) {
    super(sourceInfo);
    this.methodRef = methodRef;
    this.instance = instance;
    this.arguments = arguments;
    this.type = type;
  }

  public List<JExpression> getArguments() {
    return arguments;
  }

  public JExpression getInstance() {
    return instance;
  }

  public JribMethodRef getMethodRef() {
    return methodRef;
  }

  public JType getType() {
    return type;
  }

  @Override
  public boolean hasSideEffects() {
    return true;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor instanceof JribVisitor) {
      JribVisitor jribVisitor = (JribVisitor) visitor;
      if (jribVisitor.visit(this, ctx)) {
        if (instance != null) {
          visitor.accept(instance);
        }
        visitor.accept(arguments);

        jribVisitor.endVisit(this, ctx);
      }
    }
  }
}
