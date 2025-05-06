package fojaccia;

class AstPrinter implements Expression.Visitor<String> {

    public static void main(String[] args) {
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expression.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expression.Grouping(new Expression.Literal(45.67))
        );

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expression exp) {
        return exp.accept(this);
    }

    @Override
    public String visitBinary(Expression.Binary exp) {
        return parenthesize(exp.operator.lexeme, exp.left, exp.right);
    }

    @Override
    public String visitGrouping(Expression.Grouping exp) {
        return parenthesize("group", exp.expression);
    }

    @Override
    public String visitLiteral(Expression.Literal exp) {
        if (exp.value == null) return "null";
        return exp.value.toString();
    }

    @Override
    public String visitUnary(Expression.Unary exp) {
        return parenthesize(exp.operator.lexeme, exp.right);
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression exp : expressions) {
            builder.append(" ");
            builder.append(exp.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}