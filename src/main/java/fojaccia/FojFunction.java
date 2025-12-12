package fojaccia;

import java.util.List;

import fojaccia.Fojaccia.LogLevel;

public class FojFunction implements FojCallable {
  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  FojFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  FojFunction bind(FojInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new FojFunction(declaration, environment, isInitializer);
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

    Fojaccia.Log(LogLevel.DEBUG, "calling function: " + declaration.name + " with arguments: " + arguments.toString());
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return r) {
      if (isInitializer) return closure.getAt(0, "this");
      return r.value;
    }

    if (isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
