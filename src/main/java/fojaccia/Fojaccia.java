package fojaccia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static fojaccia.TokenType.EOF;

/**
 * Note: Exit codes are specified based on the Unix sysexits.h header:
 * <a href=
 * "https://man.freebsd.org/cgi/man.cgi?query=sysexits&manpath=FreeBSD+4.3-RELEASE"
 * />
 */

public class Fojaccia {

    private static final Interpreter interpreter = new Interpreter();

    private static boolean hadError;
    private static boolean hadRuntimeError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: foj <script>");
            System.exit(64); // EX_USAGE
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static void Log(String message) {
        String dateTime = new Date(System.currentTimeMillis()).toString();
        String logMessage = String.format("%s\t%s", dateTime, message);
        System.out.println(logMessage);
    }

    public static void RuntimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n" +
                "[line " + error.token.line + "]");

        hadRuntimeError = true;
    }

    public static void Error(int line, String message) {
        report(line, "", message);
    }

    public static void Error(Token token, String message) {
        if (token.type.equals(EOF)) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError)
            System.exit(65); // EX_DATAERR
        if (hadRuntimeError)
            System.exit(70); // EX_SOFTWARE
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String input) {
        Scanner scanner = new Scanner(input);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();
        if (hadError)
            return;
        // System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(expression);
    }

    private static void report(int line, String where, String message) {
        Log("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
}
