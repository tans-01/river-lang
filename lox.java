import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
public class lox {
    
 public static double rainfall = 1.0;

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            System.out.println("Usage: jlox [script] [rainfall_mm]");
            System.exit(64);
        }
        else {
            if (args.length == 2) rainfall = Double.parseDouble(args[1]);
            if (args.length >= 1) runFile(args[0]); else runPrompt();
        }
    }
    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }
    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }
    public static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();
    new Interpreter(rainfall).interpret(statements);
    }


    static void error(int line, String msg) {
        System.err.println("[line " + line + "] Error: " + msg);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + " [\nline " + error.token().line + "]");
    }

}
