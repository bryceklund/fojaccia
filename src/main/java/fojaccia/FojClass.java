package fojaccia;

import java.util.List;
import java.util.Map;

class FojClass implements FojCallable {
  final String name;
  private final Map<String, FojFunction> methods;

  FojClass(String name, Map<String, FojFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    FojInstance instance = new FojInstance(this);
    return instance;
  }

  FojFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    return null;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public String toString() {
    return name;
  }
}
