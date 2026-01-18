package com.compiler.Engine;
import com.compiler.Engine.ast.*;
import com.compiler.Engine.compiler.escaner.Escaner;

import java.util.ArrayList;

import org.fxmisc.richtext.CodeArea;

public class Parser {

    private final int IF = 0, PRINT = 1, INPUTINT = 2, INPUTFLOAT = 3, INPUTSTRING = 4, ELSE = 5, TIPO_DATO_INT = 6, 
        TIPO_DATO_FLOAT = 7, TIPO_DATO_STRING = 8, ID = 9, FLOAT = 10, NUM = 11, COMP = 12, ASIG = 13, OPER = 14, 
        PAROPEN = 16, PARCLOSE = 17, LLAVEOPEN = 18, LLAVECLOSE = 19, EOL = 20, CADENA = 21, FOR = 22,
        INC = 23, DEC = 24, MAIN = 25, DIR = 26;

    private final String[] Words = {"if", "print", "inputInt", "inputFloat", "inputString", "else", "\"TIPO_INT\"", 
    "\"TIPO_FLOAT\"", "\"TIPO_STRING\"", "ID", "FLOAT", "NUM", "COMPARADOR", "=", "OPERADOR", "\"$$\"", "(", 
    ")", "{", "}", ";", "CADENA", "for", "++", "--", "main", "#"};
    
    private int i = 0;
    private int Tok;
    private boolean ParserError = false;
    private ArrayList<String> Calculos = new ArrayList<>();
    private final Escaner Escaneado;
    private int lineaActual = 1;

    // Árbol sintáctico
    private NodoParseTree arbolSintactico;

    public Parser(Escaner escaneado, CodeArea codeAreaParser) {
        Escaneado = escaneado;
    }

    public boolean P() {
        this.Tok = (int) Escaneado.tokens.get(i);
        
        // Crear nodo raíz del árbol
        arbolSintactico = new NodoParseTree("PROGRAMA");

        if (Tok == DIR) {
             NodoParseTree nodoX = eat(DIR);
            System.out.println(nodoX.toString());
        }
       
        
        // Verificar main
        NodoParseTree nodoMain = eat(MAIN);
        if (nodoMain == null) {
            Error();
            System.out.println("Error: Se esperaba 'main' al inicio del programa");
            return false;
        }
        arbolSintactico.agregarHijo(nodoMain);
        
        // Verificar (
        NodoParseTree nodoParOpen = eat(PAROPEN);
        if (nodoParOpen == null) {
            Error();
            System.out.println("Error: Se esperaba '(' después de main");
            return false;
        }
        arbolSintactico.agregarHijo(nodoParOpen);
        
        // Verificar )
        NodoParseTree nodoParClose = eat(PARCLOSE);
        if (nodoParClose == null) {
            Error();
            System.out.println("Error: Se esperaba ')' después de '('");
            return false;
        }
        arbolSintactico.agregarHijo(nodoParClose);
        
        // Verificar {
        NodoParseTree nodoLlaveOpen = eat(LLAVEOPEN);
        if (nodoLlaveOpen == null) {
            Error();
            System.out.println("Error: Se esperaba '{' después de main()");
            return false;
        }
        arbolSintactico.agregarHijo(nodoLlaveOpen);
        
        // Procesar estatutos
        NodoParseTree nodoEstatuto = ESTATUTO();
        if (nodoEstatuto != null) arbolSintactico.agregarHijo(nodoEstatuto);
        System.out.println("Terminó Estatuto en P");
        
        // Verificar } final - CRÍTICO
        NodoParseTree nodoLlaveClose = eat(LLAVECLOSE);
        if (nodoLlaveClose == null) {
            if (!this.ParserError) {
                Error();
            }
            System.out.println("Error: Se esperaba '}' al final del programa");
            return false;
        }
        arbolSintactico.agregarHijo(nodoLlaveClose);
        
        return !this.ParserError;
    }    
    
    public NodoParseTree getArbolSintactico() {
        return this.arbolSintactico;
    }
       
    public NodoParseTree DECLARACION() {
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
                return ESTATUTO();
        }
        return nodo;
    }

    public NodoParseTree ESTATUTO() {
        if(this.ParserError) return null;

        NodoParseTree nodo = new NodoParseTree("ESTATUTO");
        
        switch (this.Tok) {
            case TIPO_DATO_INT:
            case TIPO_DATO_FLOAT: 
            case TIPO_DATO_STRING:
                nodo.agregarHijo(DECLARACION()); 
                nodo.agregarHijo(ESTATUTO()); 
                break;
            case ID:
                nodo.agregarHijo(eat(ID)); 
                nodo.agregarHijo(eat(ASIG)); 
                nodo.agregarHijo(CALCULO()); 
                nodo.agregarHijo(eat(EOL)); 
                nodo.agregarHijo(ESTATUTO()); 
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
                                nodoIf.agregarHijo(ESTATUTO());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(ESTATUTO());
                                break;
                            case PAROPEN:
                                nodoIf.agregarHijo(eat(PAROPEN)); 
                                nodoIf.agregarHijo(CALCULO()); 
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(ESTATUTO());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(ESTATUTO());
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
                                nodoIf.agregarHijo(ESTATUTO());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(ESTATUTO());
                                break;
                            case PAROPEN:
                                nodoIf.agregarHijo(eat(PAROPEN)); 
                                nodoIf.agregarHijo(CALCULO());
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(PARCLOSE));
                                nodoIf.agregarHijo(eat(LLAVEOPEN));
                                nodoIf.agregarHijo(ESTATUTO());
                                nodoIf.agregarHijo(eat(LLAVECLOSE));
                                nodoIf.agregarHijo(handleElse());
                                nodo.agregarHijo(nodoIf);
                                nodo.agregarHijo(ESTATUTO());
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
                        nodo.agregarHijo(ESTATUTO());
                        break;
                    case CADENA:
                        nodoPrint.agregarHijo(eat(CADENA)); 
                        nodoPrint.agregarHijo(eat(PARCLOSE)); 
                        nodoPrint.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(nodoPrint);
                        nodo.agregarHijo(ESTATUTO());
                        break;
                    case PAROPEN:
                        nodoPrint.agregarHijo(CALCULO()); 
                        nodoPrint.agregarHijo(eat(PARCLOSE)); 
                        nodoPrint.agregarHijo(eat(EOL)); 
                        nodo.agregarHijo(nodoPrint);
                        nodo.agregarHijo(ESTATUTO());
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
                nodoFor.agregarHijo(ESTATUTO());
                nodoFor.agregarHijo(eat(LLAVECLOSE));
                nodo.agregarHijo(nodoFor);
                nodo.agregarHijo(ESTATUTO());
                break;
        }
        return nodo;
    }

    private NodoParseTree handleElse() {
        if (this.Tok == ELSE) {
            NodoParseTree nodoElse = new NodoParseTree("ELSE");
            nodoElse.agregarHijo(eat(ELSE));
            nodoElse.agregarHijo(eat(LLAVEOPEN));
            nodoElse.agregarHijo(ESTATUTO());
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

    public NodoParseTree eat(int tok) {
        if (this.ParserError) return null;
    
        if (i >= Escaneado.tokens.size() || i < 0) {
            return null;
        }
        if (this.Tok == tok) {
            String lexema = "";
            if (i < Escaneado.lexemas.size()) {
                lexema = Escaneado.lexemas.get(i);
            }
            System.out.println("Token reconocido: " + this.Tok + " " + Words[this.Tok]);
            
            // Crear nodo con tipo, valor y lexema
            NodoParseTree nodo = new NodoParseTree(Words[this.Tok], Words[this.Tok], lexema, lineaActual);
            
            Avanzar();
            return nodo;
        } else {
            Error();
            return null;
        }
    }

    private void Avanzar() {
        i++;    
        if (i < Escaneado.tokens.size()) {
            this.Tok = (int) Escaneado.tokens.get(i);
        } else {
            System.out.println("Sin errores de Parser");
        }
    }
    
    private void Error() {
        this.setParserError(true);        
        System.out.println("Token inesperado: " + this.Tok + " " + Words[this.Tok]);
    }

    public boolean isParserError() {
        return ParserError;
    }

    public void setParserError(boolean parserError) {
        this.ParserError = parserError;
    }

    public ArrayList<String> getCalculos() {
        return Calculos;
    }

    public void setCalculos(ArrayList<String> calculos) {
        Calculos = calculos;
    }

    public void setCalcTok(int Tok) {
        Escaneado.Scan();
    }

    public String[] getWords() {
        return Words;
    }
}