package fojaccia;

import java.util.List;

import fojaccia.Fojaccia.LogLevel;

public class FojFunction implements FojCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  FojFunction(Stmt.Function declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);

    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    Fojaccia.Log(LogLevel.DEBUG, "calling function: " + declaration.name + "with arguments: " + arguments.toString());
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return r) {
      return r.value;
    }
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
