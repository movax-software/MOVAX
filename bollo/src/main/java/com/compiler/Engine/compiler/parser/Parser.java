package com.compiler.Engine.compiler.parser;
import com.compiler.Engine.ast.*;
import com.compiler.Engine.compiler.escaner.Escaner;
import com.compiler.Engine.compiler.escaner.Token;
import com.compiler.Engine.compiler.escaner.TokenType;

import java.util.ArrayList;

import org.fxmisc.richtext.CodeArea;

public class Parser {

    private ArrayList<Token> tokens = new ArrayList<>();
    private boolean ParserError = false;
    private int lineaActual = 1;
    private TokenType Tok;
    private static int iterator = 0;
    private NodoParseTree arbolSintactico;

    public Parser(ArrayList<Token> tokens, CodeArea codeAreaParser) {
        this.tokens = tokens;
    }

    private NodoParseTree PROGRAMA() {

        // Crear nodo raíz del árbol
        NodoParseTree nodoPadre = new NodoParseTree("PROGRAMA");        

        // agarrar el primer token del escaneado
        this.Tok = this.tokens.get(iterator).getTokenType();

        // Manejo opcional de HASH #include
        if (this.Tok == TokenType.HASH) 
            nodoPadre.agregarHijo(IMPORTS());

        // Declaración de funciones
        while (esReturnable(this.Tok)) 
            nodoPadre.agregarHijo(FUNCION());


        return nodoPadre;
    }    

    private NodoParseTree FUNCION(){

        NodoParseTree funcionNodo = new NodoParseTree("FUNCION");

        funcionNodo.agregarHijo(eat(this.Tok));
        funcionNodo.agregarHijo(eat(TokenType.IDENTIFIER)); // Puede ser 'main'

        funcionNodo.agregarHijo(eat(TokenType.LPAREN));
        funcionNodo.agregarHijo(PARAMS());
        funcionNodo.agregarHijo(eat(TokenType.RPAREN));

        if (this.Tok == TokenType.LBRACE) {
            funcionNodo.agregarHijo(eat(TokenType.LBRACE));
            funcionNodo.agregarHijo(INSTRUCCION());
            funcionNodo.agregarHijo(eat(TokenType.RBRACE));            
        } else {
            funcionNodo.agregarHijo(eat(TokenType.SEMICOLON));
        }

        return funcionNodo;

    }

    private NodoParseTree INSTRUCCION(){

        // DECLARACION
        if (esTipo(this.Tok)) {
            
        }
        
        // LLAMADA FUNCION
        if (esFuncion(this.Tok)) {
            
        }

        // ASIGNACION
        if (this.Tok == TokenType.IDENTIFIER) {
            
        }

        // FOR
        if (this.Tok == TokenType.FOR) {
            
        }
        
        // IF
        if (this.Tok == TokenType.IF) {
            
        }
        
        // WHILE
        
        // DO-WHILE
        // SWITCH
        

    }

    private NodoParseTree IMPORTS() {
        // Si no empieza con #, no hay imports que procesar
        if (this.Tok != TokenType.HASH) 
            return null;

        // Creamos un nodo contenedor para los imports
        NodoParseTree listaImports = new NodoParseTree("IMPORTS");

        // Mientras el token actual sea #, seguimos extrayendo imports
        while (this.Tok == TokenType.HASH && !this.ParserError) {
            NodoParseTree nodoHijo = new NodoParseTree("IMPORT");
            
            nodoHijo.agregarHijo(eat(TokenType.HASH));
            nodoHijo.agregarHijo(eat(TokenType.INCLUDE));
            nodoHijo.agregarHijo(eat(TokenType.LT));
            nodoHijo.agregarHijo(eat(TokenType.IDENTIFIER));
            nodoHijo.agregarHijo(eat(TokenType.GT));
            
            listaImports.agregarHijo(nodoHijo);
            
            // Actualizar el token actual para la siguiente vuelta del while
            if (iterator < tokens.size()) {
                this.Tok = tokens.get(iterator).getTokenType();
            } else {
                break;
            }
        }

        return listaImports;
    }
    
    private NodoParseTree PARAMS() {

        NodoParseTree params = new NodoParseTree("PARAMS");

        if (!esTipo(this.Tok))
            return params;  

        do {
            NodoParseTree param = new NodoParseTree("PARAM");

            param.agregarHijo(eat(this.Tok));
            param.agregarHijo(eat(TokenType.IDENTIFIER));

            params.agregarHijo(param);

            if (this.Tok == TokenType.COMMA)
                eat(TokenType.COMMA);
            else
                break;

        } while (esTipo(this.Tok));

        return params;
    }

    private NodoParseTree DECLARACION() {
        if (this.ParserError) return null;

        NodoParseTree nodo = new NodoParseTree("DECLARACION");

        switch (this.Tok) {
            case TIPO_DATO_INT: 
                nodo.agregarHijo(eat(TIPO_DATO_INT)); 
                nodo.agregarHijo(eat(ID)); 
                switch (this.Tok) {
                    case ASIG: 
                        nodo.agregarHijo(eat(ASIG)); 
                        switch (this.Tok) {
                            case NUM: 
                                nodo.agregarHijo(eat(NUM)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case INPUTINT: 
                                nodo.agregarHijo(eat(INPUTINT)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case ID:
                                nodo.agregarHijo(eat(ID));
                                nodo.agregarHijo(eat(EOL));
                                nodo.agregarHijo(DECLARACION());
                                break;
                        } break;
                    case EOL: 
                        nodo.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(DECLARACION()); 
                        break;
                } break;
            case TIPO_DATO_FLOAT: 
                nodo.agregarHijo(eat(TIPO_DATO_FLOAT)); 
                nodo.agregarHijo(eat(ID)); 
                switch (this.Tok) {
                    case ASIG: 
                        nodo.agregarHijo(eat(ASIG)); 
                        switch (this.Tok) {
                            case FLOAT: 
                                nodo.agregarHijo(eat(FLOAT)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case INPUTFLOAT: 
                                nodo.agregarHijo(eat(INPUTFLOAT)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case ID:
                                nodo.agregarHijo(eat(ID));
                                nodo.agregarHijo(eat(EOL));
                                nodo.agregarHijo(DECLARACION());
                                break;
                        } break;
                    case EOL: 
                        nodo.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(DECLARACION()); 
                        break;
                } break;
            case TIPO_DATO_STRING: 
                nodo.agregarHijo(eat(TIPO_DATO_STRING)); 
                nodo.agregarHijo(eat(ID)); 
                switch (this.Tok) {
                    case ASIG: 
                        nodo.agregarHijo(eat(ASIG)); 
                        switch (this.Tok) {
                            case CADENA: 
                                nodo.agregarHijo(eat(CADENA)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case INPUTSTRING: 
                                nodo.agregarHijo(eat(INPUTSTRING)); 
                                nodo.agregarHijo(eat(EOL)); 
                                nodo.agregarHijo(DECLARACION()); 
                                break;
                            case ID:
                                nodo.agregarHijo(eat(ID));
                                nodo.agregarHijo(eat(EOL));
                                nodo.agregarHijo(DECLARACION());
                                break;
                            default:
                                Error();
                                break;
                        } break;
                    case EOL: 
                        nodo.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(DECLARACION()); 
                        break;
                } break;
            default: 
                return INSTRUCCION();
        }
        return nodo;
    }

    private NodoParseTree INSTRUCCIONA() {
        if(this.ParserError) return null;

        NodoParseTree nodo = new NodoParseTree("INSTRUCCION");
        
        switch (this.Tok) {
            case TIPO_DATO_INT:
            case TIPO_DATO_FLOAT: 
            case TIPO_DATO_STRING:
                nodo.agregarHijo(DECLARACION()); 
                nodo.agregarHijo(INSTRUCCION()); 
                break;
            case ID:
                nodo.agregarHijo(eat(ID)); 
                nodo.agregarHijo(eat(ASIG)); 
                nodo.agregarHijo(CALCULO()); 
                nodo.agregarHijo(eat(EOL)); 
                nodo.agregarHijo(INSTRUCCION()); 
                break;
            case IF:
                NodoParseTree nodoIf = new NodoParseTree("IF");
                nodoIf.agregarHijo(eat(IF)); 
                nodoIf.agregarHijo(eat(PAROPEN));
                switch (this.Tok) {
                    case ID:
                    case FLOAT:
                    case NUM:
                        nodoIf.agregarHijo(eat(this.Tok)); 
                        nodoIf.agregarHijo(eat(COMP));
                        switch (this.Tok) {
                            case ID:
                            case FLOAT:
                            case NUM:
                                nodoIf.agregarHijo(eat(this.Tok));
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(INSTRUCCION());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(INSTRUCCION());
                                break;
                            case PAROPEN:
                                nodoIf.agregarHijo(eat(PAROPEN)); 
                                nodoIf.agregarHijo(CALCULO()); 
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(INSTRUCCION());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(INSTRUCCION());
                                break;
                        } 
                        break;
                    case PAROPEN:
                        nodoIf.agregarHijo(eat(PAROPEN));
                        nodoIf.agregarHijo(CALCULO());
                        nodoIf.agregarHijo(eat(PARCLOSE));
                        nodoIf.agregarHijo(eat(COMP));
                        switch (this.Tok) {
                            case ID:
                            case FLOAT:
                            case NUM:
                                nodoIf.agregarHijo(eat(this.Tok));  
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(INSTRUCCION());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(INSTRUCCION());
                                break;
                            case PAROPEN:
                                nodoIf.agregarHijo(eat(PAROPEN)); 
                                nodoIf.agregarHijo(CALCULO());
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(INSTRUCCION());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(INSTRUCCION());
                                break;
                        } 
                        break;
                }       
                break;
            case PRINT:
                NodoParseTree nodoPrint = new NodoParseTree("PRINT");
                nodoPrint.agregarHijo(eat(PRINT)); 
                nodoPrint.agregarHijo(eat(PAROPEN));
                switch (this.Tok) {
                    case ID:
                        nodoPrint.agregarHijo(eat(ID)); 
                        nodoPrint.agregarHijo(eat(PARCLOSE)); 
                        nodoPrint.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(nodoPrint);
                        nodo.agregarHijo(INSTRUCCION());
                        break;
                    case CADENA:
                        nodoPrint.agregarHijo(eat(CADENA)); 
                        nodoPrint.agregarHijo(eat(PARCLOSE)); 
                        nodoPrint.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(nodoPrint);
                        nodo.agregarHijo(INSTRUCCION());
                        break;
                    case PAROPEN:
                        nodoPrint.agregarHijo(CALCULO()); 
                        nodoPrint.agregarHijo(eat(PARCLOSE)); 
                        nodoPrint.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(nodoPrint);
                        nodo.agregarHijo(INSTRUCCION());
                        break;
                }  
                break;
            case FOR:
                NodoParseTree nodoFor = new NodoParseTree("FOR");
                nodoFor.agregarHijo(eat(FOR));
                nodoFor.agregarHijo(eat(PAROPEN));
                if(this.Tok == TIPO_DATO_INT) {
                    nodoFor.agregarHijo(eat(TIPO_DATO_INT)); 
                    nodoFor.agregarHijo(eat(ID));   
                } else if(this.Tok == ID)    
                    nodoFor.agregarHijo(eat(ID));                     

                nodoFor.agregarHijo(eat(ASIG));
                switch (this.Tok) {
                    case PAROPEN:
                        nodoFor.agregarHijo(eat(PAROPEN)); 
                        nodoFor.agregarHijo(CALCULO()); 
                        nodoFor.agregarHijo(eat(PARCLOSE));
                        break;
                    case NUM:
                        nodoFor.agregarHijo(eat(NUM));
                        break;
                    case ID:
                        nodoFor.agregarHijo(eat(ID));
                        break;
                }
                nodoFor.agregarHijo(eat(EOL)); 
                switch (this.Tok) {
                    case PAROPEN:
                        nodoFor.agregarHijo(eat(PAROPEN)); 
                        nodoFor.agregarHijo(CALCULO()); 
                        nodoFor.agregarHijo(eat(PARCLOSE)); 
                        break;
                    case NUM:
                        nodoFor.agregarHijo(eat(NUM));
                        break;
                    case ID:
                        nodoFor.agregarHijo(eat(ID));
                        break;
                }
                nodoFor.agregarHijo(eat(COMP));
                switch (this.Tok) {
                    case PAROPEN:
                        nodoFor.agregarHijo(eat(PAROPEN)); 
                        nodoFor.agregarHijo(CALCULO()); 
                        nodoFor.agregarHijo(eat(PARCLOSE)); 
                        break;
                    case NUM:
                        nodoFor.agregarHijo(eat(NUM));
                        break;
                    case ID:
                        nodoFor.agregarHijo(eat(ID));
                        break;
                }
                nodoFor.agregarHijo(eat(EOL));
                nodoFor.agregarHijo(eat(ID));
                if(this.Tok == INC) nodoFor.agregarHijo(eat(INC));
                if(this.Tok == DEC) nodoFor.agregarHijo(eat(DEC));
                nodoFor.agregarHijo(eat(PARCLOSE));
                nodoFor.agregarHijo(eat(LLAVEOPEN));
                nodoFor.agregarHijo(INSTRUCCION());
                nodoFor.agregarHijo(eat(LLAVECLOSE));
                nodo.agregarHijo(nodoFor);
                nodo.agregarHijo(INSTRUCCION());
                break;
        }
        return nodo;
    }

    private NodoParseTree handleElse() {
        if (this.Tok == ELSE) {
            NodoParseTree nodoElse = new NodoParseTree("ELSE");
            nodoElse.agregarHijo(eat(ELSE));
            nodoElse.agregarHijo(eat(LLAVEOPEN));
            nodoElse.agregarHijo(INSTRUCCION());
            nodoElse.agregarHijo(eat(LLAVECLOSE));
            return nodoElse;
        }
        return null;
    }

    public NodoParseTree CALCULO() {
        NodoParseTree nodo = new NodoParseTree("CALCULO");
        
        switch (this.Tok) {
            case ID:
            case FLOAT:
            case NUM:
                nodo.agregarHijo(eat(this.Tok)); 
                break;
            case PAROPEN:
                nodo.agregarHijo(eat(PAROPEN)); 
                nodo.agregarHijo(CALCULO()); 
                nodo.agregarHijo(eat(PARCLOSE)); 
                if (this.Tok == OPER) {
                    nodo.agregarHijo(eat(OPER));
                    if (this.Tok == PAROPEN) {
                        System.out.println("Entrando a CALCULO despues de parentesis");
                        nodo.agregarHijo(eat(PAROPEN)); 
                        nodo.agregarHijo(CALCULO()); 
                        nodo.agregarHijo(eat(PARCLOSE)); 
                    } else if (this.Tok == ID || this.Tok == FLOAT || this.Tok == NUM) {
                        nodo.agregarHijo(eat(this.Tok));
                    }
                }
                break;
        }
        while (this.Tok == OPER) {
            nodo.agregarHijo(eat(OPER));
            switch (this.Tok) {
                case ID:
                case FLOAT:
                case NUM:
                    nodo.agregarHijo(eat(this.Tok)); 
                    break;
                case PAROPEN:
                    nodo.agregarHijo(eat(PAROPEN)); 
                    nodo.agregarHijo(CALCULO());
                    nodo.agregarHijo(eat(PARCLOSE)); 
                    break;
                default:
                    return nodo;
            }
        }
        return nodo;
    }

    boolean esReturnable(TokenType t) {
        return 
            t == TokenType.VOID   ||
            t == TokenType.INT    ||
            t == TokenType.CHAR   ||
            t == TokenType.DOUBLE ||
            t == TokenType.FLOAT  ||
            t == TokenType.LONG   ||
            t == TokenType.SHORT;
    }

    boolean esTipo(TokenType t) {
        return
            t == TokenType.INT    ||
            t == TokenType.CHAR   ||
            t == TokenType.DOUBLE ||
            t == TokenType.FLOAT  ||
            t == TokenType.LONG   ||
            t == TokenType.SHORT;
    }

    boolean esFuncion(TokenType t) {
        return
            t == TokenType.IDENTIFIER &&
            tokens.get(iterator+1).getTokenType() == TokenType.LPAREN;
    }
    public NodoParseTree eat(TokenType tok) {
        if (this.ParserError) return null;
    
        if (iterator >= this.tokens.size() || iterator < 0) 
            return null;
        
        if (this.Tok.equals(tok)) {
            String lexema = "";
            if (iterator < tokens.size()) 
                lexema = tokens.get(iterator).getLexema();
            
            System.out.println("Token reconocido: " + this.Tok.getLexema() + " " + this.Tok.getTokenType().name());
            
            // Crear nodo con tipo, valor y lexema
            NodoParseTree nodo = new NodoParseTree(this.Tok.getTokenType().name(), null, lexema, lineaActual);
            
            Avanzar();
            return nodo;
        } else {
            Error(tok);
            return null;
        }
    }

    private void Avanzar() {
        iterator++;    

        if (iterator < tokens.size()) 
            this.Tok = tokens.get(iterator);
        else 
            System.out.println("Sin errores de Parser");
    }
    
    private void Error(Token tok) {
        this.setParserError(true);        
        System.out.println("Token inesperado: " + this.Tok.getTokenType().name() + " " + this.Tok.getLexema() + " en la línea " + this.lineaActual);
        System.out.println("Se esperaba " + tok.getTokenType().name() + " " + tok.getLexema());
    }

    public boolean isParserError() {
        return ParserError;
    }

    public void setParserError(boolean parserError) {
        this.ParserError = parserError;
    }

    public NodoParseTree getArbolSintactico() {
        return this.arbolSintactico;
    }
       
}