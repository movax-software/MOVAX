package com.compiler.Engine.compiler.escaner;

public class Token {
    private TokenType tokenType;
    private String lexema;
    
    public Token(TokenType tokenType, String lexema) {
        this.tokenType = tokenType;
        this.lexema = lexema;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }
}
