package fojaccia;

class AstPrinter implements Expr.Visitor<String> {

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expr exp) {
        return exp.accept(this);
    }

    @Override
    public String visitLogical(Expr.Logical expr) {
        return "";
    }

    @Override
    public String visitAssignment(Expr.Assignment expr) {
        return "";
    }

    @Override
    public String visitVariable(Expr.Variable expr) {
        return "";
    }

    @Override
    public String visitBinary(Expr.Binary exp) {
        return parenthesize(exp.operator.lexeme, exp.left, exp.right);
    }

    @Override
    public String visitGrouping(Expr.Grouping exp) {
        return parenthesize("group", exp.expression);
    }

    @Override
    public String visitLiteral(Expr.Literal exp) {
        if (exp.value == null)
            return "null";
        return exp.value.toString();
    }

    @Override
    public String visitUnary(Expr.Unary exp) {
        return parenthesize(exp.operator.lexeme, exp.right);
    }

    private String parenthesize(String name, Expr... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr exp : expressions) {
            builder.append(" ");
            builder.append(exp.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}