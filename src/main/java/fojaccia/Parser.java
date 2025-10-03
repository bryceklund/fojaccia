package fojaccia;

import java.util.List;

import static fojaccia.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression exp = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            exp = new Expression.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expression comparison() {
        Expression exp = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            exp = new Expression.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expression term() {
        Expression exp = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            exp = new Expression.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expression factor() {
        Expression exp = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            exp = new Expression.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Expression.Unary(operator, unary());
        }
        return primary();
    }

    private Expression primary() {
        if (match(FALSE)) {
            Fojaccia.Log("matched false");
            return new Expression.Literal(false);
        }

        if (match(TRUE)) {
            Fojaccia.Log("matched true");
            return new Expression.Literal(true);
        }

        if (match(NULL))
            return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expression exp = expression();
            consume(RIGHT_PAREN, "Missing ')' after expression");
            return new Expression.Grouping(exp);
        }

        throw error(peek(), "Expression expected");
    }

    private ParseError error(Token token, String message) {
        Fojaccia.Error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!atEOF()) {
            if (previous().type.equals(SEMICOLON))
                return;

            switch (peek().type) {
                case CLASS:
                case FN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private boolean match(TokenType... tokenTypes) {
        for (TokenType type : tokenTypes) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        return !atEOF() && peek().type.equals(type);
    }

    private Token advance() {
        if (!atEOF())
            current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return current == 0
                ? tokens.get(current)
                : tokens.get(current - 1);
    }

    private boolean atEOF() {
        return peek().type.equals(EOF);
    }
}
