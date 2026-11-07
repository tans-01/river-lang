class Interpreter implements Expr.Visitor<Object>{


public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
}

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
 
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

public Object visitVariableExpr(Expr.Variable expr) {
    // Implement variable lookup here
    return null;
}   

public Obje

  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    switch (expr.operator.type) {
        case TokenType.MINUS:
            checkNumberOperand(expr.operator, right);
            return -(double)right;

        case TokenType.BANG:
            checkNumberOperand(expr.operator, right);
            return !isTruthy(right);
    }

    return null;
        
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }

  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate (expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
        
        case TokenType.PLUS:
            if (left instanceof Double && right instanceof Double) {
                return (double)left + (double)right;                    // we could add string too now (concatenation)
            }
            if (left instanceof String && right instanceof String) {
                return (String)left + (String)right;
            }
            throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

        case TokenType.MINUS:
            checkNumberOperands(expr.operator, left, right);
            return (double)left - (double)right;
        case TokenType.STAR:
            checkNumberOperands(expr.operator, left, right);
            return (double)left * (double)right;
        case TokenType.SLASH:
            checkNumberOperands(expr.operator, left, right);
            return (double)left / (double)right;
        case TokenType.EQUAL_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return isEqual(left, right);
        case TokenType.BANG_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return !isEqual(left, right);
        case TokenType.GREATER:
            checkNumberOperands(expr.operator, left, right);
            return (double)left > (double)right;
        case TokenType.GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left >= (double)right;
        case TokenType.LESS:
            checkNumberOperands(expr.operator, left, right);
            return (double)left < (double)right;
        case TokenType.LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left <= (double)right;
    }

    throw new RuntimeError(expr.operator, "Unknown binary operator: " + expr.operator.type);
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number: " + operator.lexeme);
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers: " + operator.lexeme);
  }


  void interpret(Expr expression) {
    try {
        Object value = evaluate(expression);
        System.out.println(stringify(value));
    } catch (RuntimeError error) {
        lox.runtimeError(error);
    }
  }
  public String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
        String text = object.toString();
        if (text.endsWith(".0")) 
            text = text.substring(0, text.length() - 2);
        return text;
    }
    return object.toString();
  }
}