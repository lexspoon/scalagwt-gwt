package com.google.gwt.dev.javac.jribble.ast;

import java.util.List;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JVisitor;

public class JribNewInstance extends JExpression {
  private final JClassType type;
  private final JribMethodRef constructorRef;
  private final List<JExpression> arguments;

  public JribNewInstance(SourceInfo info, JClassType type,
      JribMethodRef constructorRef, List<JExpression> arguments) {
    super(info);
    this.type = type;
    this.constructorRef = constructorRef;
    this.arguments = arguments;
  }

  public List<JExpression> getArguments() {
    return arguments;
  }
  
  public JribMethodRef getConstructorRef() {
    return constructorRef;
  }
  
  @Override
  public JClassType getType() {
    return type;
  }

  @Override
  public boolean hasSideEffects() {
    return true;
  }

  @Override
  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor instanceof JribVisitor) {
      JribVisitor jribVisitor = (JribVisitor) visitor;
      if (jribVisitor.visit(this, ctx)) {
        visitor.accept(arguments);
        jribVisitor.endVisit(this, ctx);
      }
    }
  }
}
