public class runtimeError extends RuntimeException {
    final Token token;

    runtimeError(Token token, String message) {
      super(message);
      this.token = token;
    }
  } {
    
}
