package fojaccia;

import java.util.List;

abstract class Expr {

  private Expr() {
    // Private constructor to prevent instantiation
  }

  public interface Visitor<R> {
    R visitBinary(Binary exp);

    R visitUnary(Unary exp);

    R visitCall(Call exp);

    R visitGrouping(Grouping exp);

    R visitLiteral(Literal exp);

    R visitLogical(Logical exp);

    R visitVariable(Variable exp);

    R visitAssignment(Assignment exp);

    R visitGet(Get exp);

    R visitSet(Set exp);

    R visitThis(This exp);

    R visitSuper(Super exp);
  }

  abstract <R> R accept(Visitor<R> visitor);

  public static class Super extends Expr {
    final Token keyword;
    final Token method;

    Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    <R> R accept(Visitor<R> visitor) { return visitor.visitSuper(this); }
  }

  public static class This extends Expr {
    final Token keyword;

    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) { return visitor.visitThis(this); }
  }

  public static class Set extends Expr {
    final Expr object;
    final Token name;
    final Expr value;

    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSet(this);
    }
  }

  public static class Get extends Expr {
    final Expr object;
    final Token name;

    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGet(this);
    }
  }

  public static class Assignment extends Expr {
    final Token name;
    final Expr value;

    public Assignment(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignment(this);
    }
  }

  public static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinary(this);
    }
  }

  public static class Unary extends Expr {
    final Token operator;
    final Expr right;

    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnary(this);
    }
  }

  public static class Call extends Expr {
    Expr callee;
    Token paren;
    List<Expr> arguments;

    public Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCall(this);
    }
  }

  public static class Grouping extends Expr {
    final Expr expression;

    public Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGrouping(this);
    }
  }

  public static class Literal extends Expr {
    final Object value;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteral(this);
    }
  }

  public static class Logical extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    public Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogical(this);
    }
  }

  public static class Variable extends Expr {
    final Token name;

    public Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariable(this);
    }
  }
}
