class Interpreter implements Expr.Visitor<Object>{
}


public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
}

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
 
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    switch (expr.operator.type) {
        case TokenType.MINUS:
            checkNumberOperand(expr.operator, right);
            return -(double)right;

        case TokenType.BANG:
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
            break;
        case TokenType.MINUS:
            return (double)left - (double)right;
        case TokenType.STAR:
            return (double)left * (double)right;
        case TokenType.SLASH:
            return (double)left / (double)right;
        case TokenType.EQUAL_EQUAL:
            return isEqual(left, right);
        case TokenType.BANG_EQUAL:
            return !isEqual(left, right);
        case TokenType.GREATER:
            return (double)left > (double)right;
        case TokenType.GREATER_EQUAL:
            return (double)left >= (double)right;
        case TokenType.LESS:
            return (double)left < (double)right;
        case TokenType.LESS_EQUAL:
            return (double)left <= (double)right;
    }

    throw new RuntimeException("Unknown binary operator: " + expr.operator.type);
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeException("Operand must be a number: " + operator.lexeme);
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeException("Operands must be numbers: " + operator.lexeme);
  }

  
    