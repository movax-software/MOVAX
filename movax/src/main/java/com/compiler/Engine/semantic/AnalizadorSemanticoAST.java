package com.compiler.Engine.semantic;

import com.compiler.Engine.ast.*;
import java.util.*;

class Simbolo {
    private String nombre;
    private String tipo;
    private String categoria;
    private int linea;
    private boolean inicializado;
    
    public Simbolo(String nombre, String tipo, String categoria, int linea) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linea = linea;
        this.inicializado = false;
    }
    
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCategoria() { return categoria; }
    public int getLinea() { return linea; }
    public boolean isInicializado() { return inicializado; }
    public void setInicializado(boolean init) { this.inicializado = init; }
    
    @Override
    public String toString() {
        return String.format("%-15s | %-10s | %-12s | Línea: %-5d | Inicializado: %s",
                           nombre, tipo, categoria, linea, inicializado);
    }
}

class TablaSimbolos {
    private Map<String, Simbolo> simbolos;
    private TablaSimbolos padre;
    private String nombreScope;
    
    public TablaSimbolos(TablaSimbolos padre, String nombreScope) {
        this.simbolos = new LinkedHashMap<>();
        this.padre = padre;
        this.nombreScope = nombreScope;
    }
    
    public void insertar(Simbolo simbolo) throws Exception {
        if (simbolos.containsKey(simbolo.getNombre())) {
            throw new Exception("Variable '" + simbolo.getNombre() + 
                              "' ya declarada en este scope (línea " + simbolo.getLinea() + ")");
        }
        simbolos.put(simbolo.getNombre(), simbolo);
    }
    
    public Simbolo buscar(String nombre) {
        Simbolo sim = simbolos.get(nombre);
        if (sim != null) return sim;
        if (padre != null) return padre.buscar(nombre);
        return null;
    }
    
    public Map<String, Simbolo> getSimbolos() {
        return simbolos;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║  TABLA DE SÍMBOLOS - %-61s║\n", nombreScope));
        sb.append("╠═══════════════════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ %-15s │ %-10s │ %-12s │ %-12s │ %-20s ║\n",
                               "Nombre", "Tipo", "Categoría", "Línea", "Inicializado"));
        sb.append("╠═══════════════════════════════════════════════════════════════════════════════════╣\n");
        
        if (simbolos.isEmpty()) {
            sb.append("║  (vacía)                                                                          ║\n");
        } else {
            for (Simbolo s : simbolos.values()) {
                sb.append(String.format("║ %s   ║\n", s.toString()));
            }
        }
        
        sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}

// ============================================================================
// ANALIZADOR SEMÁNTICO
// ============================================================================

public class AnalizadorSemanticoAST implements ASTVisitor<String> {
    private TablaSimbolos tablaGlobal;
    private TablaSimbolos tablaActual;
    private List<String> errores;
    private List<String> warnings;
    private int contadorScopes;
    
    public AnalizadorSemanticoAST() {
        this.tablaGlobal = new TablaSimbolos(null, "Global");
        this.tablaActual = tablaGlobal;
        this.errores = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.contadorScopes = 0;
    }
    
    public ResultadoSemantico analizar(ProgramaNode programa) {
        try {
            programa.accept(this);
        } catch (Exception e) {
            errores.add("Error fatal: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new ResultadoSemantico(
            errores.isEmpty(),
            errores,
            warnings,
            tablaGlobal
        );
    }
    
    // ========================================================================
    // IMPLEMENTACIÓN DEL VISITOR
    // ========================================================================
    
    @Override
    public String visit(ProgramaNode node) {
        for (SentenciaNode sent : node.getSentencias()) {
            sent.accept(this);
        }
        return null;
    }
    
    @Override
    public String visit(DeclaracionVariableNode node) {
        for (String id : node.getIdentificadores()) {
            try {
                Simbolo sim = new Simbolo(id, node.getTipo(), "variable", node.getLinea());
                
                // Si hay inicialización, verificar tipos
                if (node.getInicializacion() != null) { 
                    String tipoExpr = node.getInicializacion().accept(this);
                    if (!compatibleTipos(node.getTipo(), tipoExpr)) {
                        errores.add(String.format(
                            "Línea %d: Tipo incompatible en inicialización de '%s'. " +
                            "Esperado: %s, Encontrado: %s",
                            node.getLinea(), id, node.getTipo(), tipoExpr
                        ));
                    }
                    sim.setInicializado(true);
                }
                
                tablaActual.insertar(sim);
            } catch (Exception e) {
                errores.add("Línea " + node.getLinea() + ": " + e.getMessage());
            }
        }
        return null;
    }
    
    @Override
    public String visit(DeclaracionFuncionNode node) {
        // Crear nuevo scope para la función
        contadorScopes++;
        TablaSimbolos tablaAnterior = tablaActual;
        tablaActual = new TablaSimbolos(tablaAnterior, "funcion_" + node.getNombre());
        
        // Agregar parámetros a la tabla
        for (ParametroNode param : node.getParametros()) {
            param.accept(this);
        }
        
        // Analizar el cuerpo
        node.getCuerpo().accept(this);
        
        // Restaurar scope anterior
        tablaActual = tablaAnterior;
        
        return node.getTipoRetorno();
    }
    
    @Override
    public String visit(ParametroNode node) {
        try {
            Simbolo sim = new Simbolo(
                node.getNombre(),
                node.getTipo(),
                "parametro",
                node.getLinea()
            );
            sim.setInicializado(true);
            tablaActual.insertar(sim);
        } catch (Exception e) {
            errores.add("Línea " + node.getLinea() + ": " + e.getMessage());
        }
        return node.getTipo();
    }
    
    @Override
    public String visit(BloqueNode node) {
        for (SentenciaNode sent : node.getSentencias()) {
            sent.accept(this);
        }
        return null;
    }
    
    @Override
    public String visit(AsignacionNode node) {
        // Verificar que la variable existe
        Simbolo sim = tablaActual.buscar(node.getIdentificador());
        if (sim == null) {
            errores.add(String.format(
                "Línea %d: Variable '%s' no declarada",
                node.getLinea(), node.getIdentificador()
            ));
            return "error";
        }
        
        // Verificar compatibilidad de tipos
        String tipoExpr = node.getExpresion().accept(this);
        if (!compatibleTipos(sim.getTipo(), tipoExpr)) {
            errores.add(String.format(
                "Línea %d: Tipo incompatible en asignación a '%s'. " +
                "Esperado: %s, Encontrado: %s",
                node.getLinea(), node.getIdentificador(), sim.getTipo(), tipoExpr
            ));
        }
        
        sim.setInicializado(true);
        return null;
    }
    
    @Override
    public String visit(IfNode node) {
        // Verificar que la condición sea booleana o numérica (comparación)
        String tipoCond = node.getCondicion().accept(this);
        
        // En tu lenguaje, las comparaciones devuelven int, no boolean
        // Así que aceptamos tanto int como boolean
        if (!tipoCond.equals("boolean") && !tipoCond.equals("int")) {
            warnings.add(String.format(
                "Línea %d: La condición del if tiene tipo '%s' (se esperaba boolean o int)",
                node.getLinea(), tipoCond
            ));
        }
        
        // Crear nuevo scope para el if
        contadorScopes++;
        TablaSimbolos tablaAnterior = tablaActual;
        tablaActual = new TablaSimbolos(tablaAnterior, "if_" + contadorScopes);
        
        node.getBloqueIf().accept(this);
        
        tablaActual = tablaAnterior;
        
        // Analizar else si existe
        if (node.getBloqueElse() != null) {
            contadorScopes++;
            tablaAnterior = tablaActual;
            tablaActual = new TablaSimbolos(tablaAnterior, "else_" + contadorScopes);
            
            node.getBloqueElse().accept(this);
            
            tablaActual = tablaAnterior;
        }
        
        return null;
    }
    
    @Override
    public String visit(WhileNode node) {
        // Verificar condición
        String tipoCond = node.getCondicion().accept(this);
        if (!tipoCond.equals("boolean") && !tipoCond.equals("int")) {
            warnings.add(String.format(
                "Línea %d: La condición del while tiene tipo '%s'",
                node.getLinea(), tipoCond
            ));
        }
        
        // Crear nuevo scope para el while
        contadorScopes++;
        TablaSimbolos tablaAnterior = tablaActual;
        tablaActual = new TablaSimbolos(tablaAnterior, "while_" + contadorScopes);
        
        node.getCuerpo().accept(this);
        
        tablaActual = tablaAnterior;
        
        return null;
    }
    
    @Override
    public String visit(ForNode node) {
        // Crear nuevo scope para el for
        contadorScopes++;
        TablaSimbolos tablaAnterior = tablaActual;
        tablaActual = new TablaSimbolos(tablaAnterior, "for_" + contadorScopes);
        
        // Analizar inicialización (declaración de variable)
        if (node.getInicializacion() != null) {
            node.getInicializacion().accept(this);
        }
        
        // Verificar condición
        if (node.getCondicion() != null) {
            String tipoCond = node.getCondicion().accept(this);
            if (!tipoCond.equals("boolean") && !tipoCond.equals("int")) {
                warnings.add(String.format(
                    "Línea %d: La condición del for tiene tipo '%s'",
                    node.getLinea(), tipoCond
                ));
            }
        }
        
        // Analizar incremento
        if (node.getIncremento() != null) {
            node.getIncremento().accept(this);
        }
        
        // Analizar cuerpo
        node.getCuerpo().accept(this);
        
        // Restaurar scope anterior
        tablaActual = tablaAnterior;
        
        return null;
    }
    
    @Override
    public String visit(PrintNode node) {
        if (node.getExpresion() != null) {
            node.getExpresion().accept(this);
        }
        return null;
    }
    
    @Override
    public String visit(ReturnNode node) {
        if (node.getExpresion() != null) {
            String tipoRetorno = node.getExpresion().accept(this);
            return tipoRetorno;
        }
        return "void";
    }
    
    @Override
    public String visit(ExpresionBinariaNode node) {
        String tipoIzq = node.getIzquierda().accept(this);
        String tipoDer = node.getDerecha().accept(this);
        String operador = node.getOperador();
        
        // Operadores aritméticos: +, -, *, /
        if (operador.equals("+") || operador.equals("-") || 
            operador.equals("*") || operador.equals("/")) {
            
            // Verificar que ambos operandos sean numéricos
            if (!esNumerico(tipoIzq) || !esNumerico(tipoDer)) {
                errores.add(String.format(
                    "Línea %d: Operador '%s' requiere operandos numéricos. " +
                    "Encontrado: %s %s %s",
                    node.getLinea(), operador, tipoIzq, operador, tipoDer
                ));
                return "error";
            }
            
            // Si alguno es float, el resultado es float
            if (tipoIzq.equals("float") || tipoDer.equals("float")) {
                node.setTipoResultado("float");
                return "float";
            }
            
            node.setTipoResultado("int");
            return "int";
        }
        
        // Operadores de comparación: <, >, <=, >=, ==, !=
        if (operador.equals("<") || operador.equals(">") || 
            operador.equals("<=") || operador.equals(">=") ||
            operador.equals("==") || operador.equals("!=")) {
            
            // Verificar que los tipos sean compatibles
            if (!compatibleTipos(tipoIzq, tipoDer) && !compatibleTipos(tipoDer, tipoIzq)) {
                errores.add(String.format(
                    "Línea %d: No se pueden comparar tipos incompatibles: %s y %s",
                    node.getLinea(), tipoIzq, tipoDer
                ));
            }
            
            node.setTipoResultado("int"); // En tu lenguaje, comparaciones devuelven int
            return "int";
        }
        
        // Operadores lógicos: &&, ||
        if (operador.equals("&&") || operador.equals("||")) {
            node.setTipoResultado("boolean");
            return "boolean";
        }
        
        return "int";
    }
    
    @Override
    public String visit(ExpresionUnariaNode node) {
        String tipoExpr = node.getExpresion().accept(this);
        
        if (node.getOperador().equals("-")) {
            if (!esNumerico(tipoExpr)) {
                errores.add(String.format(
                    "Línea %d: Operador unario '-' requiere operando numérico, encontrado: %s",
                    node.getLinea(), tipoExpr
                ));
            }
            node.setTipoResultado(tipoExpr);
            return tipoExpr;
        }
        
        if (node.getOperador().equals("!")) {
            node.setTipoResultado("boolean");
            return "boolean";
        }
        
        return tipoExpr;
    }
    
    @Override
    public String visit(LiteralNode node) {
        return node.getTipo();
    }
    
    @Override
    public String visit(IdentificadorNode node) {
        Simbolo sim = tablaActual.buscar(node.getNombre());
        
        if (sim == null) {
            errores.add(String.format(
                "Línea %d: Variable '%s' no declarada",
                node.getLinea(), node.getNombre()
            ));
            return "error";
        }
        
        if (!sim.isInicializado()) {
            warnings.add(String.format(
                "Línea %d: Variable '%s' puede no estar inicializada",
                node.getLinea(), node.getNombre()
            ));
        }
        
        node.setTipoResultado(sim.getTipo());
        return sim.getTipo();
    }
    
    @Override
    public String visit(LlamadaFuncionNode node) {
        // Buscar la función en la tabla de símbolos
        Simbolo simFunc = tablaActual.buscar(node.getNombreFuncion());
        
        if (simFunc == null) {
            errores.add(String.format(
                "Línea %d: Función '%s' no declarada",
                node.getLinea(), node.getNombreFuncion()
            ));
            return "error";
        }
        
        // Analizar argumentos
        for (ExpresionNode arg : node.getArgumentos()) {
            arg.accept(this);
        }
        
        return simFunc.getTipo();
    }
    
    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================
    
    private boolean compatibleTipos(String tipo1, String tipo2) {
        if (tipo1.equals(tipo2)) return true;
        
        // int puede asignarse a float
        if (tipo1.equals("float") && tipo2.equals("int")) return true;
        if (tipo1.equals("double") && (tipo2.equals("int") || tipo2.equals("float"))) return true;
        
        return false;
    }
    
    private boolean esNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("float") || tipo.equals("double");
    }
}