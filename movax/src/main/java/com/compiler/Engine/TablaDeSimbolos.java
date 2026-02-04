package com.compiler.Engine;

import java.util.LinkedHashMap;

public class TablaDeSimbolos {
    private LinkedHashMap<String, Simbolo> simbolos; // Mantiene el orden de inserción

    public TablaDeSimbolos() {
        simbolos = new LinkedHashMap<>();
    }

    public void InsertarSimbolo(Simbolo s) {
        simbolos.put(s.getNombre(), s);
    }

    public LinkedHashMap<String, Simbolo> getTabla() {
        return simbolos;
    }

    public void MostrarSimbolos() {
        System.out.println("\n-- Tabla de Símbolos --");
        for (Simbolo s : simbolos.values()) {
            System.out.println(s.getNombre() + " -> " + s.getTipo() + " = " + s.getValor());
        }
    }
}
