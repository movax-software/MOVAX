package com.compiler.Engine.compiler.escaner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;

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

    private final Map<String, TokenType> OperadoresUnarios = Map.ofEntries(
        Map.entry("{", TokenType.LBRACE), 
        Map.entry("}", TokenType.RBRACE), 
        Map.entry("[", TokenType.LBRACKET), 
        Map.entry("]", TokenType.RBRACKET), 
        Map.entry("(", TokenType.LPAREN), 
        Map.entry(")", TokenType.RPAREN), 
        Map.entry(";", TokenType.SEMICOLON), 
        Map.entry("#", TokenType.HASH), 
        Map.entry("\\", TokenType.BACKSLASH), 
        Map.entry("'", TokenType.APOSTROPHE)
    );

    private final Map<String, TokenType> OperadoresBinarios = Map.ofEntries(
        Map.entry("*", TokenType.MUL),
        Map.entry("/", TokenType.DIV),
        Map.entry("%", TokenType.MOD),
        Map.entry("+", TokenType.PLUS),
        Map.entry("-", TokenType.MINUS),
        Map.entry("=", TokenType.ASSIGN),
        Map.entry("<<", TokenType.SHL),
        Map.entry(">>", TokenType.SHR),
        Map.entry("<", TokenType.LT),
        Map.entry(">", TokenType.GT),
        Map.entry("<=", TokenType.LE),
        Map.entry(">=", TokenType.GE),
        Map.entry("==", TokenType.EQ),
        Map.entry("!=", TokenType.NE),
        Map.entry("&", TokenType.AND),
        Map.entry("|", TokenType.OR),
        Map.entry("^", TokenType.XOR),
        Map.entry("&&", TokenType.LAND),
        Map.entry("||", TokenType.LOR),
        Map.entry(",", TokenType.COMMA)
    );

    private int lineaActual = 1;
    private boolean hasError = false;    
    private String CodigoFuente;
    private ArrayList<Token> Tokens = new ArrayList<>();

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
                    Tokens.add(new Token(TokenType.INC, Token.toString()));
                    i++;
                } else {
                    Tokens.add(new Token(TokenType.PLUS, Token.toString()));
                }
                continue;
            }
            
            if (c == '-' && i + 1 < chars.length && chars[i+1] == '-') {
                tokens.add(24); 
                lexemas.add("--"); 
                i+=2;
                Scanned.append(DEC + "\n");
                continue;
            }            
            
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                tokens.add(14);
                lexemas.add(String.valueOf(c)); 
                Scanned.append(c + "\n");
                i++;
                continue;
            }

            if (c == '$' && i + 1 < chars.length && chars[i + 1] == '$') {
                tokens.add(15);
                lexemas.add("$$"); 
                Scanned.append("$$\n");
                i += 2;
                continue;
            }
    
            if (c == '(') {
                tokens.add(16);
                lexemas.add("("); 
                Scanned.append(c + "\n");
                i++;
                continue;
            }
            
            if (c == ')') {
                tokens.add(17);
                lexemas.add(")"); 
                Scanned.append(c + "\n");
                i++;
                continue;
            }
    
            if (c == '{') {
                tokens.add(18);
                lexemas.add("{"); 
                Scanned.append(c + "\n");
                i++;
                continue;
            }
            
            if (c == '}') {
                tokens.add(19);
                lexemas.add("}"); 
                Scanned.append(c + "\n");
                i++;
                continue;
            }
            
            if (c == ';') {
                tokens.add(20);
                lexemas.add(";"); 
                Scanned.append(";\n");
                i++;
                continue;
            }
    
            if (c == '"') {
                Token.setLength(0);
                Token.append(c);
                i++;
                while (i < chars.length && chars[i] != '"') {
                    Token.append(chars[i]);
                    i++;
                }
                if (i < chars.length && chars[i] == '"') {
                    Token.append(chars[i]);
                    String cadena = Token.toString();
                    tokens.add(21);
                    lexemas.add(cadena); 
                    Scanned.append(cadena + "\n");
                    i++;
                }
                continue;
            }

            if (c == '#') {
                tokens.add(25);
                lexemas.add("#");
                Scanned.append(c + "\n");
                i++;
                continue;
            }
            
            // Token inválido
            this.hasError = true;
            tokens.add(-1);
            lexemas.add(String.valueOf(c)); // NUEVO: Guardar el carácter inválido
            Scanned.append("ERROR: Token inválido ('" + c + "') en línea " + this.lineaActual + "\n");
            i++;
            
            return Scanned.toString();

        }
    }
    
    public void WriteRun(CodeArea codeAreaLexico) {
        String scannedResult = this.Scan();
        formatLexerOutput(scannedResult);
        codeAreaLexico.replaceText(this.TokensString);
    }

    private void formatLexerOutput(String scanned) {
        String[] lines = scanned.split("\n");
        StringBuilder formattedTokens = new StringBuilder();
        
        // Mapa para almacenar errores únicos: clave = "carácter_línea"
        Map<String, String> uniqueErrors = new LinkedHashMap<>();
        int tokenCount = 0;

        for (String line : lines) {
            if (line.startsWith("ERROR:")) {
                // Extraer el carácter inválido y número de línea
                Pattern pattern = Pattern.compile("Token inválido \\('(.)'\\) en línea (\\d+)");
                Matcher matcher = pattern.matcher(line);
                
                if (matcher.find()) {
                    String invalidChar = matcher.group(1);
                    String lineNumber = matcher.group(2);
                    
                    // Crear clave única: carácter + línea
                    String key = invalidChar + "_" + lineNumber;
                    
                    // Solo agregar si no existe (evita duplicados)
                    if (!uniqueErrors.containsKey(key)) {
                        uniqueErrors.put(key, String.format("Token inválido '%s' en línea %s", 
                            invalidChar, lineNumber));
                    }
                }
            } else if (!line.trim().isEmpty()) {
                // Línea válida (token reconocido)
                formattedTokens.append("TOKEN: ").append(line).append("\n");
                tokenCount++;
            }
        }

        // Decidir el contenido final de TokensString
        if (!uniqueErrors.isEmpty()) {
            StringBuilder errorOutput = new StringBuilder();
            errorOutput.append(String.format(
                "ERRORES LÉXICOS ENCONTRADOS: %d\n" +
                "═══════════════════════════════════\n\n",
                uniqueErrors.size()
            ));
            
            // Mostrar errores únicos
            for (String errorMsg : uniqueErrors.values()) {
                errorOutput.append("  • ").append(errorMsg).append("\n");
            }
            
            errorOutput.append(String.format(
                "\n═══════════════════════════════════\n" +
                "✓ Tokens válidos: %d\n" +
                "✗ Errores únicos: %d",
                tokenCount, uniqueErrors.size()
            ));
            
            this.TokensString = errorOutput.toString();
        } else {
            this.TokensString = String.format(
                "✓ ANÁLISIS LÉXICO COMPLETADO\n" +
                "═══════════════════════════════════\n" +
                "Total de tokens: %d\n\n%s",
                tokenCount, formattedTokens.toString()
            );
        }
    }

    public String getTokensString() {
        return TokensString;
    }

    public void setTokensString(String tokensString) {
        TokensString = tokensString;
    }
    
    public boolean gethasError() {
        return this.hasError;
    }
    
    public void sethasError(boolean error){
        this.hasError = error;
    }
    
    public ArrayList<Integer> getTokens() {
        return tokens;
    }
    
    public ArrayList<String> getLexemas() {
        return lexemas;
    }
}