package com.compiler.Engine.compiler.parser;
import com.compiler.Engine.compiler.escaner.Token;
import com.compiler.Engine.compiler.escaner.TokenType;
import com.compiler.Engine.compiler.parser.exceptions.ExpressionException;
import com.compiler.Engine.compiler.parser.exceptions.ParserException;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;


public class Parser {

    private LinkedList<Token> tokens = new LinkedList<>();
    private boolean ParserError = false;
    private int lineaActual = 1;
    private TokenType Tok;
    private static int iterator = 0;
    private NodoParseTree arbolSintactico;

    public Parser(LinkedList<Token> tokens) {
        this.tokens = tokens;
    }

    // "El método" con mayuscula
   public void parse() throws ParserException {
        try {
            PROGRAMA();
            
            if (!this.ParserError && this.Tok != TokenType.EOF) {
                System.err.println("Advertencia: tokens restantes despues del análisis");
            }
        } catch (Exception e) {
            // Mostrar árbol parcial
            if (this.arbolSintactico != null) {
                System.out.println("\n---------- ÁRBOL SINTACTICO PARCIAL ----------");
                System.out.println(arbolSintactico.toString());
            }
            
            throw new ParserException("Error durante el analisis sintáctico");
        }
    }

    private NodoParseTree PROGRAMA() throws ExpressionException {

        // Crear nodo raíz del árbol
        NodoParseTree nodoPadre = new NodoParseTree("PROGRAMA");
        
        this.arbolSintactico = nodoPadre;

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

    private NodoParseTree FUNCION() throws ExpressionException{

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

    private NodoParseTree INSTRUCCION() throws ExpressionException{

        System.out.println("--------- INSTRUCCION -----------");
        NodoParseTree nodoInstruccion = new NodoParseTree("INSTRUCCION");

        if (this.Tok == TokenType.RBRACE || this.Tok == TokenType.EOF) 
            return nodoInstruccion;        

        // DECLARACION
        if (esTipo(this.Tok)) {
            DECLARACION();              
            System.out.println("DESPUES DE DECL");
        }

        // LLAMADA FUNCION
        else if (esFuncion(this.Tok)){ 
            CALL();
        }
        
        // ASIGNACION
        else if (this.Tok == TokenType.IDENTIFIER) {
             ASIGNACION();
        } 
        

        // FOR
        else if (this.Tok == TokenType.FOR) {
            FOR();
        }
        
        // IF
        else if (this.Tok == TokenType.IF) {
            
        }
        
        // WHILE
        
        // DO-WHILE
        // SWITCH

        System.out.println("<<<<<<< FIN INSTRUCCION >>>>>>>>");
        
        return INSTRUCCION();
    }

    private NodoParseTree FOR() {
        NodoParseTree forNodo = new NodoParseTree("FOR");

        forNodo.agregarHijo(eat(TokenType.FOR));
        forNodo.agregarHijo(eat(TokenType.LPAREN));
        forNodo.agregarHijo(DECLARACION());

        return forNodo;
    }
    
    private NodoParseTree ASIGNACION() {
        
        NodoParseTree asigNodo = new NodoParseTree("ASIGNACION");

        // asignación a arreglo
        if (esElementoArray(this.Tok))  // Asignación a arreglo arreglo[a] = CALCULO();
            asigNodo.agregarHijo(ARRAY_ACCESS());
        else 
            asigNodo.agregarHijo(eat(TokenType.IDENTIFIER)); 

        asigNodo.agregarHijo(eat(TokenType.ASSIGN));
        try {
            asigNodo.agregarHijo(CALCULO());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        asigNodo.agregarHijo(eat(TokenType.SEMICOLON));

        return asigNodo;
    }

    private NodoParseTree DECLARACION() {

        System.out.println("--------- DECLARACIÓN ---------");
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
                try {
                    decl.agregarHijo(CALCULO(TokenType.RBRACKET));
                } catch (ExpressionException e) {
                    e.printStackTrace();
                } // tamaño
            
            decl.agregarHijo(eat(TokenType.RBRACKET));

            // inicialización de arreglo
            if (this.Tok == TokenType.ASSIGN) {
                eat(TokenType.ASSIGN);
                try {
                    decl.agregarHijo(INIT_ARRAY());
                } catch (ExpressionException e) {
                    e.printStackTrace();
                }
            }
        }

        // inicialización normal
        else if (this.Tok == TokenType.ASSIGN) {
            NodoParseTree init = new NodoParseTree("INIT");
            init.agregarHijo(eat(TokenType.ASSIGN));
            try {
                init.agregarHijo(CALCULO(TokenType.SEMICOLON));
            } catch (ExpressionException e) {
                e.printStackTrace();
            }
            decl.agregarHijo(init);
        }

        decl.agregarHijo(eat(TokenType.SEMICOLON));

        System.out.println("-   RETORNANDO DECLARACIÓN ... token: " + this.Tok.name());
        return decl;
    }

    private NodoParseTree INIT_ARRAY() throws ExpressionException {

        NodoParseTree initNode = new NodoParseTree("INIT_ARRAY");

        initNode.agregarHijo(eat(TokenType.LBRACE));

        if (this.Tok != TokenType.RBRACE) {

            initNode.agregarHijo(CALCULO(TokenType.COMMA, TokenType.RBRACE));

            while (this.Tok == TokenType.COMMA) {
                initNode.agregarHijo(eat(TokenType.COMMA));
                initNode.agregarHijo(CALCULO(TokenType.COMMA, TokenType.RBRACE));
            }
        }

        initNode.agregarHijo(eat(TokenType.RBRACE));
        return initNode;
    }

    private static Stack<NodoParseTree> operandos = new Stack<>();
    private static Stack<NodoParseTree> operadores = new Stack<>();

    private NodoParseTree CALCULO(TokenType... limites) throws ExpressionException {

        System.out.println("---------- CALCULO ----------");

        Set<TokenType> limitesSet = Set.of(limites);        
        NodoParseTree nodoCalculo = new NodoParseTree("CALCULO");
        operandos.clear();
        operadores.clear();
        int nivelParentesis = 0; // Contador de paréntesis de agrupación
        
        while (this.Tok != TokenType.SEMICOLON && 
           this.Tok != TokenType.EOF && 
           !limitesSet.contains(this.Tok)) {
            
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
                    continue;
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
                else if (limitesSet.contains(TokenType.RPAREN)) {
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
                if (limitesSet.contains(TokenType.COMMA)) 
                    break;
                
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
            System.out.println("AGREGANDO HIJO SOLO");
            nodoCalculo.agregarHijo(operandos.pop());
        }

        return nodoCalculo;
    }

    // Sobrecarga para mantener compatibilidad
    private NodoParseTree CALCULO() throws ExpressionException{
        return CALCULO(TokenType.SEMICOLON);
    }

    private void construirSubArbol() {

        System.out.println(" CONSTRUYENDO SUBÁRBOL ...");
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
    private NodoParseTree CALL() throws ExpressionException {  
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

        nodoAccessoArray.agregarHijo(eat(TokenType.IDENTIFIER));
        nodoAccessoArray.agregarHijo(eat(TokenType.LBRACKET));
        try {
            nodoAccessoArray.agregarHijo(CALCULO(TokenType.RBRACKET));
        } catch (ExpressionException e) {
            e.printStackTrace();
        }
        nodoAccessoArray.agregarHijo(eat(TokenType.RBRACKET));
        
        return nodoAccessoArray;
    }

    private NodoParseTree ATRIB() throws ExpressionException {

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

    private NodoParseTree ARG() throws ExpressionException {

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
                break;
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
            nodoHijo.agregarHijo(eat(TokenType.LIB_ID));
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
            
            System.out.println("Parser: Token reconocido: " + this.Tok.name());
            
            // Crear nodo con tipo, valor y lexema
            NodoParseTree nodo = new NodoParseTree(this.Tok.name(), null, lexema, lineaActual);
            
            Avanzar();
            return nodo;
        } else {
            System.err.println("\n========== ERROR DE SINTAXIS ==========");
            System.err.println("Línea " + lineaActual);
            System.err.println("Se esperaba: " + tok.name());
            System.err.println("Se obtuvo: " + this.Tok.name());
            
            // Mostrar contexto
            if (iterator < tokens.size()) {
                System.err.println("Lexema actual: " + tokens.get(iterator).getLexema());
            }
            System.err.println("=======================================\n");
            
            this.ParserError = true;
            
            // Mostrar el árbol sintáctico parcial
            if (this.arbolSintactico != null) {
                System.out.println("\n========== ÁRBOL SINTÁCTICO PARCIAL ==========");
                imprimirArbol(this.arbolSintactico, "", true);
                System.out.println("==============================================\n");
            }
            
            System.exit(1);
            return null;
        }
    }

    private void imprimirArbol(NodoParseTree nodo, String prefijo, boolean esUltimo) {
        if (nodo == null) return;
        
        System.out.print(prefijo);
        System.out.print(esUltimo ? "└── " : "├── ");
        
        // Mostrar información del nodo
        String info = nodo.getTipo();
        if (nodo.getLexema() != null && !nodo.getLexema().isEmpty()) {
            info += " [" + nodo.getLexema() + "]";
        }
        if (nodo.getLinea() > 0) {
            info += " (linea " + nodo.getLinea() + ")";
        }
        System.out.println(info);
        
        // Imprimir hijos
        List<NodoParseTree> hijos = nodo.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            imprimirArbol(hijos.get(i), nuevoPrefijo, ultimo);
        }
    }
    private void Avanzar() {
        iterator++;    

        if (iterator < tokens.size()) 
            this.Tok = tokens.get(iterator).getTokenType();
        else 
            System.out.println("Sin errores de Parser");
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