package fojaccia;

public class Interpreter implements Expression.Visitor<Object> {

    public void interpret(Expression exp) {
        try {
            Object value = evaluate(exp);
            System.out.println(makeTreeString(value));
        } catch (RuntimeError err) {
            Fojaccia.RuntimeError(err);
        }
    }

    @Override
    public Object visitBinary(Expression.Binary exp) {
        Object left = evaluate(exp.left);
        Object right = evaluate(exp.right);

        switch (exp.operator.type) {
            case GREATER:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case MINUS:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(exp.operator, "Operands must be numbers or strings");
            case SLASH:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                verifyNumberOperands(exp.operator, left, right);
                return (double) left * (double) right;
            case AND:
                Fojaccia.Log("interpreting AND with left: " + left + ", right: " + right);
                return (boolean) left && (boolean) right;
            case OR:
                Fojaccia.Log("interpreting OR with left: " + left + ", right: " + right);
                return (boolean) left && (boolean) right;
            default:
                return null;
        }
    }

    @Override
    public Object visitUnary(Expression.Unary exp) {
        Object right = evaluate(exp.right);

        switch (exp.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                verifyNumberOperand(exp.operator, right);
                return -(double) right;
            default:
                return null;
        }

    }

    @Override
    public Object visitGrouping(Expression.Grouping exp) {
        return evaluate(exp.expression);
    }

    @Override
    public Object visitLiteral(Expression.Literal exp) {
        return exp.value;
    }

    private String makeTreeString(Object tree) {
        if (tree == null)
            return "null";

        if (tree instanceof Double) {
            String text = tree.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return tree.toString();
    }

    private void verifyNumberOperands(Token operator, Object left, Object right) {
        verifyNumberOperand(operator, left);
        verifyNumberOperand(operator, right);
    }

    private void verifyNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;

        if (a == null)
            return false;

        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;

        if (object instanceof Boolean)
            return (boolean) object;

        return true;
    }

    private Object evaluate(Expression exp) {
        return exp.accept(this);
    }
}
