package com.compiler.Engine.ast;
import java.util.*;

import com.compiler.Engine.compiler.parser.NodoParseTree;

public class ParseTreeToASTConverterDebug {
    
    public ProgramaNode convertir(NodoParseTree parseTree) {
        System.out.println("=== INICIANDO CONVERSIÓN ===");
        System.out.println("Tipo raíz: " + parseTree.getTipo());
        System.out.println("Hijos de raíz: " + parseTree.getHijos().size());
        
        if (!parseTree.getTipo().equals("PROGRAMA")) {
            throw new IllegalArgumentException("El nodo raíz debe ser PROGRAMA, encontrado: " + parseTree.getTipo());
        }
        
        List<SentenciaNode> sentencias = new ArrayList<>();
        
        // Imprimir estructura del parse tree
        imprimirEstructura(parseTree, 0);
        
        // Buscar ESTATUTO
        for (NodoParseTree hijo : parseTree.getHijos()) {
            System.out.println("Procesando hijo: " + hijo.getTipo());
            if (hijo.getTipo().equals("ESTATUTO")) {
                System.out.println("  → Encontré ESTATUTO, convirtiendo...");
                convertirEstatutos(hijo, sentencias);
            }
        }
        
        System.out.println("\nTotal sentencias convertidas: " + sentencias.size());
        
        return new ProgramaNode(sentencias, 1, 1);
    }
    
    private void imprimirEstructura(NodoParseTree nodo, int nivel) {
        String indent = "  ".repeat(nivel);
        String info = indent + "└─ " + nodo.getTipo();
        
        if (nodo.getLexema() != null) {
            info += " [" + nodo.getLexema() + "]";
        }
        
        System.out.println(info);
        
        // Solo imprimir primeros 2 niveles para no saturar
        if (nivel < 3) {
            for (NodoParseTree hijo : nodo.getHijos()) {
                imprimirEstructura(hijo, nivel + 1);
            }
        } else if (nivel == 3 && !nodo.getHijos().isEmpty()) {
            System.out.println(indent + "  └─ ... (" + nodo.getHijos().size() + " hijos más)");
        }
    }
    
    private void convertirEstatutos(NodoParseTree nodo, List<SentenciaNode> sentencias) {
        List<NodoParseTree> hijos = nodo.getHijos();
        System.out.println("  convertirEstatutos - Hijos: " + hijos.size());
        
        if (hijos.isEmpty()) {
            System.out.println("  ⚠️ ESTATUTO vacío");
            return;
        }
        
        NodoParseTree primerHijo = hijos.get(0);
        System.out.println("  Primer hijo: " + primerHijo.getTipo());
        
        switch (primerHijo.getTipo()) {
            case "DECLARACION":
                System.out.println("  → Convirtiendo DECLARACION");
                convertirDeclaracion(primerHijo, sentencias);
                break;
                
            case "FOR":
                System.out.println("  → Convirtiendo FOR");
                sentencias.add(convertirFor(primerHijo));
                break;
                
            case "IF":
                System.out.println("  → Convirtiendo IF");
                sentencias.add(convertirIf(primerHijo));
                break;
                
            case "WHILE":
                System.out.println("  → Convirtiendo WHILE");
                sentencias.add(convertirWhile(primerHijo));
                break;
                
            case "PRINT":
                System.out.println("  → Convirtiendo PRINT");
                sentencias.add(convertirPrint(primerHijo));
                break;
                
            case "ID":
                System.out.println("  → Posible asignación");
                SentenciaNode asignacion = convertirAsignacion(nodo);
                if (asignacion != null) {
                    sentencias.add(asignacion);
                }
                break;
                
            default:
                System.out.println("  ⚠️ Tipo no reconocido: " + primerHijo.getTipo());
        }
        
        // Buscar siguiente estatuto
        for (NodoParseTree hijo : hijos) {
            if (hijo.getTipo().equals("ESTATUTO")) {
                System.out.println("  → Estatuto anidado encontrado");
                convertirEstatutos(hijo, sentencias);
            }
        }
    }
    
    private void convertirDeclaracion(NodoParseTree nodo, List<SentenciaNode> sentencias) {
        List<NodoParseTree> hijos = nodo.getHijos();
        System.out.println("    convertirDeclaracion - Hijos: " + hijos.size());
        
        String tipo = null;
        String nombreVar = null;
        ExpresionNode inicializacion = null;
        int linea = 1;
        
        for (int i = 0; i < hijos.size(); i++) {
            NodoParseTree hijo = hijos.get(i);
            System.out.println("      Hijo[" + i + "]: " + hijo.getTipo() + 
                             (hijo.getLexema() != null ? " = " + hijo.getLexema() : ""));
            
            String tipoNodo = hijo.getTipo();
            if (tipoNodo.startsWith("TIPO_") || 
                tipoNodo.startsWith("\"TIPO_") ||
                tipoNodo.equals("\"TIPO_INT\"") ||
                tipoNodo.equals("\"TIPO_STRING\"") ||
                tipoNodo.equals("\"TIPO_FLOAT\"") ||
                tipoNodo.equals("\"TIPO_DOUBLE\"") ||
                tipoNodo.equals("\"TIPO_BOOLEAN\"") ||
                tipoNodo.contains("TIPO_")) {
                tipo = hijo.getLexema();
                linea = hijo.getLinea();
                System.out.println("      ✓ Tipo encontrado: " + tipo + " (nodo: " + tipoNodo + ")");
            }
            else if (hijo.getTipo().equals("ID") && nombreVar == null) {
                nombreVar = hijo.getLexema();
                linea = hijo.getLinea();
                System.out.println("      ✓ Variable encontrada: " + nombreVar);
            }
            else if (hijo.getTipo().equals("NUM")) {
                inicializacion = convertirLiteral(hijo);
                System.out.println("      ✓ Inicialización NUM: " + hijo.getLexema());
            }
            else if (hijo.getTipo().equals("CADENA")) {
                inicializacion = convertirLiteral(hijo);
                System.out.println("      ✓ Inicialización CADENA: " + hijo.getLexema());
            }
            else if (hijo.getTipo().equals("CALCULO")) {
                inicializacion = convertirCalculo(hijo);
                System.out.println("      ✓ Inicialización CALCULO");
            }
            else if (hijo.getTipo().equals("DECLARACION")) {
                System.out.println("      → Declaración anidada encontrada");
            }
            else if (hijo.getTipo().equals("ESTATUTO")) {
                System.out.println("      → Estatuto anidado encontrado");
            }
        }
        
        // Crear nodo de declaración
        if (tipo != null && nombreVar != null) {
            System.out.println("    ✅ Creando DeclaracionVariableNode: " + tipo + " " + nombreVar);
            List<String> identificadores = new ArrayList<>();
            identificadores.add(nombreVar);
            
            DeclaracionVariableNode declaracion = new DeclaracionVariableNode(
                tipo, identificadores, inicializacion, linea, 1
            );
            sentencias.add(declaracion);
        } else {
            System.out.println("    ⚠️ No se pudo crear declaración. Tipo: " + tipo + ", Var: " + nombreVar);
        }
        
        // Procesar siguiente declaración o estatuto
        for (NodoParseTree hijo : hijos) {
            if (hijo.getTipo().equals("DECLARACION")) {
                System.out.println("    → Procesando siguiente DECLARACION");
                convertirDeclaracion(hijo, sentencias);
            } else if (hijo.getTipo().equals("ESTATUTO")) {
                System.out.println("    → Procesando ESTATUTO dentro de DECLARACION");
                convertirEstatutos(hijo, sentencias);
            }
        }
    }
    
    private SentenciaNode convertirAsignacion(NodoParseTree nodo) {
        System.out.println("    convertirAsignacion");
        // ... resto del código igual
        return null;
    }
    
    private ForNode convertirFor(NodoParseTree nodo) {
        System.out.println("    convertirFor");
        // ... código simplificado por brevedad
        return new ForNode(null, null, null, new BloqueNode(new ArrayList<>(), 1, 1), 1, 1);
    }
    
    private IfNode convertirIf(NodoParseTree nodo) {
        System.out.println("    convertirIf");
        // ... código simplificado
        return new IfNode(new LiteralNode(true, "boolean", 1, 1), 
                         new BloqueNode(new ArrayList<>(), 1, 1), null, 1, 1);
    }
    
    private WhileNode convertirWhile(NodoParseTree nodo) {
        System.out.println("    convertirWhile");
        return new WhileNode(new LiteralNode(true, "boolean", 1, 1),
                           new BloqueNode(new ArrayList<>(), 1, 1), 1, 1);
    }
    
    private PrintNode convertirPrint(NodoParseTree nodo) {
        System.out.println("    convertirPrint");
        return new PrintNode(null, 1, 1);
    }
    
    private ExpresionNode convertirCalculo(NodoParseTree nodo) {
        System.out.println("      convertirCalculo");
        return new LiteralNode(0, "int", 1, 1);
    }
    
    private ExpresionNode convertirLiteral(NodoParseTree nodo) {
        if (nodo.getTipo().equals("NUM")) {
            String valor = nodo.getLexema();
            if (valor.contains(".")) {
                return new LiteralNode(Double.parseDouble(valor), "float", nodo.getLinea(), 1);
            } else {
                return new LiteralNode(Integer.parseInt(valor), "int", nodo.getLinea(), 1);
            }
        }
        else if (nodo.getTipo().equals("CADENA")) {
            String valor = nodo.getLexema().replace("\"", "");
            return new LiteralNode(valor, "String", nodo.getLinea(), 1);
        }
        
        return new LiteralNode(0, "int", 1, 1);
    }
}