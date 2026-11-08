import java.util.HashMap;
import java.util.Map;

class Environment {
   private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

   void define (String name, Object value) {
    values.put(name, value);
   }

   Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
        return values.get(name.lexeme);
    }
    if (enclosing != null) {
        return enclosing.get(name);
    }
    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");  
   }
   Object getByName(String name) { return values.get(name); }
}
