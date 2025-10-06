package fojaccia;

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

        while (!atEOF()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "identifier expected");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "`;` expected after declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(IF))
            return ifStatement();
        if (match(PRINT))
            return printStatement();
        if (match(WHILE))
            return whileStatement();

        if (match(LEFT_BRACK))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "`(` expected after `while`)");
        Expr condition = expression();
        consume(RIGHT_PAREN, "`)` expected after expression");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "`(` expected after `if`)");
        Expr condition = expression();
        consume(RIGHT_PAREN, "`)` expected after expression");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACK) && !atEOF()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACK, "`}` expected after block");
        return statements;
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
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assignment(name, value);
            }
            error(equals, "Invalid token assignment");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr exp = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
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

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

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
