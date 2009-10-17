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

import static com.google.gwt.dev.jjs.SourceOrigin.UNKNOWN;

import com.google.gwt.dev.javac.ljava.ast.JribMethodCall;
import com.google.gwt.dev.javac.ljava.ast.JribMethodRef;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Loose Java into a syntax tree.
 * 
 * TODO(grek) implement a real parser
 */
public class LooseJavaParser {

  public static JClassType parse(Reader source) {
    // Create a throw-away program because some AST nodes need one
    JProgram program = new JProgram();

    JClassType sillyClass = new JClassType(UNKNOWN,
        "com.google.gwt.sample.hello.client.Silly", false, false);
    {
      // constructor method TODO(spoon) make it a JConstructor
      JMethod constructor = new JMethod(UNKNOWN, "Silly", sillyClass,
          JPrimitiveType.VOID, false, false, false, false);
      JMethodBody methodBody = new JMethodBody(UNKNOWN);
      constructor.setBody(methodBody);
      sillyClass.addMethod(constructor);
    }

    {
      // onModuleLoad() method
      JMethod onModuleLoad = new JMethod(UNKNOWN, "onModuleLoad", sillyClass,
          JPrimitiveType.VOID, false, false, false, false);
      JMethodBody methodBody = new JMethodBody(UNKNOWN);
      onModuleLoad.setBody(methodBody);

      JBlock block = methodBody.getBlock();

      {
        // Window.alert("Hello, world!")
        List<JExpression> args = new ArrayList<JExpression>();
        args.add(program.getLiteralString(UNKNOWN, "Hello, world!"));
        block.addStmt(new JribMethodCall(UNKNOWN, new JribMethodRef(
            "com.google.gwt.user.client.Window", "alert(Ljava/lang/String;)V"),
            null, args, JPrimitiveType.VOID).makeStatement());
      }

      sillyClass.addMethod(onModuleLoad);
    }
    return sillyClass;
  }
}
