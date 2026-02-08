package com.compiler.Engine.compiler.escaner;

public enum TokenType {

    // PALABRAS RESERVADAS
    AUTO, BREAK, CASE, CHAR, CONST, CONTINUE, DEFAULT, DO, DOUBLE,
    ELSE, ENUM, EXTERN, FLOAT, FOR, GOTO, IF, INT, LONG, REGISTER,
    RETURN, SHORT, SIGNED, SIZEOF, STATIC, STRUCT, SWITCH, TYPEDEF,
    UNION, UNSIGNED, VOID, VOLATILE, WHILE, PACKED, INCLUDE,

    // OPERADORES 
    ASSIGN(1),
    PLUS(2), MINUS(2),
    MUL(3), DIV(3), MOD(3),
    SHL(4), SHR(4),

    // PARÉNTESIS 
    LPAREN, RPAREN,

    // RELACIONALES
    LT, GT, LE, GE, EQ, NE,

    // BIT A BIT / LÓGICOS
    AND, OR, XOR, LAND, LOR,

    // OTROS
    COMMA, NOT, INC, DEC,
    MINUS_ASSIGN, PLUS_ASSIGN, MUL_ASSIGN,
    DIV_ASSIGN, MOD_ASSIGN, LIB_ID,

    // PUNTUACIÓN
    LBRACE, RBRACE, LBRACKET, RBRACKET,
    SEMICOLON, HASH, QUOTE,
    BACKSLASH, APOSTROPHE,
    LINE_COMMENT, LBLOCK_COMMENT, RBLOCK_COMMENT,

    IDENTIFIER, NUMBER, REAL, STRING, EOF,

    UNKNOWN;

    private final int precedencia;

    TokenType(int precedencia) {
        this.precedencia = precedencia;
    }

    TokenType() {
        this.precedencia = -1; // no operador
    }

    public int getPrecedencia() {
        return precedencia;
    }

    public boolean esOperador() {
        return precedencia > 0;
    }
}
