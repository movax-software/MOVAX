package com.compiler.Engine.compiler.parser;
import com.compiler.Engine.ast.*;
import com.compiler.Engine.compiler.escaner.Escaner;
import com.compiler.Engine.compiler.escaner.Token;
import com.compiler.Engine.compiler.escaner.TokenType;
import com.compiler.Engine.compiler.parser.exceptions.ExpressionException;
import com.compiler.Engine.compiler.parser.exceptions.ParserException;

import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

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

    // "El método" con mayuscula
    public void parse() throws ParserException {
        this.arbolSintactico = PROGRAMA();

        if (!this.ParserError && this.Tok != TokenType.EOF) {
            error("Error: Símbolo inesperado después del programa en línea " + lineaActual);
        }
    }

    private NodoParseTree PROGRAMA()  {

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

        NodoParseTree nodoInstruccion = new NodoParseTree("INSTRUCCION");

        if (this.Tok == TokenType.RBRACE || this.Tok == TokenType.EOF) 
            return nodoInstruccion;        

        // DECLARACION
        if (esTipo(this.Tok)) 
            return DECLARACION();            
        
        
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
    
    private NodoParseTree DECLARACION() {

        NodoParseTree decl = new NodoParseTree("DECLARACION");

        // tipo
        decl.agregarHijo(eat(this.Tok));

        // identificador
        NodoParseTree id = eat(TokenType.IDENTIFIER);
        decl.agregarHijo(id);

        // arreglo
        if (this.Tok == TokenType.LBRACKET) {
            decl.agregarHijo(eat(TokenType.LBRACKET));

            if (this.Tok != TokenType.RBRACKET) 
                decl.agregarHijo(CALCULO(TokenType.RBRACKET)); // tamaño
            
            decl.agregarHijo(eat(TokenType.RBRACKET));

            // inicialización de arreglo
            if (this.Tok == TokenType.ASSIGN) {
                eat(TokenType.ASSIGN);
                decl.agregarHijo(INIT_ARRAY());
            }
        }

        // inicialización normal
        else if (this.Tok == TokenType.ASSIGN) {
            NodoParseTree init = new NodoParseTree("INIT");
            eat(TokenType.ASSIGN);
            init.agregarHijo(CALCULO(TokenType.SEMICOLON));
            decl.agregarHijo(init);
        }

        decl.agregarHijo(eat(TokenType.SEMICOLON));
        return decl;
    }

    private NodoParseTree INIT_ARRAY() {

        NodoParseTree initNode = new NodoParseTree("INIT_ARRAY");

        eat(TokenType.LBRACE);

        if (this.Tok != TokenType.RBRACE) {

            initNode.agregarHijo(
                CALCULO(Set.of(TokenType.COMMA, TokenType.RBRACE))
            );

            while (this.Tok == TokenType.COMMA) {
                eat(TokenType.COMMA);
                initNode.agregarHijo(
                    CALCULO(Set.of(TokenType.COMMA, TokenType.RBRACE))
                );
            }
        }

        eat(TokenType.RBRACE);
        return initNode;
    }

    private static Stack<NodoParseTree> operandos = new Stack<>();
    private static Stack<NodoParseTree> operadores = new Stack<>();

    private NodoParseTree CALCULO(TokenType limite) {
        NodoParseTree nodoCalculo = new NodoParseTree("CALCULO");
        operandos.clear();
        operadores.clear();
        int nivelParentesis = 0; // Contador de paréntesis de agrupación
        
        while (this.Tok != TokenType.SEMICOLON && 
            this.Tok != TokenType.EOF && 
            this.Tok != limite) {
            
            // Procesar operandos
            if (esOperando(this.Tok)) {
                if (esFuncion(this.Tok)) {
                    operandos.push(CALL());
                } 
                else if (esElementoArray(this.Tok)) {
                    operandos.push(ARRAY_ACCESS());    
                } 
                else {
                    operandos.push(eat(this.Tok));
                }
            }
            // Procesar operadores binarios
            else if (esOperadorBinario(this.Tok)) {
                while (!operadores.isEmpty() && 
                    !operadores.peek().getTipo().equals("LPAREN") &&
                    getPrecedencia(operadores.peek().getTipo()) >= getPrecedencia(this.Tok.name())) {
                        
                    construirSubArbol();
                }
                operadores.push(eat(this.Tok));
            }
            // Paréntesis izquierdo
            else if (this.Tok == TokenType.LPAREN) {
                operadores.push(eat(TokenType.LPAREN));
                nivelParentesis++; // Incrementar contador
            }
            // Paréntesis derecho
            else if (this.Tok == TokenType.RPAREN) {
                // Si es un paréntesis de agrupación interno, procesarlo
                if (nivelParentesis > 0) {
                    while (!operadores.isEmpty() && 
                        !operadores.peek().getTipo().equals("LPAREN")) {
                        construirSubArbol();
                    }
                    
                    if (!operadores.isEmpty() && 
                        operadores.peek().getTipo().equals("LPAREN")) {
                        operadores.pop();
                        nivelParentesis--; // Decrementar contador
                    }
                    
                    eat(TokenType.RPAREN);
                } 
                // Si no hay paréntesis internos y el límite es RPAREN, salir
                else if (limite == TokenType.RPAREN) {
                    break;
                }
                // Paréntesis inesperado
                else {
                    System.err.println("Error: Paréntesis de cierre inesperado en línea " + lineaActual);
                    this.ParserError = true;
                    break;
                }
            }
            // Coma: si es el límite, salir
            else if (this.Tok == TokenType.COMMA) {
                if (limite == TokenType.COMMA) 
                    break;
                
                error("Error: Coma inesperada en línea " + lineaActual);
                this.ParserError = true;
                break;
            }
            else {
                break;
            }
        }

        // Verificar paréntesis balanceados
        if (nivelParentesis > 0) {
            System.err.println("Error: Paréntesis sin cerrar en línea " + lineaActual);
            this.ParserError = true;
            return null;
        }

        // Procesar operadores restantes
        while (!operadores.isEmpty()) {
            if (operadores.peek().getTipo().equals("LPAREN")) {
                this.ParserError = true;
                System.err.println("Error: Paréntesis sin cerrar en expresión en línea " + lineaActual);
                throw new ExpressionException("Error: Paréntesis sin cerrar en expresión en línea " + lineaActual);
            }
            construirSubArbol();
        }

        if (!operandos.isEmpty()) {
            nodoCalculo.agregarHijo(operandos.pop());
        }

        return nodoCalculo;
    }

    // Sobrecarga para mantener compatibilidad
    private NodoParseTree CALCULO() throws ExpressionException{
        return CALCULO(TokenType.SEMICOLON);
    }

    private void construirSubArbol() {
        if (operadores.isEmpty()) {
            System.err.println("Error: Falta operador en línea " + lineaActual);
            this.ParserError = true;
            return;
        }
        
        if (operandos.size() < 2) {
            System.err.println("Error: Faltan operandos para el operador en línea " + lineaActual);
            this.ParserError = true;
            return;
        }
        
        NodoParseTree operador = operadores.pop();
        NodoParseTree derecho = operandos.pop();
        NodoParseTree izquierdo = operandos.pop();
        
        // El operador es la raíz, con los operandos como hijos
        operador.agregarHijo(izquierdo);
        operador.agregarHijo(derecho);
        
        // Empujar el subárbol completo de vuelta a operandos
        operandos.push(operador);
    }

    private int getPrecedencia(String tipoToken) {
        try {
            TokenType tipo = TokenType.valueOf(tipoToken);
            return tipo.getPrecedencia();
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    // llamada a funciones
    private NodoParseTree CALL() {  
        NodoParseTree nodoLlamada = new NodoParseTree("LLAMADA");
        
        nodoLlamada.agregarHijo(eat(TokenType.IDENTIFIER));
        nodoLlamada.agregarHijo(eat(TokenType.LPAREN));
        
        // Procesar argumentos usando CALCULO
        if (this.Tok != TokenType.RPAREN) {
            NodoParseTree argumentos = new NodoParseTree("ARGUMENTOS");
            
            // Primer argumento
            argumentos.agregarHijo(CALCULO(TokenType.COMMA));
            
            // Argumentos adicionales
            while (this.Tok == TokenType.COMMA) {
                eat(TokenType.COMMA);
                argumentos.agregarHijo(CALCULO(TokenType.COMMA));
            }
            
            nodoLlamada.agregarHijo(argumentos);
        }
        
        nodoLlamada.agregarHijo(eat(TokenType.RPAREN));
        
        return nodoLlamada;
    }

    private NodoParseTree ARRAY_ACCESS() {
        NodoParseTree nodoAccessoArray = new NodoParseTree("ARRAY_ACCESS");

        nodoAccessoArray.agregarHijo(TokenType.IDENTIFIER);
        nodoAccessoArray.agregarHijo(TokenType.LBRACKET);
        nodoAccessoArray.agregarHijo(CALCULO(TokenType.RBRACKET));
        nodoAccessoArray.agregarHijo(TokenType.RBRACKET);
        
        return nodoAccessoArray;
    }

    private NodoParseTree ATRIB() {

        NodoParseTree args = new NodoParseTree("ARGS");

        if (this.Tok == TokenType.RPAREN)
            return args;

        do {
            args.agregarHijo(ARG());

            if (this.Tok == TokenType.COMMA)
                eat(TokenType.COMMA);
            else
                break;

        } while (true);

        return args;
    }

    private NodoParseTree ARG() {

        NodoParseTree argNode = new NodoParseTree("ARG");

        if (esFuncion(this.Tok)) {
            argNode.agregarHijo(CALL());
            return argNode;
        }

        switch (this.Tok) {
            case IDENTIFIER:
            case NUMBER:
            case REAL:
                argNode.agregarHijo(CALCULO());
                break;

            case STRING:
                argNode.agregarHijo(eat(TokenType.STRING));
                break;

            default:
                error("Argumento inválido");
        }

        return argNode;
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

    private boolean esReturnable(TokenType t) {
        return 
            t == TokenType.VOID   ||
            t == TokenType.INT    ||
            t == TokenType.CHAR   ||
            t == TokenType.DOUBLE ||
            t == TokenType.FLOAT  ||
            t == TokenType.LONG   ||
            t == TokenType.SHORT;
    }

    private boolean esTipo(TokenType t) {
        return
            t == TokenType.INT    ||
            t == TokenType.CHAR   ||
            t == TokenType.DOUBLE ||
            t == TokenType.FLOAT  ||
            t == TokenType.LONG   ||
            t == TokenType.SHORT;
    }

    private boolean esOperadorBinario(TokenType t) {
        return 
            t == TokenType.PLUS   ||
            t == TokenType.MINUS  ||
            t == TokenType.MUL    ||
            t == TokenType.DIV    ||
            t == TokenType.MOD    ||
            t == TokenType.SHL    ||
            t == TokenType.SHR   ||
            t == TokenType.AND   ||
            t == TokenType.OR    ||
            t == TokenType.XOR    ||
            t == TokenType.LAND    ||
            t == TokenType.LOR;
            
    }

    private boolean esOperando(TokenType t) {
        return
            t == TokenType.NUMBER       ||
            t == TokenType.REAL         ||
            t == TokenType.IDENTIFIER   ||
            esFuncion(t);
            
    }
    
    private boolean esOperadorUnario(TokenType t) {
        return 
            t == TokenType.INC   ||
            t == TokenType.DEC;
            
    }

    private boolean esFuncion(TokenType t) {
        return
            t == TokenType.IDENTIFIER &&
            tokens.get(iterator+1).getTokenType() == TokenType.LPAREN;
    }
    
    private boolean esElementoArray(TokenType t) {
        return
            t == TokenType.IDENTIFIER &&
            tokens.get(iterator+1).getTokenType() == TokenType.LBRACKET;
    }

    private NodoParseTree eat(TokenType tok) {
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
    
    private void error(String msg) {
        System.out.println("Error: " + msg);
        ParserError = true;
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