import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitVarStmt(Var stmt);
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitReleaseStmt(Release stmt);
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }


    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
    final Expr expression;
  }
  static class Var extends Stmt {
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }


    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
    final Token name;
    final Expr initializer;
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }


    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
    final List<Stmt> statements;
  }
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }


    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
  static class Release extends Stmt {
    Release(Expr value) {
      this.value = value;
    }


    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReleaseStmt(this);
    }
    final Expr value;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
