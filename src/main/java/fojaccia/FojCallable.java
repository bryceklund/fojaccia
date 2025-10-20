package fojaccia;

import java.util.List;

public interface FojCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);

}
