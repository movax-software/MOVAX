package com.compiler.Engine.compiler.escaner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Escaner {
   
    private final Map<String, TokenType> palabrasReservadas = Map.ofEntries(
        Map.entry("auto", TokenType.AUTO),
        Map.entry("break", TokenType.BREAK),
        Map.entry("case", TokenType.CASE),
        Map.entry("char", TokenType.CHAR),
        Map.entry("const", TokenType.CONST),
        Map.entry("continue", TokenType.CONTINUE),
        Map.entry("default", TokenType.DEFAULT),
        Map.entry("do", TokenType.DO),
        Map.entry("double", TokenType.DOUBLE),
        Map.entry("else", TokenType.ELSE),
        Map.entry("enum", TokenType.ENUM),
        Map.entry("extern", TokenType.EXTERN),
        Map.entry("float", TokenType.FLOAT),
        Map.entry("for", TokenType.FOR),
        Map.entry("goto", TokenType.GOTO),
        Map.entry("if", TokenType.IF),
        Map.entry("int", TokenType.INT),
        Map.entry("long", TokenType.LONG),
        Map.entry("register", TokenType.REGISTER),
        Map.entry("return", TokenType.RETURN),
        Map.entry("short", TokenType.SHORT),
        Map.entry("signed", TokenType.SIGNED),
        Map.entry("sizeof", TokenType.SIZEOF),
        Map.entry("static", TokenType.STATIC),
        Map.entry("struct", TokenType.STRUCT),
        Map.entry("switch", TokenType.SWITCH),
        Map.entry("typedef", TokenType.TYPEDEF),
        Map.entry("union", TokenType.UNION),
        Map.entry("unsigned", TokenType.UNSIGNED),
        Map.entry("void", TokenType.VOID),
        Map.entry("volatile", TokenType.VOLATILE),
        Map.entry("while", TokenType.WHILE),
        Map.entry("_Packed", TokenType.PACKED)
    );


    private int lineaActual = 1;
    private boolean hasError = false;    
    private String CodigoFuente;
    private List<Token> Tokens = new LinkedList<>();

    public Escaner(String codigoFuente) {
        this.CodigoFuente = codigoFuente;
    }

    public void Scan() {
        // StringBuilder local para evitar acumulación
        StringBuilder Scanned = new StringBuilder();
        
        // Reiniciar estado
        this.lineaActual = 1;
        this.hasError = false;
        this.Tokens.clear();
        
        char[] chars = this.CodigoFuente.toCharArray();
        StringBuilder Token = new StringBuilder();
        int i = 0;

        while (i < chars.length) {
            char c = chars[i];
    
            //avance de numero de líneas
            if (Character.isWhitespace(c)) {
                if (c == '\n' || c == '\r') { 
                    this.lineaActual++;
                }
                i++;
                continue;
            }
            
            if (Character.isLetter(c) || c == '_') {
                Token.setLength(0);

                while (i < chars.length && (Character.isLetter(chars[i]) || Character.isDigit(chars[i]) || chars[i] == '_')) {
                    Token.append(chars[i]);
                    i++;
                }

                String palabra = Token.toString();
                
                if (palabrasReservadas.containsKey(palabra)) {
                    this.Tokens.add(new Token(palabrasReservadas.get(palabra), palabra));
                    continue;

                } else {
                    this.Tokens.add(new Token(TokenType.IDENTIFIER, palabra));
                    continue;
                }
            }

            //numeros binarios y hexadecimales
            if (chars[i] == '0' && i + 1 < chars.length) {

                Token.setLength(0);
                Token.append('0');

                if (chars[i+1] == 'b' || chars[i+1] == 'B') {
                    Token.append(chars[i+1]);
                    i += 2;

                    while (i < chars.length && (chars[i] == '0' || chars[i] == '1')) {
                        Token.append(chars[i]);
                        i++;
                    }

                    if (Token.length() <= 2) {
                        System.out.println("Numero binario inválido en la línea: " + lineaActual);
                    } else {
                        Tokens.add(new Token(TokenType.NUMBER, Token.toString()));
                    }
                    continue;
                }

                if (chars[i+1] == 'x' || chars[i+1] == 'X') {
                    Token.append(chars[i+1]);
                    i += 2;

                    while (i < chars.length &&
                        (Character.isDigit(chars[i]) ||
                        (chars[i] >= 'a' && chars[i] <= 'f') ||
                        (chars[i] >= 'A' && chars[i] <= 'F'))) {
                        Token.append(chars[i]);
                        i++;
                    }

                    if (Token.length() <= 2) {
                        System.out.println("Numero hexadecimal inválido en la línea: " + lineaActual);
                    } else {
                        Tokens.add(new Token(TokenType.NUMBER, Token.toString()));
                    }
                    continue;
                }
            }

            //numeros octales
            if (chars[i] == '0' && i + 1 < chars.length && chars[i+1] >= '0' && chars[i+1] <= '7') {

                Token.setLength(0);
                Token.append('0');
                i++;

                while (i < chars.length && chars[i] >= '0' && chars[i] <= '7') {
                    Token.append(chars[i]);
                    i++;
                }

                Tokens.add(new Token(TokenType.NUMBER, Token.toString()));
                continue;
            }

            //numeros decimales
            if (Character.isDigit(c) || (c == '.' && i + 1 < chars.length && Character.isDigit(chars[i+1]))) {
                Token.setLength(0);
                boolean esExp = false;
                boolean esFloat = false;

                while (i < chars.length) {
                    char ch = chars[i];

                    if (Character.isDigit(ch)) 
                        Token.append(ch);

                    else if (ch == '.' && !esFloat && !esExp) {
                        esFloat = true;
                        Token.append(ch);
                    } else if ((ch == 'e' || ch == 'E') && !esExp) {
                        esExp = true;
                        esFloat = true;
                        Token.append(ch);

                        if (i + 1 < chars.length && (chars[i+1] == '+' || chars[i+1] == '-')) {
                            i++;
                            Token.append(chars[i]);
                        }
                    }
                    else { break; }
                    i++;
                }

                String numero = Token.toString();

                if (numero.matches(".*[eE][+-]?$") || numero.endsWith(".")) {
                    Scanned.append("ERROR: Número inválido (" + numero + ")\n");
                } else {
                    if (esFloat) {
                        Tokens.add(new Token(TokenType.REAL, numero));
                    } else {
                        Tokens.add(new Token(TokenType.NUMBER, numero));
                    }
                }
                   
                continue;
            }
                        
            if (c == '=') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    i++;
                    Tokens.add(new Token(TokenType.EQ, Token.toString()));
                } else {
                    Tokens.add(new Token(TokenType.ASSIGN, Token.toString()));
                }

                Scanned.append(Token.toString()).append("\n");
                continue;
            }

            if (c == '!') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    i++;
                    Tokens.add(new Token(TokenType.NE, Token.toString()));
                } else {
                    Tokens.add(new Token(TokenType.NOT, Token.toString()));
                }

                continue;
            }

            if (c == '<') {
                Token.setLength(0); 
                Token.append(c); 
                i++;
                if (i < chars.length && chars[i] == '=') {                     
                    Token.append('='); 
                    Tokens.add(new Token(TokenType.LE, Token.toString()));
                    i++; 
                } else {
                    Tokens.add(new Token(TokenType.LT, Token.toString())); 
                }
                continue;
            }
            if (c == '+') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '+') {
                    Token.append('+');
                    Tokens.add(new Token(TokenType.INC, Token.toString()));   // ++
                    i++;
                } 
                else if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    Tokens.add(new Token(TokenType.PLUS_ASSIGN, Token.toString())); // +=
                    i++;
                } 
                else {
                    Tokens.add(new Token(TokenType.PLUS, Token.toString())); // +
                }

                continue;
            }

            if (c == '-') {
                Token.setLength(0);
                Token.append(c);
                i++;
                if (i < chars.length && chars[i] == '-') {
                    Token.append('-');
                    Tokens.add(new Token(TokenType.DEC, Token.toString()));
                    i++;
                } 
                else if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    Tokens.add(new Token(TokenType.MINUS_ASSIGN, Token.toString()));
                    i++;
                } 
                else {
                    Tokens.add(new Token(TokenType.MINUS, Token.toString()));
                }
                continue;
            }  

            if (c == '*') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    Tokens.add(new Token(TokenType.MUL_ASSIGN, Token.toString()));
                    i++;
                }
                else if (i < chars.length && chars[i] == '/') {
                    Token.append('/');
                    Tokens.add(new Token(TokenType.RBLOCK_COMMENT, Token.toString()));
                    i++;
                }
                else {
                    Tokens.add(new Token(TokenType.MUL, Token.toString()));
                }


                continue;
            }

            if (c == '/') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    Tokens.add(new Token(TokenType.DIV_ASSIGN, Token.toString())); // /=
                    i++;
                } 
                else if (i < chars.length && chars[i] == '/') {
                    Token.append('/');
                    Tokens.add(new Token(TokenType.LINE_COMMENT, Token.toString())); // //
                    i++;
                }
                else if (i < chars.length && chars[i] == '*') {
                    Token.append('*');
                    Tokens.add(new Token(TokenType.LBLOCK_COMMENT, Token.toString())); // /*
                    i++;
                }
                else {
                    Tokens.add(new Token(TokenType.DIV, Token.toString())); // /
                }

                continue;
            }

            if (c == '%') {
                Token.setLength(0);
                Token.append(c);
                i++;

                if (i < chars.length && chars[i] == '=') {
                    Token.append('=');
                    Tokens.add(new Token(TokenType.MOD_ASSIGN, Token.toString())); // %=
                    i++;
                } 
                else {
                    Tokens.add(new Token(TokenType.MOD, Token.toString())); // %
                }

                continue;
            }

    
            if (c == '(') {
                Tokens.add(new Token(TokenType.LPAREN, "("));
                i++;
                continue;
            }
            
            if (c == ')') {
                Tokens.add(new Token(TokenType.RPAREN, ")"));
                i++;
                continue;
            }
    
            if (c == '{') {
                Tokens.add(new Token(TokenType.LBRACE, "{"));
                i++;
                continue;
            }
            
            if (c == '}') {
                Tokens.add(new Token(TokenType.RBRACE, "}"));
                i++;
                continue;
            }
            
            if (c == '[') {
                Tokens.add(new Token(TokenType.LBRACKET, "["));
                i++;
                continue;
            }
            
            if (c == ']') {
                Tokens.add(new Token(TokenType.RBRACKET, "]"));
                i++;
                continue;
            }
            if (c == ';') {
                Tokens.add(new Token(TokenType.SEMICOLON, ";"));
                i++;
                continue;
            }
    
            if (c == '"') {
                Token.setLength(0);
                i++; // saltar comilla inicial

                while (i < chars.length && chars[i] != '"') {
                    if (chars[i] == '\\' && i + 1 < chars.length) {
                        Token.append(chars[i]);     // \
                        i++;
                        Token.append(chars[i]);     // carácter escapado
                    } else {
                        Token.append(chars[i]);
                    }
                    i++;
                }

                if (i < chars.length && chars[i] == '"') {
                    i++; // cerrar comilla
                    Tokens.add(new Token(TokenType.STRING, Token.toString()));
                    Scanned.append("\"" + Token + "\"\n");
                } else {
                    Scanned.append("ERROR: Cadena no cerrada\n");
                }

                continue;
            }


            if (c == '#') {
                Tokens.add(new Token(TokenType.HASH, "#"));
                i++;
                continue;
            }
            
            // Token inválido
            Tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(c)));
            hasError = true;
            break;
        }
    }
    
    public void writeScan() {
        for (Token token : this.Tokens) {
            System.out.println(token.getTokenType().name() + " | " + token.getLexema());
        }

        
        //formatLexerOutput(scannedResult);
        //codeAreaLexico.replaceText(this.TokensString);
    }

    public boolean gethasError() {
        return this.hasError;
    }
    
    public void sethasError(boolean error){
        this.hasError = error;
    }
    
}