package com.compiler.Engine.semantic;

import java.util.List;

public class ResultadoSemantico {
    private boolean exitoso;
    private List<String> errores;
    private List<String> warnings;
    private TablaSimbolos tablaSimbolos;
    
    public ResultadoSemantico(boolean exitoso, List<String> errores, 
                             List<String> warnings, TablaSimbolos tabla) {
        this.exitoso = exitoso;
        this.errores = errores;
        this.warnings = warnings;
        this.tablaSimbolos = tabla;
    }
    
    public boolean isExitoso() { return exitoso; }
    public List<String> getErrores() { return errores; }
    public List<String> getWarnings() { return warnings; }
    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║               RESULTADO DEL ANÁLISIS SEMÁNTICO                                    ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n");
        
        if (exitoso && warnings.isEmpty()) {
            sb.append("\n✅ Análisis semántico exitoso. No se encontraron errores.\n");
        } else {
            if (!errores.isEmpty()) {
                sb.append("\n❌ ERRORES ENCONTRADOS:\n");
                sb.append("─────────────────────────────────────────────────────────────────────\n");
                for (int i = 0; i < errores.size(); i++) {
                    sb.append(String.format("  %d. %s\n", i + 1, errores.get(i)));
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("\n⚠️  ADVERTENCIAS:\n");
                sb.append("─────────────────────────────────────────────────────────────────────\n");
                for (int i = 0; i < warnings.size(); i++) {
                    sb.append(String.format("  %d. %s\n", i + 1, warnings.get(i)));
                }
            }
        }
        
        sb.append(tablaSimbolos.toString());
        
        return sb.toString();
    }
}