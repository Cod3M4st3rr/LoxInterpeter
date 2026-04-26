package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Static import to avoid typing "TokenType." everywhere
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    // Scanner stores raw source code as simple string
    // and generates tokens as we parse through the source
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;  // tracks start of current lexeme
    private int current = 0;  // tracks current position in source
    private int line = 1;  // tracks line in source

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            // Update the lexeme start position to current position
            this.start = this.current;
            scanToken();
        }

        // We are at the end of source; append EOF token
        // not strictly needed, but makes the parser cleaner.
        tokens.add(new Token(EOF, "", null, this.line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single-character tokens.
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // One or two-character operators.
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;

            case '\n':
                line++;
            case ' ':
            case '\r':
            case '\t':
                break;


            case '"':
                string();
                break;

            default:
                if (isDigit(c))
                {
                    number();
                }
                else if (isAlpha(c))
                {
                    identifier();
                }
                else
                {
                    Lox.error(this.line, "Unexpected character.");
                    break;
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = this.source.substring(this.start, this.current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private void number()
    {
        while (isDigit(peek()))
        {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext()))
        {
            advance();

            while (isDigit(peek()))
            {
                advance();
            }
        }

        addToken(
                NUMBER,
                Double.parseDouble(this.source.substring(this.start, this.current))
        );
    }

    private void string()
    {
        while (peek() != '"' && !isAtEnd())
        {
            if (peek() == '\n')
            {
                this.line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance();

        String value = this.source.substring(this.start + 1, this.current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected)
    {
        if (isAtEnd())
        {
            return false;
        }

        if (this.source.charAt(this.current) != expected)
        {
            return false;
        }

        this.current++;
        return true;
    }


    private char peek()
    {
        if (isAtEnd())
        {
            return '\0';
        }

        return this.source.charAt(this.current);
    }

    private char peekNext()
    {
        if (this.current + 1 >= this.source.length())
        {
            return '\0';
        }

        return this.source.charAt(this.current + 1);
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd()
    {
        return this.current >= this.source.length();
    }


    private char advance()
    {
        return this.source.charAt(this.current++);
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(type, text, literal, this.line));
    }
}