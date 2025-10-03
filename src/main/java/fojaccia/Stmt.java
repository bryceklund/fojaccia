package fojaccia;

public abstract class Stmt {

    private Stmt() {
        // Private constructor to prevent instantiation
    }

    interface Visitor<R> {
        R visitExpressionStmt(Expression statement);

        R visitPrintStmt(Print statement);
    }

    abstract <R> R accept(Visitor<R> visitor);

    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

}
