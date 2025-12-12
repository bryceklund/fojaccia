package fojaccia;

import java.util.ArrayList;
import java.util.Arrays;
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
      if (match(CLASS))
        return classDeclaration();
      if (match(FN))
        return function("function");
      if (match(VAR))
        return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(IDENTIFIER, "Class name expected");

    Expr.Variable superclass = null;
    if (match(LESS)) {
      consume(IDENTIFIER, "Superclass name expected");
      superclass = new Expr.Variable(previous());
    }

    consume(LEFT_BRACK, "`{` expected before class body");

    List<Stmt.Function> methods = new ArrayList<>();
    while (!check(RIGHT_BRACK) && !atEOF()) {
      methods.add(function("method"));
    }

    consume(RIGHT_BRACK, "`}` expected after class body");

    return new Stmt.Class(name, superclass, methods);
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
    if (match(RETURN))
      return returnStatement();
    if (match(WHILE))
      return whileStatement();
    if (match(FOR))
      return forStatement();

    if (match(LEFT_BRACK))
      return new Stmt.Block(block());

    return expressionStatement();
  }

  /**
   * Anatomy: for (var i = 0; i < 11; i++) {...}
   * Initializer: `int i = 0;`
   * Condition: `i < 11;`
   * Increment: `i++;`
   * Body: `{...}`
   */
  private Stmt forStatement() {
    consume(LEFT_PAREN, "`(` expected after `for`");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null; // why?
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "`;` expected after loop condition");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "`)` expected after for clauses");
    Stmt body = statement();

    // Parse and interpret an expression out of the incrementer, i.e. `i++`
    // This goes into an arraylist which Block executes entry by entry within the
    // corresponding scope environment
    if (increment != null) {
      body = new Stmt.Block(
          Arrays.asList(
              body,
              new Stmt.Expression(increment)));
    }

    if (condition == null)
      condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(
          Arrays.asList(
              initializer,
              body));
    }

    return body;
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

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
      Fojaccia.Log(LogLevel.DEBUG, "Return value class: " + value.getClass().getName());
      if (value instanceof Expr.Binary) {
        Fojaccia.Log(LogLevel.DEBUG, "It's a binary expression");
      }

      if (value instanceof Expr.Variable) {
        Fojaccia.Log(LogLevel.DEBUG, "It's a variable expression");
      }
    }

    consume(SEMICOLON, "`;` expected after return value");
    return new Stmt.Return(keyword, value);
  }

  private Stmt expressionStatement() {
    Expr value = expression();
    consume(SEMICOLON, "`;` expected after value");
    return new Stmt.Expression(value);
  }

  private Stmt.Function function(String type) {
    Token name = consume(IDENTIFIER, "Expected " + type + " name");
    List<Token> parameters = new ArrayList<>();
    consume(LEFT_PAREN, "Expected `(` after " + type + " name");

    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters");
        }

        parameters.add(consume(IDENTIFIER, "Expected parameter name"));
      } while (match(COMMA));
    }

    consume(RIGHT_PAREN, "`)` expected after parameter list");
    consume(LEFT_BRACK, "`{` expected before " + type + " body");
    List<Stmt> body = block();

    return new Stmt.Function(name, parameters, body);
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
      } else if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get) expr;
        return new Expr.Set(get.object, get.name, value);
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
    return call();
  }

  private Expr call() {
    Expr expr = primary();

    Fojaccia.Log(LogLevel.DEBUG, "call() executing");
    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(DOT)) {
        Token name = consume(IDENTIFIER, "Property name expected after `.`");
        expr = new Expr.Get(expr, name);
      } else {
        break;
      }
    }
    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments");
        }

        arguments.add(expression());
        Fojaccia.Log("call finishing");
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN, "`)` expected after argument list");
    return new Expr.Call(callee, paren, arguments);
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

    if (match(THIS))
      return new Expr.This(previous());

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(SUPER)) {
      Token keyword = previous();
      consume(DOT, "`.` expected after `super`");
      Token method = consume(IDENTIFIER, "Superclass method name expected");
      return new Expr.Super(keyword, method);
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
