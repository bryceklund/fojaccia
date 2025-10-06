package fojaccia;

import java.util.HashMap;
import java.util.Map;

import fojaccia.Fojaccia.LogLevel;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        Fojaccia.Log(LogLevel.DEBUG, "defining token with lexeme: " + name + ", value: " + value);
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        Fojaccia.Log(LogLevel.DEBUG, "assigning token with lexeme: " + name.lexeme + ", value: " + value);

        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable: " + name.lexeme);
    }

    Object get(Token name) {
        Fojaccia.Log(LogLevel.DEBUG, "getting token with lexeme: " + name.lexeme);
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable: " + name.lexeme);
    }
}
