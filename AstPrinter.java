import java.io.IOException;
import java.util.List;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    String print(Stmt stmt) {
        return stmt.accept(this);
    }

    // Expr visitors 

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
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }
    public String visitconnectionExpr(Expr.connection expr) {
        return parenthesize("from " + expr.dam.lexeme, expr.source);
    }
    public String visitflowLiteralExpr(Expr.flowLiteral expr) {
        return parenthesize("rain", expr.start, expr.spread, expr.magnitude);
    }

    // Stmt visitors 

    public String visitExpressionStmt(Stmt.Expression stmt) {
        return print(stmt.expression) + ";";
    }

    public String visitVarStmt(Stmt.Var stmt) {
        return "var " + stmt.name.lexeme + " = " +
            (stmt.initializer != null ? print(stmt.initializer) : "nil") + ";";
    }

    public String visitRiverStmt(Stmt.River stmt) {
        return (stmt.output ? "output " : "") + "river " + stmt.name.lexeme + " = " + print(stmt.value) + ";";
    }

    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder("{\n");
        for (Stmt s : stmt.statements) {
            sb.append("  ").append(print(s)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("if (").append(print(stmt.condition)).append(") ").append(print(stmt.thenBranch));
        if (stmt.elseBranch != null) {
            sb.append(" else ").append(print(stmt.elseBranch));
        }
        return sb.toString();
    }

    public String visitReleaseStmt(Stmt.Release stmt) {
        return "release " + print(stmt.value) + ";";
    }

    public String visitDamStmt(Stmt.Dam stmt) {
        StringBuilder params = new StringBuilder();
        for (Token p : stmt.params) params.append(p.lexeme).append(" ");
        StringBuilder sb = new StringBuilder();
        sb.append("dam ").append(stmt.name.lexeme).append("(").append(params.toString().trim()).append(") {\n");
        for (Stmt s : stmt.body) {
            sb.append("  ").append(print(s)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    // helper 

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
}