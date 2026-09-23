import java.io.IOException;
import java.util.List;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    public String visitVarriableExpr(Expr.Varriable expr) {
        return expr.name.lexeme;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {
    java.util.Scanner input = new java.util.Scanner(System.in);
    System.out.print("Enter an expression: ");
    String source = input.nextLine();

    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    for (Stmt stmt : statements) {
        if (stmt instanceof Stmt.Var varStmt) {
            System.out.println("Var: " + varStmt.name.lexeme + " = " + 
                (varStmt.initializer != null ? new AstPrinter().print(varStmt.initializer) : "null"));
        } else if (stmt instanceof Stmt.Expression exprStmt) {
            System.out.println("Expr statement: " + new AstPrinter().print(exprStmt.expression));
        }
    }
}

    
}