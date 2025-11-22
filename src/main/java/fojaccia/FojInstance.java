package fojaccia;

import java.util.HashMap;
import java.util.Map;

class FojInstance {
  private FojClass fojClass;
  private final Map<String, Object> fields = new HashMap<>();

  FojInstance(FojClass fojClass) {
    this.fojClass = fojClass;
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    FojFunction method = fojClass.findMethod(name.lexeme);
    if (method != null)
      return method;

    throw new RuntimeError(name, "Undefined property: `" + name.lexeme + "`");
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return fojClass.name + " instance";
  }
}
