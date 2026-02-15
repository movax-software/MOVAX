package com.compiler.Engine.compiler.escaner;

public enum TokenType {

    // PALABRAS RESERVADAS
    AUTO, BREAK, CASE, CHAR, CONST, CONTINUE, DEFAULT, DO, DOUBLE,
    ELSE, ENUM, EXTERN, FLOAT, FOR, GOTO, IF, INT, LONG, REGISTER,
    RETURN, SHORT, SIGNED, SIZEOF, STATIC, STRUCT, SWITCH, TYPEDEF,
    UNION, UNSIGNED, VOID, VOLATILE, WHILE, PACKED, INCLUDE,

    // OPERADORES 
    ASSIGN(12), PLUS_ASSIGN(12), MINUS_ASSIGN(12), MUL_ASSIGN(12), DIV_ASSIGN(12), MOD_ASSIGN(12),
    PLUS(2), MINUS(2),
    MUL(3), DIV(3), MOD(3),
    SHL(4), SHR(4),


    // PARÉNTESIS 
    LPAREN, RPAREN,

    // RELACIONALES
    
    // OPERADORES RELACIONALES
    LT(3), GT(3), LE(3), GE(3),
    EQ(2), NE(2),
    
    // OPERADORES BIT A BIT
    AND(7), XOR(8), OR(9),
    
    // OPERADORES LÓGICOS (menor precedencia)
    LAND(10), LOR(11),
    
    // OTROS
    COMMA, NOT, INC, DEC, 
    
    LIB_ID,

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
