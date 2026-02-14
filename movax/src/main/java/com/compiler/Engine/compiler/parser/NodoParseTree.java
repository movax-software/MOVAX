package com.compiler.Engine.compiler.parser;

import java.util.*;

public class NodoParseTree {
    private String tipo;
    private String valor;
    private String lexema;
    private int linea;
    private LinkedList<NodoParseTree> hijos;
    
    // Constructor simple - solo tipo
    public NodoParseTree(String tipo) {
        this.tipo = tipo;
        this.valor = "";
        this.lexema = "";
        this.linea = 1;
        this.hijos = new LinkedList<>();
    }
    
    // Constructor completo - tipo, valor, lexema, línea
    public NodoParseTree(String tipo, String valor, String lexema, int linea) {
        this.tipo = tipo;
        this.valor = valor != null ? valor : "";
        this.lexema = lexema != null ? lexema : "";
        this.linea = linea;
        this.hijos = new LinkedList<>();
    }
    
    // Agregar un hijo al nodo

    public void agregarHijo(NodoParseTree hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
            // VISTA.dibujarNodo(this, hijo);
        }
    }
    
    // Getters
    public String getTipo() { 
        return tipo; 
    }
    
    public String getValor() { 
        return valor; 
    }
    
    public String getLexema() { 
        return lexema; 
    }
    
    public int getLinea() { 
        return linea; 
    }
    
    public List<NodoParseTree> getHijos() { 
        return hijos; 
    }
    
    public boolean esTerminal() { 
        return hijos.isEmpty(); 
    }
    
    // Setters (por si se necesitan)
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public void setValor(String valor) {
        this.valor = valor;
    }
    
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }
    
    public void setLinea(int linea) {
        this.linea = linea;
    }
    
    public String imprimirArbol() {
        return imprimirArbol("", true);
    }
    
    private String imprimirArbol(String prefijo, boolean esUltimo) {
        StringBuilder sb = new StringBuilder();
        
        // Imprimir el nodo actual
        sb.append(prefijo);
        sb.append(esUltimo ? "└── " : "├── ");
        sb.append(tipo);
        
        // Agregar lexema si existe
        if (lexema != null && !lexema.isEmpty()) {
            sb.append(" [").append(getLexema()).append("]");
        }
        // Si no hay lexema pero hay valor, mostrar valor
        else if (valor != null && !valor.isEmpty() && !valor.equals(tipo)) {
            sb.append(" (").append(getTipo()).append(")");
        }
        
        sb.append("\n");
        
        // Imprimir los hijos
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            sb.append(hijos.get(i).imprimirArbol(nuevoPrefijo, ultimoHijo));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NodoParseTree{");
        sb.append("tipo='").append(tipo).append('\'');
        if (lexema != null && !lexema.isEmpty()) {
            sb.append(", lexema='").append(lexema).append('\'');
        }
        if (valor != null && !valor.isEmpty() && !valor.equals(tipo)) {
            sb.append(", valor='").append(valor).append('\'');
        }
        sb.append(", linea=").append(linea);
        sb.append(", hijos=").append(hijos.size());
        sb.append('}');
        return sb.toString();
    }
    
    
}