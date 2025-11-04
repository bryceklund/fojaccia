package fojaccia;

import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import fojaccia.Fojaccia.LogLevel;
import fojaccia.Stmt.Expression;
import fojaccia.Stmt.If;
import fojaccia.Stmt.Print;

public class Interpreter implements
    Expr.Visitor<Object>,
    Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private final Map<Expr, Integer> locals = new HashMap<>();
  private Environment environment = globals;

  public static final String NATIVE_CLOCK = "clock";
  public static final String NATIVE_PRINT = "print";

  Interpreter() {
    globals.define(NATIVE_CLOCK, new FojCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() {
        return "<native function>";
      }
    });

    // globals.define(NATIVE_PRINT, new FojCallable(String message) {
    // @Override
    // public int arity() { return 1; }
    //
    // @Override
    // public Object call(Interpreter interpreter, List<Object> arguments) {
    // System.out.println(arguments.get(0));
    // return new Object();
    // }
    //
    // @Override
    // public String toString() {
    // return "<native function>";
    // }
    // });
  }

  public void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
      // Object value = evaluate(exp);
      // System.out.println(makeTreeString(value));
    } catch (RuntimeError err) {
      Fojaccia.RuntimeError(err);
    }
  }

  private void execute(Stmt statement) {
    statement.accept(this);
  }

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;

    Fojaccia.Log(LogLevel.DEBUG, "executing block");
    try {
      this.environment = environment;
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    FojFunction function = new FojFunction(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Object visitVariable(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  @Override
  public Object visitAssignment(Expr.Assignment expr) {
    Object value = evaluate(expr.value);
    Fojaccia.Log(LogLevel.DEBUG, "visiting assignment expression with value: " + value);
    environment.assign(expr.name, value);
    return value;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    Object value = evaluate(stmt.expression);
    if (Fojaccia.repl) {
      System.out.println(makeTreeString(value));
    }
    return null;
  }

  @Override
  public Void visitPrintStmt(Print stmt) {
    Object value = makeTreeString(evaluate(stmt.expression));
    System.out.println(value);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) {
      Fojaccia.Log(LogLevel.DEBUG, "About to evaluate return value: " + stmt.value);
      value = evaluate(stmt.value);
      Fojaccia.Log(LogLevel.DEBUG, "Return value evaluated to: " + value);

    }

    throw new Return(value);
  }

  @Override
  public Void visitIfStmt(If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  @Override
  public Object visitBinary(Expr.Binary exp) {
    Fojaccia.Log(LogLevel.DEBUG, "Evaluating binary with operator: " + exp.operator.type);
    Object left = evaluate(exp.left);
    Fojaccia.Log(LogLevel.DEBUG, "Left evaluated to: " + left);
    Object right = evaluate(exp.right);
    Fojaccia.Log(LogLevel.DEBUG, "Right evaluated to: " + right);

    switch (exp.operator.type) {
      case GREATER:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left > (double) right;
      case GREATER_EQUAL:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left >= (double) right;
      case LESS:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left < (double) right;
      case LESS_EQUAL:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left <= (double) right;
      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      case MINUS:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left - (double) right;
      case PLUS:
        Fojaccia.Log(LogLevel.DEBUG, "In PLUS case now");
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }

        if (left instanceof String || right instanceof String) {
          return makeTreeString(left) + makeTreeString(right);
        }

        throw new RuntimeError(exp.operator, "Operands must be numbers or strings");
      case SLASH:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left / (double) right;
      case STAR:
        verifyNumberOperands(exp.operator, left, right);
        return (double) left * (double) right;
      // case AND:
      // Fojaccia.Log(LogLevel.DEBUG, "interpreting AND with left: " + left + ",
      // right: " + right);
      // return (boolean) left && (boolean) right;
      // case OR:
      // Fojaccia.Log(LogLevel.DEBUG, "interpreting OR with left: " + left + ", right:
      // " + right);
      // return (boolean) left && (boolean) right;
      default:
        return null;
    }
  }

  @Override
  public Object visitCall(Expr.Call exp) {
    Object callee = evaluate(exp.callee);
    Fojaccia.Log(LogLevel.DEBUG, "visiting call expression, callee is: " + callee);
    Fojaccia.Log(LogLevel.DEBUG, "callee class is: " + callee.getClass().getName());
    if (!(callee instanceof FojCallable)) {
      throw new RuntimeError(exp.paren, "Only functions and classes can be called");
    }

    // List<Object> arguments = exp.arguments.stream()
    // .map(argument -> evaluate(argument))
    // .toList();
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : exp.arguments) {
      arguments.add(evaluate(argument));
    }

    Fojaccia.Log(LogLevel.DEBUG, "argument list evaluated");
    FojCallable function = (FojCallable) callee;
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(exp.paren,
          String.format("Expected %s arguments but got %s",
              function.arity(), arguments.size()));
    }

    Fojaccia.Log(LogLevel.DEBUG, "now calling function");
    return function.call(this, arguments);
  }

  @Override
  public Object visitUnary(Expr.Unary exp) {
    Object right = evaluate(exp.right);

    switch (exp.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        verifyNumberOperand(exp.operator, right);
        return -(double) right;
      default:
        return null;
    }

  }

  @Override
  public Object visitGrouping(Expr.Grouping exp) {
    return evaluate(exp.expression);
  }

  @Override
  public Object visitLiteral(Expr.Literal exp) {
    return exp.value;
  }

  @Override
  public Object visitLogical(Expr.Logical exp) {
    Object left = evaluate(exp.left);

    if (exp.operator.type == TokenType.OR) {
      if (isTruthy(left)) {
        return left;
      }
    } else {
      if (!isTruthy(left)) {
        return left;
      }
    }

    return evaluate(exp.right);
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
  }

  private String makeTreeString(Object tree) {
    if (tree == null)
      return "null";

    if (tree instanceof Double) {
      String text = tree.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return tree.toString();
  }

  private void verifyNumberOperands(Token operator, Object left, Object right) {
    verifyNumberOperand(operator, left);
    verifyNumberOperand(operator, right);
  }

  private void verifyNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;
    throw new RuntimeError(operator, "Operand must be a number");
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;

    if (a == null)
      return false;

    return a.equals(b);
  }

  private boolean isTruthy(Object object) {
    if (object == null)
      return false;

    if (object instanceof Boolean)
      return (boolean) object;

    return true;
  }

  private Object evaluate(Expr exp) {
    return exp.accept(this);
  }
}
