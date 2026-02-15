package com.compiler.Engine.compiler.parser;
import com.compiler.Engine.compiler.escaner.Token;
import com.compiler.Engine.compiler.escaner.TokenType;
import com.compiler.Engine.compiler.parser.exceptions.ExpressionException;
import com.compiler.Engine.compiler.parser.exceptions.ParserException;

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
        PROGRAMA();
        
        if (!this.ParserError && this.Tok != TokenType.EOF) 
            System.err.println("Advertencia: tokens restantes despues del análisis");
        
        // Mostrar árbol parcial
        if (this.arbolSintactico != null) {
            System.out.println("\n---------- ÁRBOL SINTACTICO PARCIAL ----------");
            System.out.println(arbolSintactico.toString());
        }
        
        if (this.ParserError) {
            throw new ParserException("Error de sintaxis en línea " + lineaActual);
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

    private NodoParseTree FUNCION()  {

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

   private NodoParseTree INSTRUCCION() {

        System.out.println("\n--------- INSTRUCCION -----------");
        System.out.println(">>> Token actual al iniciar INSTRUCCION: " + this.Tok);
        NodoParseTree nodoInstruccion = new NodoParseTree("INSTRUCCION");

        if (this.Tok == TokenType.RBRACE || this.Tok == TokenType.EOF) 
            return nodoInstruccion;        

        // DECLARACION
        if (esTipo(this.Tok)) {
            nodoInstruccion.agregarHijo(DECLARACION());
            nodoInstruccion.agregarHijo(eat(TokenType.SEMICOLON));
        }
        // LLAMADA FUNCION
        else if (esFuncion(this.Tok)){ 
            nodoInstruccion.agregarHijo(CALL());
            nodoInstruccion.agregarHijo(eat(TokenType.SEMICOLON));
        }
        // ASIGNACION
        else if (this.Tok == TokenType.IDENTIFIER) {
            nodoInstruccion.agregarHijo(ASIGNACION());
            // ASIGNACION ya consume el SEMICOLON, no lo vuelvas a comer aquí
        }
        // FOR
        else if (this.Tok == TokenType.FOR) {
            nodoInstruccion.agregarHijo(FOR());
            // FOR no termina con SEMICOLON
        }
        // IF
        else if (this.Tok == TokenType.IF) {
            nodoInstruccion.agregarHijo(IF());
            // IF no termina con SEMICOLON
        }
        else {
            System.out.println("\nÁrbol parcial actual: " + this.arbolSintactico.imprimirArbol());
            System.err.println("Error: Token inesperado en INSTRUCCION: " + this.Tok);
            this.ParserError = true;
            return null;
        }
        System.out.println("<<<<<<< FIN INSTRUCCION >>>>>>>>");
        
        return INSTRUCCION();
    }

    private NodoParseTree IF() {
        System.out.println("\n========== IF ==========");
        NodoParseTree ifNodo = new NodoParseTree("IF");
        
        ifNodo.agregarHijo(eat(TokenType.IF));
        ifNodo.agregarHijo(eat(TokenType.LPAREN));
        
        ifNodo.agregarHijo(CALCULO());
        
        ifNodo.agregarHijo(eat(TokenType.RPAREN));
        
        // Cuerpo del if
        ifNodo.agregarHijo(eat(TokenType.LBRACE));
        
        NodoParseTree cuerpoIf = new NodoParseTree("IF_BODY");
        while (this.Tok != TokenType.RBRACE && this.Tok != TokenType.EOF && !this.ParserError) {
            NodoParseTree instr = INSTRUCCION();
            if (instr != null) 
                cuerpoIf.agregarHijo(instr);
        }
        ifNodo.agregarHijo(cuerpoIf);
        
        ifNodo.agregarHijo(eat(TokenType.RBRACE));
        
        // Else opcional
        if (this.Tok == TokenType.ELSE) {
            ifNodo.agregarHijo(eat(TokenType.ELSE));
            ifNodo.agregarHijo(eat(TokenType.LBRACE));
            
            NodoParseTree cuerpoElse = new NodoParseTree("ELSE_BODY");
            while (this.Tok != TokenType.RBRACE && this.Tok != TokenType.EOF && !this.ParserError) {
                NodoParseTree instr = INSTRUCCION();
                if (instr != null) {
                    cuerpoElse.agregarHijo(instr);
                }
            }
            ifNodo.agregarHijo(cuerpoElse);
            
            ifNodo.agregarHijo(eat(TokenType.RBRACE));
        }
        
        System.out.println("\n>>> Arbol completo del IF:");
        System.out.println(ifNodo.imprimirArbol() + "\n");
        
        return ifNodo;
    }

    private NodoParseTree FOR() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          PROCESANDO FOR            ║");
        System.out.println("╚════════════════════════════════════╝");
        
        NodoParseTree forNodo = new NodoParseTree("FOR");

        forNodo.agregarHijo(eat(TokenType.FOR));
        forNodo.agregarHijo(eat(TokenType.LPAREN));
        
        // 1. Declaración
        System.out.println("\n--- 1. INICIALIZACIÓN ---");
        forNodo.agregarHijo(DECLARACION());
        eat(TokenType.SEMICOLON);
        
        System.out.println("\n--- 2. CONDICIÓN ---");
        System.out.println("Token al iniciar condición: " + this.Tok);
        
        forNodo.agregarHijo(CALCULO());
        
        System.out.println("Token después de CALCULO: " + this.Tok);
        eat(TokenType.SEMICOLON);
        
        // 3. Incremento
        System.out.println("\n--- 3. INCREMENTO ---");
        System.out.println("Token al iniciar incremento: " + this.Tok);
        
        NodoParseTree incremento = new NodoParseTree("INCREMENTO");
        
        if (this.Tok == TokenType.IDENTIFIER) {
            if (esElementoArray(this.Tok)) {
                incremento.agregarHijo(ARRAY_ACCESS());
            } else {
                incremento.agregarHijo(eat(TokenType.IDENTIFIER));
            }
            
            if (this.Tok == TokenType.INC || this.Tok == TokenType.DEC) {
                System.out.println("• Operador de incremento/decremento: " + this.Tok);
                incremento.agregarHijo(eat(this.Tok));
            } else if (this.Tok == TokenType.ASSIGN) {
                System.out.println("• Asignación en incremento");
                incremento.agregarHijo(eat(TokenType.ASSIGN));
                incremento.agregarHijo(CALCULO());
            }
        }
        
        forNodo.agregarHijo(incremento);
        forNodo.agregarHijo(eat(TokenType.RPAREN));
        
        // 4. Cuerpo
        System.out.println("\n--- 4. CUERPO ---");
        forNodo.agregarHijo(eat(TokenType.LBRACE));
        
        NodoParseTree cuerpo = new NodoParseTree("FOR_BODY");
        while (this.Tok != TokenType.RBRACE && this.Tok != TokenType.EOF && !this.ParserError) {
            NodoParseTree instr = INSTRUCCION();
            if (instr != null) {
                cuerpo.agregarHijo(instr);
            }
        }
        forNodo.agregarHijo(cuerpo);
        
        forNodo.agregarHijo(eat(TokenType.RBRACE));
        
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║      ÁRBOL FOR COMPLETO            ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println(forNodo.imprimirArbol());
        System.out.println("════════════════════════════════════\n");
        
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

        System.out.println("\n--------- DECLARACION ---------");
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
                decl.agregarHijo(CALCULO(TokenType.RBRACKET));
            
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
            init.agregarHijo(eat(TokenType.ASSIGN));
            init.agregarHijo(CALCULO(TokenType.SEMICOLON));
            decl.agregarHijo(init);
        }

        return decl;
    }

    private NodoParseTree INIT_ARRAY()  {

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

    private NodoParseTree CALCULO(TokenType... limites)  {

        System.out.println("\n========================================");
        System.out.println("        INICIANDO CALCULO");
        System.out.println("========================================");

        Set<TokenType> limitesSet = Set.of(limites);        
        NodoParseTree nodoCalculo = new NodoParseTree("CALCULO");
        operandos.clear();
        operadores.clear();
        int nivelParentesis = 0;
        
        if (limites.length > 0) {
            System.out.println("Límites configurados: " + java.util.Arrays.toString(limites));
        }
        
        while (this.Tok != TokenType.SEMICOLON && 
            this.Tok != TokenType.EOF && 
            !limitesSet.contains(this.Tok)) {
            
            System.out.println("\n─────────────────────────────────────");
            System.out.println("Token: " + this.Tok + " | Nivel paréntesis: " + nivelParentesis);
            System.out.println("Operandos en pila: " + operandos.size() + " | Operadores en pila: " + operadores.size());
            
            // Procesar operandos
            if (esOperando(this.Tok) || this.Tok == TokenType.STRING) {
                System.out.println("→ OPERANDO detectado");
                
                if (esFuncion(this.Tok)) {
                    System.out.println("  • Es una función, procesando CALL...");
                    operandos.push(CALL());
                } 
                else if (esElementoArray(this.Tok)) {
                    System.out.println("  • Es acceso a array, procesando ARRAY_ACCESS...");
                    operandos.push(ARRAY_ACCESS());    
                } 
                else {
                    System.out.println("  • Agregando operando: " + this.Tok);
                    operandos.push(eat(this.Tok));
                }
            }
            // Procesar TODOS los operadores
            else if (this.Tok.esOperador()) {
                System.out.println("→ OPERADOR detectado: " + this.Tok + " (precedencia: " + this.Tok.getPrecedencia() + ")");
                
                while (!operadores.isEmpty() && 
                    !operadores.peek().getTipo().equals("LPAREN") &&
                    getPrecedenciaDeNodo(operadores.peek()) <= this.Tok.getPrecedencia()) {
                    
                    System.out.println("  • Precedencia del operador en pila (" + operadores.peek().getTipo() + 
                                    ": " + getPrecedenciaDeNodo(operadores.peek()) + 
                                    ") ≤ precedencia actual (" + this.Tok.getPrecedencia() + ")");
                    System.out.println("  • Construyendo subárbol antes de apilar nuevo operador...");
                    construirSubArbol();
                }
                
                System.out.println("  • Apilando operador: " + this.Tok);
                operadores.push(eat(this.Tok));
            }
            // Paréntesis izquierdo
            else if (this.Tok == TokenType.LPAREN) {
                System.out.println("→ PARÉNTESIS IZQUIERDO");
                operadores.push(eat(TokenType.LPAREN));
                nivelParentesis++;
                System.out.println("  • Nivel de paréntesis incrementado a: " + nivelParentesis);
            }
                        // Paréntesis derecho
            else if (this.Tok == TokenType.RPAREN) {
                System.out.println("→ PARÉNTESIS DERECHO (nivel actual: " + nivelParentesis + ")");
                
                if (nivelParentesis > 0) {
                    System.out.println("  • Es un paréntesis de agrupación interno");
                    System.out.println("  • Procesando operadores hasta encontrar LPAREN...");
                    
                    while (!operadores.isEmpty() && 
                        !operadores.peek().getTipo().equals("LPAREN")) {
                        System.out.println("    ├─ Construyendo subárbol con: " + operadores.peek().getTipo());
                        construirSubArbol();
                    }
                    
                    if (!operadores.isEmpty() && 
                        operadores.peek().getTipo().equals("LPAREN")) {
                        operadores.pop();
                        nivelParentesis--;
                        System.out.println("  • LPAREN eliminado. Nivel ahora: " + nivelParentesis);
                    }
                    
                    eat(TokenType.RPAREN);
                    // NO hacer break aquí - continuar procesando
                } 
                else if (limitesSet.contains(TokenType.RPAREN)) {
                    System.out.println("  • RPAREN está en los límites, deteniendo CALCULO");
                    break;
                }
                else {
                    // Nivel == 0 y RPAREN no es límite
                    // Esto es un RPAREN externo del IF/FOR/etc
                    System.out.println("  • ⚠ RPAREN externo detectado (nivel 0), saliendo de CALCULO");
                    break;
                }
            }
            
            // Coma
            else if (this.Tok == TokenType.COMMA) {
                if (limitesSet.contains(TokenType.COMMA)) {
                    System.out.println("→ COMMA es límite, deteniendo CALCULO");
                    break;
                }
                
                System.err.println("→ ⚠ ERROR: COMMA inesperada");
                this.ParserError = true;
                break;
            }
            else {
                System.out.println("→ Token no procesable: " + this.Tok);
                System.out.println("  • Finalizando CALCULO");
                break;
            }
        }

        System.out.println("\n========================================");
        System.out.println("     PROCESAMIENTO FINAL");
        System.out.println("========================================");
        System.out.println("Operadores restantes en pila: " + operadores.size());
        System.out.println("Operandos en pila: " + operandos.size());

        // Procesar operadores restantes
        if (!operadores.isEmpty()) {
            System.out.println("\nVaciando pila de operadores...");
        }
        
        while (!operadores.isEmpty()) {
            if (operadores.peek().getTipo().equals("LPAREN")) {
                this.ParserError = true;
                System.err.println("⚠ ERROR: Paréntesis sin cerrar en línea " + lineaActual);
                break;
            }
            System.out.println("  ├─ Construyendo subárbol final con: " + operadores.peek().getTipo());
            construirSubArbol();
        }

        if (!operandos.isEmpty()) {
            System.out.println("\n✓ Agregando resultado final al nodo CALCULO");
            nodoCalculo.agregarHijo(operandos.pop());
        }
        
        System.out.println("\n========================================");
        System.out.println("        ÁRBOL DE CÁLCULO GENERADO");
        System.out.println("========================================");
        System.out.println(nodoCalculo.imprimirArbol());
        System.out.println("========================================\n");

        return nodoCalculo;
    }
    // Método auxiliar para obtener precedencia de un nodo
    private int getPrecedenciaDeNodo(NodoParseTree nodo) {
        try {
            TokenType tipo = TokenType.valueOf(nodo.getTipo());
            return tipo.getPrecedencia();
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }


    // Sobrecarga para mantener compatibilidad
    private NodoParseTree CALCULO() {
        return CALCULO(TokenType.SEMICOLON);
    }
    private void construirSubArbol() {

        System.out.println("\n    ┌─────────────────────────────────┐");
        System.out.println("    │   CONSTRUYENDO SUBÁRBOL         │");
        System.out.println("    └─────────────────────────────────┘");
        
        if (operadores.isEmpty()) {
            System.err.println("    ⚠ ERROR: Falta operador en línea " + lineaActual);
            this.ParserError = true;
            return;
        }
        
        if (operandos.size() < 2) {
            System.err.println("    ⚠ ERROR: Faltan operandos para el operador en línea " + lineaActual);
            this.ParserError = true;
            return;
        }
        
        NodoParseTree operador = operadores.pop();
        NodoParseTree derecho = operandos.pop();
        NodoParseTree izquierdo = operandos.pop();
        
        System.out.println("    Operador: " + operador.getTipo());
        System.out.println("    Operando izquierdo: " + izquierdo.getTipo() + 
                        (izquierdo.getLexema() != null ? " [" + izquierdo.getLexema() + "]" : ""));
        System.out.println("    Operando derecho: " + derecho.getTipo() + 
                        (derecho.getLexema() != null ? " [" + derecho.getLexema() + "]" : ""));
        
        // El operador es la raíz, con los operandos como hijos
        operador.agregarHijo(izquierdo);
        operador.agregarHijo(derecho);
        
        System.out.println("    ✓ Subárbol creado: " + operador.getTipo() + 
                        "(" + izquierdo.getTipo() + ", " + derecho.getTipo() + ")");
        
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

        System.out.println("\n--------- CALL ---------");
        
        nodoLlamada.agregarHijo(eat(TokenType.IDENTIFIER));
        nodoLlamada.agregarHijo(eat(TokenType.LPAREN));
        
        // Procesar argumentos usando CALCULO
        if (this.Tok != TokenType.RPAREN) {
            NodoParseTree argumentos = new NodoParseTree("ARGUMENTOS");
            
            // Primer argumento
            argumentos.agregarHijo(CALCULO(TokenType.COMMA, TokenType.RPAREN));
            
            // Argumentos adicionales
            while (this.Tok == TokenType.COMMA) {
                eat(TokenType.COMMA);
                argumentos.agregarHijo(CALCULO(TokenType.COMMA));
            }
            
            nodoLlamada.agregarHijo(argumentos);
        }
        
        nodoLlamada.agregarHijo(eat(TokenType.RPAREN));

        System.out.println("\n>>> Arbol de CALL generado:");
        System.out.println(nodoLlamada.imprimirArbol() + "\n");
        
        return nodoLlamada;
    }

    private NodoParseTree ARRAY_ACCESS() {
        NodoParseTree nodoAccessoArray = new NodoParseTree("ARRAY_ACCESS");

        nodoAccessoArray.agregarHijo(eat(TokenType.IDENTIFIER));
        nodoAccessoArray.agregarHijo(eat(TokenType.LBRACKET));
        nodoAccessoArray.agregarHijo(CALCULO(TokenType.RBRACKET));        
        nodoAccessoArray.agregarHijo(eat(TokenType.RBRACKET));
        
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

    private NodoParseTree ARG()  {

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


    // Métodos auxiliares
    private boolean esOperadorComparacion(TokenType tok) {
        return tok == TokenType.EQ || tok == TokenType.NE ||
            tok == TokenType.LT || tok == TokenType.GT ||
            tok == TokenType.LE || tok == TokenType.GE;
    }

    private boolean esOperadorLogico(TokenType tok) {
        return tok == TokenType.AND || tok == TokenType.OR;
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
    
    private boolean esOperador(TokenType tok) {
    return esOperadorBinario(tok) || esOperadorComparacion(tok) || esOperadorLogico(tok);
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
            this.ParserError = true;
            
            // Mostrar el árbol sintáctico parcial
            
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