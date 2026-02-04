package com.compiler.Engine;

public class Simbolo {
    
    private String Nombre = "";
    private String Tipo = "";
    private String Valor = "";
    private int Scope = 0;
    
    public Simbolo(String nombre, String tipo, int scope, String valor) {
        this.Nombre = nombre;
        this.Tipo = tipo;
        this.Scope = scope;
        this.Valor = valor;
    }

    public String toString(){
        return Nombre + " " + Tipo + " " + Scope + " " + Valor;
    }
    public String getNombre() {
        return Nombre;
    }
    public void setNombre(String nombre) {
        Nombre = nombre;
    }
    public String getTipo() {
        return Tipo;
    }
    public void setTipo(String tipo) {
        Tipo = tipo;
    }
    public int getScope() {
        return this.Scope;
    }
    public void setScope(int scope) {
        this.Scope = scope;
    }
    public String getValor() {
        return Valor;
    }
    public void setValor(String valor) {
        Valor = valor;
    }
}