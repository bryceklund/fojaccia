package fojaccia;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.List;

import static fojaccia.TokenType.*;
import static fojaccia.Fojaccia.LogLevel;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        try {
            while (!atEOF()) {
                statements.add(statement());
            }
        } catch (ParseError error) {
            return null;
        }

        return statements;
    }

    private Stmt statement() {
        if (match(PRINT))
            return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "`;` expected after value");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(SEMICOLON, "`;` expected after value");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr exp = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL, AND, OR)) {
            Token operator = previous();
            Expr right = comparison();
            exp = new Expr.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expr comparison() {
        Expr exp = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            exp = new Expr.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expr term() {
        Expr exp = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            exp = new Expr.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expr factor() {
        Expr exp = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            exp = new Expr.Binary(exp, operator, right);
        }

        return exp;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            Fojaccia.Log(LogLevel.DEBUG, "matched false");
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            Fojaccia.Log(LogLevel.DEBUG, "matched true");
            return new Expr.Literal(true);
        }

        if (match(NULL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr exp = expression();
            consume(RIGHT_PAREN, "Missing ')' after expression");
            return new Expr.Grouping(exp);
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
