package com.google.gwt.dev.javac.jribble;

import static com.google.gwt.dev.jjs.SourceOrigin.UNKNOWN;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.jribble.ast.JribMethodCall;
import com.google.gwt.dev.javac.jribble.ast.JribMethodRef;
import com.google.gwt.dev.javac.jribble.ast.JribNewInstance;
import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.jjs.ast.JBinaryOperation;
import com.google.gwt.dev.jjs.ast.JBinaryOperator;
import com.google.gwt.dev.jjs.ast.JBlock;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JDeclarationStatement;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JField.Disposition;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JInterfaceType;
import com.google.gwt.dev.jjs.ast.JLocal;
import com.google.gwt.dev.jjs.ast.JLocalRef;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodBody;
import com.google.gwt.dev.jjs.ast.JParameter;
import com.google.gwt.dev.jjs.ast.JParameterRef;
import com.google.gwt.dev.jjs.ast.JPrimitiveType;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.ast.JStatement;
import com.google.gwt.dev.jjs.ast.JThisRef;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVariable;
import com.google.jribble.ast.JribbleProtos;
import com.google.jribble.ast.JribbleProtos.Assignment;
import com.google.jribble.ast.JribbleProtos.Block;
import com.google.jribble.ast.JribbleProtos.DeclaredType;
import com.google.jribble.ast.JribbleProtos.Expr;
import com.google.jribble.ast.JribbleProtos.FieldDef;
import com.google.jribble.ast.JribbleProtos.GlobalName;
import com.google.jribble.ast.JribbleProtos.Literal;
import com.google.jribble.ast.JribbleProtos.Method;
import com.google.jribble.ast.JribbleProtos.MethodCall;
import com.google.jribble.ast.JribbleProtos.MethodSignature;
import com.google.jribble.ast.JribbleProtos.Modifiers;
import com.google.jribble.ast.JribbleProtos.NewObject;
import com.google.jribble.ast.JribbleProtos.ParamDef;
import com.google.jribble.ast.JribbleProtos.Statement;
import com.google.jribble.ast.JribbleProtos.Type;
import com.google.jribble.ast.JribbleProtos.VarDef;
import com.google.jribble.ast.JribbleProtos.VarRef;

/**
 * Converts a Jribble file to a GWT AST.
 */
public class JribbleReader {
  private JProgram dummyProgram = new JProgram();
  private final TreeLogger logger;

  private Map<String, JVariable> localVariables = new LinkedHashMap<String, JVariable>();
  private JMethodBody currentMethodBody;
  private JDeclaredType currentType;

  public JribbleReader(TreeLogger logger) {
    this.logger = logger;
  }

  public JDeclaredType declaredType(InputStream stream)
      throws UnableToCompleteException {
    DeclaredType proto;
    try {
      proto = JribbleProtos.DeclaredType.parseFrom(stream);
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Error reading Jribble file", e);
      throw new UnableToCompleteException();
    }

    JDeclaredType declaredType;
    if (proto.getIsInterface()) {
      declaredType = dummyProgram.createInterface(UNKNOWN,
          name(proto.getName()));
    } else {
      declaredType = dummyProgram.createClass(UNKNOWN, name(proto.getName()),
          proto.getModifiers().getIsAbstract(), proto.getModifiers()
              .getIsFinal());
    }
    currentType = declaredType;

    declaredType.setSuperClass(dummyClass(name(proto.getExt())));
    for (JribbleProtos.GlobalName name : proto.getImplementsList()) {
      declaredType.addImplements(dummyInterface(name(name)));
    }

    for (JribbleProtos.Declaration decl : proto.getBodyList()) {
      switch (decl.getType()) {
      case Field:
        FieldDef fieldProto = decl.getFieldDef();
        JField field = dummyProgram.createField(UNKNOWN, name(proto.getName()),
            declaredType, type(fieldProto.getTpe()), decl.getModifiers()
                .getIsStatic(), Disposition.NONE);
        if (fieldProto.hasInitializer()) {
          JFieldRef ref = new JFieldRef(UNKNOWN,
              field.isStatic() ? null : new JThisRef(UNKNOWN, dummyProgram
                  .getNonNullType(declaredType)), field,
              declaredType);
          field.setInitializer(new JDeclarationStatement(UNKNOWN, ref,
              expr(fieldProto.getInitializer())));
        }

        break;

      case Method:
        Method methodProto = decl.getMethod();
        Modifiers mods = decl.getModifiers();
        JMethod method;
        if (methodProto.getName().equals("new")) {
          method = dummyProgram.createConstructor(UNKNOWN,
              (JClassType) declaredType);
        } else {
          method = dummyProgram.createMethod(UNKNOWN, methodProto.getName(),
              declaredType, type(methodProto.getReturnType()),
              mods.getIsAbstract(), mods.getIsStatic(), mods.getIsFinal(),
              mods.getIsPrivate(), false); // TODO(spoon) should be
                                           // mods.getIsNative()
        }
        localVariables.clear();

        for (ParamDef paramProto : methodProto.getParamDefList()) {
          JParameter parameter = JProgram.createParameter(UNKNOWN,
              paramProto.getName(), type(paramProto.getTpe()), false, false,
              method);
          localVariables.put(parameter.getName(), parameter);
        }

        if (methodProto.hasBody()) {
          currentMethodBody = new JMethodBody(UNKNOWN);
          method.setBody(currentMethodBody);

          currentMethodBody.getBlock().addStmt(statement(methodProto.getBody()));
        }
      }
    }

    return declaredType;
  }

  private JClassType dummyClass(String name) {
    return dummyProgram.createClass(UNKNOWN, name, false, false);
  }

  private JInterfaceType dummyInterface(String name) {
    return dummyProgram.createInterface(UNKNOWN, name);
  }

  public JExpression expr(Expr expr) {
    switch (expr.getType()) {
    case Literal:
      Literal literal = expr.getLiteral();
      switch (literal.getType()) {
      case Boolean:
        return dummyProgram.getLiteralBoolean(literal.getBoolValue());
      case Char:
        return dummyProgram.getLiteralChar((char) literal.getCharValue());
      case Double:
        return dummyProgram.getLiteralDouble(literal.getDoubleValue());
      case Float:
        return dummyProgram.getLiteralFloat(literal.getFloatValue());
      case Int:
        return dummyProgram.getLiteralInt(literal.getIntValue());
      case Long:
        return dummyProgram.getLiteralLong(literal.getLongValue());
      case Null:
        return dummyProgram.getLiteralNull();
      case String:
        return dummyProgram.getLiteralString(UNKNOWN, literal.getStringValue());
      }
    case MethodCall: {
      MethodCall methodCall = expr.getMethodCall();
      JExpression instance = null;
      if (methodCall.hasReceiver()) {
        instance = expr(methodCall.getReceiver());
      }
      List<JExpression> arguments = new ArrayList<JExpression>();
      for (Expr arg : methodCall.getArgumentList()) {
        arguments.add(expr(arg));
      }
      JribMethodRef methodRef = methodRef(methodCall.getSignature());
      return new JribMethodCall(UNKNOWN, methodRef, instance, arguments,
          type(methodCall.getSignature().getReturnType()));
    }
    case NewObject: {
      NewObject newProto = expr.getNewObject();
      JClassType type = dummyClass(name(newProto.getClazz()));
      List<JExpression> arguments = new ArrayList<JExpression>();
      for (Expr arg : newProto.getArgumentList()) {
        arguments.add(expr(arg));
      }
      JribMethodRef methodRef = methodRef(newProto.getSignature());
      return new JribNewInstance(UNKNOWN, type, methodRef, arguments);
    }
    case ThisRef:
      return new JThisRef(UNKNOWN, dummyProgram.getNonNullType(currentType));
    case VarRef:
      VarRef varProto = expr.getVarRef();
      JVariable var = localVariables.get(varProto.getName());
      if (var == null) {
        throw new InternalCompilerException("Undeclared variable "
            + varProto.getName());
      }
      if (var instanceof JLocal) {
        return new JLocalRef(UNKNOWN, (JLocal) var);
      }
      if (var instanceof JParameter) {
        return new JParameterRef(UNKNOWN, (JParameter) var);
      }
      throw new InternalCompilerException("Should not reach here");
      
    case Assignment:
      Assignment assignProto = expr.getAssignment();
      JExpression lhs = expr(assignProto.getLhs());
      JExpression rhs = expr(assignProto.getRhs());
      return new JBinaryOperation(UNKNOWN, lhs.getType(), JBinaryOperator.ASG, lhs, rhs);

      // TODO(spoon) many more cases
    }

    throw new InternalCompilerException("Should not be reachable");
  }

  public JribMethodRef methodRef(MethodSignature signature) {
    String typeName = name(signature.getOwner());
    String jsniTypes = "(";
    for (Type tpe : signature.getParamTypeList()) {
      jsniTypes += type(tpe).getJsniSignatureName();
    }
    jsniTypes += ")";
    if (signature.getName().equals("new")) {
      jsniTypes += "V";
    } else {
      jsniTypes += type(signature.getReturnType()).getJsniSignatureName();
    }
    String methodName = signature.getName();
    if (signature.getName().equals("new")) {
      methodName = typeName;
      if (methodName.contains(".")) {
        methodName = methodName.substring(methodName.lastIndexOf('.') + 1);
      }
    }
    String typeSig = methodName + jsniTypes;
    return new JribMethodRef(typeName, typeSig);
  }

  public String name(GlobalName name) {
    String prefix = "";
    if (name.hasPkg()) {
      prefix = name.getPkg() + ".";
    }
    return prefix + name.getName();
  }

  public JStatement statement(Statement proto) {
    switch (proto.getType()) {
    case Block: {
      Block blockProto = proto.getBlock();
      JBlock block = new JBlock(UNKNOWN);
      for (Statement stat : blockProto.getStatementList()) {
        block.addStmt(statement(stat));
      }
      return block;
    }
    case VarDef: {
      VarDef varDef = proto.getVarDef();
      JLocal local = JProgram.createLocal(UNKNOWN, varDef.getName(),
          type(varDef.getTpe()), false, currentMethodBody);
      localVariables.put(local.getName(), local);
      JExpression initializer = null;
      if (varDef.hasInitializer()) {
        JExpression initExpr = expr(varDef.getInitializer());
        initializer = new JBinaryOperation(UNKNOWN, local.getType(),
            JBinaryOperator.ASG, new JLocalRef(UNKNOWN, local), initExpr);
      }
      return new JDeclarationStatement(UNKNOWN, new JLocalRef(UNKNOWN, local),
          initializer);
    }
    case Expr:
      return expr(proto.getExpr()).makeStatement();
      // TODO(spoon) many more cases
    }

    throw new InternalCompilerException("Should not be reachable");
  }

  public JType type(Type tpe) {
    switch (tpe.getType()) {
    case Array:
      int dimensions = 0;
      Type leaf = tpe;
      while (leaf.getType() == JribbleProtos.Type.TypeType.Array) {
        dimensions++;
        leaf = leaf.getArrayElementType();
      }
      return dummyProgram.getTypeArray(type(leaf), dimensions);

    case Named:
      return dummyClass(name(tpe.getNamedType()));

    case Primitive:
      switch (tpe.getPrimitiveType()) {
      case Boolean:
        return JPrimitiveType.BOOLEAN;
      case Byte:
        return JPrimitiveType.BYTE;
      case Char:
        return JPrimitiveType.CHAR;
      case Double:
        return JPrimitiveType.DOUBLE;
      case Float:
        return JPrimitiveType.FLOAT;
      case Int:
        return JPrimitiveType.INT;
      case Long:
        return JPrimitiveType.LONG;
      case Short:
        return JPrimitiveType.SHORT;
      }

    case Void:
      return JPrimitiveType.VOID;
    }

    throw new InternalCompilerException("Should not be reachable");
  }

}
