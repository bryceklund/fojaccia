package fojaccia;

public class FojFunction implements FojCallable {
    private final Stmt.Function declaration;

    FojFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = interpreter.globals;

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, declaration.params.get(i));
        }

        interpreter.executeBlock(declaration.body, environment);
        return null;
    }
}
