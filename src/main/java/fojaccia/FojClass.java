package fojaccia;

import java.util.List;
import java.util.Map;

class FojClass implements FojCallable {
  final String name;
  final FojClass superclass;
  private final Map<String, FojFunction> methods;

  FojClass(String name, FojClass superclass, Map<String, FojFunction> methods) {
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    FojInstance instance = new FojInstance(this);
    FojFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }
    return instance;
  }

  FojFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    if (superclass != null) {
      return superclass.findMethod(name);
    }
    return null;
  }

  @Override
  public int arity() {
    FojFunction initializer = findMethod("init");
    if (initializer == null)
      return 0;
    return initializer.arity();
  }

  @Override
  public String toString() {
    return name;
  }
}
