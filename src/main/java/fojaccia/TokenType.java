package fojaccia;

public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACK, RIGHT_BRACK,
    COMMA, DOT, MINUS, PLUS, SLASH, STAR, SEMICOLON,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    SLASH_STAR, STAR_SLASH,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    CLASS, FN, IF, ELSE, FALSE, TRUE, FOR,
    WHILE, AND, OR, NULL, SUPER, THIS, VAR,
    PRINT, RETURN,

    EOF
}
