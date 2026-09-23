import java.util.List;
import java.util.ArrayList;
class Parser {
    private final List<Token> tokens;
    int current = 0;
    
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        if (match(TokenType.VAR)) {
            return vardeclaration();
        }
        return statement();
    }

    private Stmt vardeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        
        Expr initializer = null;
        if(match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        return expressionstatement();
    }
    private Stmt expressionstatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    Expr expression() {
        return equality();
    }

    public Expr equality() {
        Expr expr = comparison();
        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();
        while(match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
    }
        return expr;
  }
    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
        Token operator = previous();
        Expr right = unary();
        return new Expr.Unary(operator, right);
    }

        return primary();
  }
    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Varriable(previous());
        }
        
        if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();

            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }
        return null;
    }
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message);

    }



    private Boolean match(TokenType... types) {
        for (TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private Boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    private Token peek(){
        return tokens.get(current);
    }
    private Boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    private Token advance() {
        if(!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}