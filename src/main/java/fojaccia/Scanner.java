package fojaccia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fojaccia.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final static Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords =  new HashMap<>();
        keywords.put("fn",       FN);
        keywords.put("if",       IF);
        keywords.put("or",       OR);
        keywords.put("and",      AND);
        keywords.put("for",      FOR);
        keywords.put("var",      VAR);
        keywords.put("else",     ELSE);
        keywords.put("null",     NULL);
        keywords.put("this",     THIS);
        keywords.put("true" ,    TRUE);
        keywords.put("class",    CLASS);
        keywords.put("false",    FALSE);
        keywords.put("print",    PRINT);
        keywords.put("super",    SUPER);
        keywords.put("while",    WHILE);
        keywords.put("return",   RETURN);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!atEOF()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = nextChar();
        switch (c) {
            case '(': addToken(LEFT_PAREN);                                         break;
            case ')': addToken(RIGHT_PAREN);                                        break;
            case '{': addToken(LEFT_BRACK);                                         break;
            case '}': addToken(RIGHT_BRACK);                                        break;
            case ',': addToken(COMMA);                                              break;
            case '.': addToken(DOT);                                                break;
            case '+': addToken(PLUS);                                               break;
            case '-': addToken(MINUS);                                              break;
            case ';': addToken(SEMICOLON);                                          break;
            case '*': addToken(STAR);                                               break;
            case '!': addToken(match('=') ? BANG_EQUAL    : BANG);         break;
            case '=': addToken(match('=') ? EQUAL_EQUAL   : EQUAL);        break;
            case '<': addToken(match('=') ? LESS_EQUAL    : LESS);         break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER);      break;
            case '/':
                // Handle line comments
                if (match('/')) {
                    while (peek() != '\n' && !atEOF()) nextChar();
                // Handle block comments
                } else if (match('*')) {
                    while (!source.startsWith("*/", current)) {
                        if (atEOF()) {
                            Fojaccia.error(line, "Unterminated block comment before EOF");
                            break;
                        }
                        if (peek() == '\n') line++;
                        nextChar();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ', '\r', '\t': break;
            case '\n': line++;    break;
            case '"': string();   break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Fojaccia.error(line, "Unexpected character");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) nextChar();
        TokenType type = keywords.get(source.substring(start, current));
        if (type == NULL) type = IDENTIFIER;

        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return  (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // This feels like a mess?
    private void number() {
        while (isDigit(peek())) nextChar();
        if (peek() == '.' && isDigit(peekNext())) {
            nextChar();

            while (isDigit(peek())) nextChar();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        while (peek() != '"' && !atEOF()) {
            if (peek() == '\n') line++;
            nextChar();
        }

        if (atEOF()) {
            Fojaccia.error(line, "Unterminated string before EOF");
            return;
        }

        // Handle the closing double-quote
        nextChar();

        // Trim off the quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(
            new Token(
                type, source.substring(start, current),
                literal, line
            )
        );
    }

    private char nextChar() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (atEOF()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (atEOF()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean atEOF() {
        return current >= source.length();
    }
}
