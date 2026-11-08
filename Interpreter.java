import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

private Environment environment = new Environment(null);

static final int DAYS = 10;
private Double rainfall = 1.0;
private final List<String> rivernames = new ArrayList<>();
private String outputname = null;

Interpreter(double rainfall) { 
    this.rainfall = rainfall;   
    }

void interpret(List<Stmt> statements) {
    try {
    for(Stmt statement : statements) statement.accept(this);
            report();
    } catch (RuntimeError error) {
        lox.runtimeError(error);
    }
  }

@Override
public Void visitExpressionStmt(Stmt.Expression stmt) {
 Object value = evaluate(stmt.expression);
 System.out.println(stringify(value));
 return null;
}

@Override 
public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
        value = evaluate(stmt.initializer);
    }
    environment.define(stmt.name.lexeme, value);
    return null;
}

@Override 
public Void visitBlockStmt(Stmt.Block stmt) {
    for (Stmt statement : stmt.statements) {
        execute(statement);
    }
    return null;
}

@Override
public Void visitRiverStmt(Stmt.River stmt) {
  environment.define(stmt.name.lexeme, evaluate(stmt.value));
  rivernames.add(stmt.name.lexeme);
  if (stmt.output) outputname = stmt.name.lexeme;
  return null;
}

@Override
public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
        execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
        execute(stmt.elseBranch);
    }
    return null;
}

@Override
public Void visitReleaseStmt(Stmt.Release stmt) {
     throw new Release(evaluate(stmt.value));
}

@Override
public Void visitDamStmt(Stmt.Dam stmt) {
    environment.define(stmt.name.lexeme, stmt);
    return null;
}

@Override
public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
}

@Override
  public Object visitconnectionExpr(Expr.connection expr) {
    Object found = environment.get(expr.dam);
    if (!(found instanceof Stmt.Dam)) {
        throw new RuntimeError(expr.dam, "'" + expr.dam.lexeme + "' is not a dam.");
    }
    Stmt.Dam dam = (Stmt.Dam) found;

    if (expr.args.size() != dam.params.size()) {
        throw new RuntimeError(expr.dam, "Dam '" + dam.name.lexeme + "' expects "
            + dam.params.size() + " arguments but got " + expr.args.size() + ".");
    }

    List<Object> argValues = new ArrayList<>();
    for (Expr a : expr.args) argValues.add(evaluate(a));

    double[] inflow = toFlow(evaluate(expr.source));

    // the parameter named startingLevel sets the dam's level on day 1
    double level = 0;
    for (int i = 0; i < dam.params.size(); i++) {
        if (dam.params.get(i).lexeme.equals("startingLevel")) level = (Double) argValues.get(i);
    }

    double[] out = new double[DAYS];
    Environment previous = environment;
    try {
        for (int d = 0; d < DAYS; d++) {
            Environment today = new Environment(previous);
            for (int i = 0; i < dam.params.size(); i++) {
                today.define(dam.params.get(i).lexeme, argValues.get(i));
            }
            today.define("inflow", inflow[d]);
            today.define("level", level);
            today.define("rainfall", rainfall);
            today.define("day", (double) (d + 1));
            environment = today;

            double release = 0;   // no release statement reached = hold everything
            try {
                for (Stmt s : dam.body) execute(s);
            } catch (Release r) {
                Object v = r.value;
                if (v instanceof double[]) release = ((double[]) v)[d];   // e.g. release rain(...)
                else release = (Double) v;
            }

            double available = level + inflow[d];
            if (release < 0) release = 0;
            if (release > available) release = available;   // can't release more than it holds

            out[d] = release;
            level = available - release;                       // carries over to tomorrow
        }
    } finally {
        environment = previous;
    }
    return out;
}

  @Override
  public Object visitflowLiteralExpr(Expr.flowLiteral expr) {
    double  start = (Double) evaluate(expr.start);
    double  spread = (Double) evaluate(expr.spread);
    double  magnitude = (Double) evaluate(expr.magnitude);
    
    double [] flow_out = new double[DAYS];
    double flow = 1/(spread + 1);
    double rain_left = magnitude * rainfall;

    for (int i=1; i <= DAYS; i++) { //starts from day 1
        
        if (i < start) continue;

        // what is does is take the rain amount that is there and mutliple it with the flow to give the flow rate and then subtracts the rain left 

        double today = rain_left * flow;   
        flow_out[i-1] = today; // store today's flow output -1 because array is 0-indexed 

        rain_left = rain_left - today;

    }
    return flow_out;

}
 
  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
}   

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);
    switch (expr.operator.type) {
        case MINUS:
            checkNumberOperand(expr.operator, right);
            return -(double)right;

        case BANG:
            return !isTruthy(right);
    }

    return null;
        
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate (expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
        
        case PLUS:
            if (left instanceof String && right instanceof String) {
                return (String) left + (String) right;
            }
            return arth(expr.operator, left, right);
        
        case MINUS:
        case STAR:
        case SLASH:
        
            return arth(expr.operator, left, right);
        
        case EQUAL_EQUAL: 
            return isEqual(left, right);
        case BANG_EQUAL:
            return !isEqual(left, right);
        case GREATER:
            checkNumberOperands(expr.operator, left, right);
            return (double)left > (double)right;
        case GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left >= (double)right;
        case LESS:
            checkNumberOperands(expr.operator, left, right);
            return (double)left < (double)right;
        case LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left <= (double)right;
    }

    throw new RuntimeError(expr.operator, "Unknown binary operator: " + expr.operator.type);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }
  
  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
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


  
  public String stringify(Object object) {  // stringify cant return a flow. thats why we do this make it string first and then return it.
    if (object == null) return "nil";

    if (object instanceof double[]) {
        double[] f = (double[]) object;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < f.length; i++) {
            sb.append(String.format("%.2f", f[i]));
            if (i < f.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    if (object instanceof Double) {
        String text = object.toString();
        if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
        return text;
    }
    return object.toString();
}

  private Object arth(Token operator, Object left, Object right) {

    if(left instanceof Double && right instanceof Double) {
        return calculate(operator, (double)left, (double)right);
    }
    boolean a = left instanceof Double[] || left instanceof double[];
    boolean b = right instanceof Double[] || right instanceof double[];

    if (!a || !b)
        throw new RuntimeError(operator, "Operands must be numbers or arrays of numbers: " + operator.lexeme);

    double[] leftArray = toFlow(left), rightArray = toFlow(right), result = new double[DAYS];
    for (int i = 0; i < DAYS; i++) {
        result[i] = calculate(operator, leftArray[i], rightArray[i]);
    }
    return result;

  }

  private double calculate(Token operator, double left, double right) {
    switch (operator.type) {
        case PLUS: return left + right;
        case MINUS: return left - right;
        case STAR: return left * right;
        case SLASH: return left / right;
        default: throw new RuntimeError(operator, "Unknown arithmetic operator: " + operator.type);
    }

  }

  private double[] toFlow(Object o) {
    if (o instanceof double[]) return (double[]) o;
    double[] a = new double[DAYS];
    Arrays.fill(a, (double) o);
    return a;
  }
  private void report() {
  System.out.println("Rainfall: " + rainfall + " mm");
  for (String name : rivernames) {
    String tag = name.equals(outputname) ? "   <-- OUTPUT" : "";
    System.out.println(name + ": " + stringify(environment.getByName(name)) + tag);
  }
}
}   