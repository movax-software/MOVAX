package com.compiler.Engine.animations;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class EscanerAnimado {
    
    // Datos del escaneo
    private String codigoFuente;
    private List<PasoEscaneo> pasos;
    private int pasoActual;
    
    // Componentes de UI
    private TextFlow textFlowCodigo;
    private TextArea textAreaInfo;
    private TextArea textAreaTokens;
    private AutomataVisualizer automataVisualizer;  // NUEVO
    
    // Animación
    private Timeline timeline;
    private boolean pausado;
    private int velocidadMs = 300; // Velocidad de la animación
    
    // Constantes de tokens (deben coincidir con tu Escaner)
    private final String[] NOMBRES_TOKENS = {
        "if", "print", "inputInt", "inputFloat", "inputString", "else", 
        "TIPO_INT", "TIPO_FLOAT", "TIPO_STRING", "ID", "FLOAT", "NUM", 
        "COMPARADOR", "ASIGNACION", "OPERADOR", "$$", "(", ")", "{", "}", 
        ";", "CADENA", "for", "++", "--", "main"
    };
    
    /**
     * Constructor
     */
    public EscanerAnimado(TextFlow textFlowCodigo, TextArea textAreaInfo, 
                         TextArea textAreaTokens, AutomataVisualizer automataVisualizer) {
        this.textFlowCodigo = textFlowCodigo;
        this.textAreaInfo = textAreaInfo;
        this.textAreaTokens = textAreaTokens;
        this.automataVisualizer = automataVisualizer;
        this.pasos = new ArrayList<>();
        this.pasoActual = 0;
        this.pausado = false;
    }
    
    /**
     * Prepara la animación analizando el código
     */
    public void prepararAnimacion(String codigo) {
        this.codigoFuente = codigo;
        this.pasos.clear();
        this.pasoActual = 0;
        
        // Analizar el código y crear los pasos
        analizarCodigo();
    }
    
    /**
     * Analiza el código y crea los pasos de la animación
     */
    private void analizarCodigo() {
        int pos = 0;
        int linea = 1;
        StringBuilder tokenActual = new StringBuilder();
        int inicioToken = 0;
        
        while (pos < codigoFuente.length()) {
            char c = codigoFuente.charAt(pos);
            
            // Agregar paso para cada carácter
            pasos.add(new PasoEscaneo(
                pos, 
                c, 
                "Leyendo: '" + c + "'", 
                tokenActual.toString(),
                linea,
                -1, // Sin token todavía
                null
            ));
            
            // Saltar espacios en blanco
            if (Character.isWhitespace(c)) {
                if (c == '\n') linea++;
                if (tokenActual.length() > 0) {
                    // Finalizar token anterior
                    TokenInfo info = identificarToken(tokenActual.toString());
                    pasos.add(new PasoEscaneo(
                        pos,
                        c,
                        "Token identificado: " + info.nombre,
                        tokenActual.toString(),
                        linea,
                        info.tipo,
                        info.nombre
                    ));
                    tokenActual.setLength(0);
                }
                pos++;
                continue;
            }
            
            // Manejo especial de operadores dobles (++, --, ==, !=, <=, >=)
            if ((c == '+' || c == '-' || c == '=' || c == '!' || c == '<' || c == '>') 
                && pos + 1 < codigoFuente.length()) {
                
                char siguiente = codigoFuente.charAt(pos + 1);
                
                // Verificar operadores dobles
                if ((c == '+' && siguiente == '+') ||   // ++
                    (c == '-' && siguiente == '-') ||   // --
                    (c == '=' && siguiente == '=') ||   // ==
                    (c == '!' && siguiente == '=') ||   // !=
                    (c == '<' && siguiente == '=') ||   // <=
                    (c == '>' && siguiente == '=')) {   // >=
                    
                    // Finalizar token anterior si existe
                    if (tokenActual.length() > 0) {
                        TokenInfo info = identificarToken(tokenActual.toString());
                        pasos.add(new PasoEscaneo(
                            pos - 1,
                            codigoFuente.charAt(pos - 1),
                            "Token identificado: " + info.nombre,
                            tokenActual.toString(),
                            linea,
                            info.tipo,
                            info.nombre
                        ));
                        tokenActual.setLength(0);
                    }
                    
                    // Procesar operador doble
                    String operadorDoble = "" + c + siguiente;
                    
                    // Paso para el primer carácter
                    pasos.add(new PasoEscaneo(
                        pos, 
                        c, 
                        "Leyendo operador: '" + c + "'", 
                        "" + c,
                        linea,
                        -1,
                        null
                    ));
                    
                    // Paso para el segundo carácter
                    pasos.add(new PasoEscaneo(
                        pos + 1, 
                        siguiente, 
                        "Leyendo operador: '" + siguiente + "'", 
                        operadorDoble,
                        linea,
                        -1,
                        null
                    ));
                    
                    // Identificar el token completo
                    TokenInfo info = identificarToken(operadorDoble);
                    pasos.add(new PasoEscaneo(
                        pos + 1,
                        siguiente,
                        "Token identificado: " + info.nombre,
                        operadorDoble,
                        linea,
                        info.tipo,
                        info.nombre
                    ));
                    
                    pos += 2;
                    continue;
                }
            }
            
            // Operadores y delimitadores de un solo carácter
            if (esDelimitador(c) && tokenActual.length() == 0) {
                TokenInfo info = identificarToken(String.valueOf(c));
                pasos.add(new PasoEscaneo(
                    pos,
                    c,
                    "Token identificado: " + info.nombre,
                    String.valueOf(c),
                    linea,
                    info.tipo,
                    info.nombre
                ));
                pos++;
                continue;
            }
            
            // Si encontramos un delimitador y hay token acumulado
            if (esDelimitador(c) && tokenActual.length() > 0) {
                TokenInfo info = identificarToken(tokenActual.toString());
                pasos.add(new PasoEscaneo(
                    pos - 1,
                    codigoFuente.charAt(pos - 1),
                    "Token identificado: " + info.nombre,
                    tokenActual.toString(),
                    linea,
                    info.tipo,
                    info.nombre
                ));
                tokenActual.setLength(0);
                continue;
            }
            
            // Manejo de cadenas
            if (c == '"') {
                if (tokenActual.length() == 0) {
                    tokenActual.append(c);
                    inicioToken = pos;
                } else if (tokenActual.charAt(0) == '"') {
                    tokenActual.append(c);
                    TokenInfo info = new TokenInfo(21, "CADENA");
                    pasos.add(new PasoEscaneo(
                        pos,
                        c,
                        "Token identificado: CADENA",
                        tokenActual.toString(),
                        linea,
                        21,
                        "CADENA"
                    ));
                    tokenActual.setLength(0);
                }
                pos++;
                continue;
            }
            
            // Si estamos dentro de una cadena
            if (tokenActual.length() > 0 && tokenActual.charAt(0) == '"') {
                tokenActual.append(c);
                pos++;
                continue;
            }
            
            // Acumular carácter
            tokenActual.append(c);
            pos++;
        }
        
        // Token final si existe
        if (tokenActual.length() > 0) {
            TokenInfo info = identificarToken(tokenActual.toString());
            pasos.add(new PasoEscaneo(
                pos - 1,
                codigoFuente.charAt(pos - 1),
                "Token identificado: " + info.nombre,
                tokenActual.toString(),
                linea,
                info.tipo,
                info.nombre
            ));
        }
    }
    
    /**
     * Identifica qué tipo de token es una cadena
     */
    private TokenInfo identificarToken(String lexema) {
        // Palabras reservadas
        if (lexema.equals("int")) return new TokenInfo(6, "TIPO_INT");
        if (lexema.equals("float")) return new TokenInfo(7, "TIPO_FLOAT");
        if (lexema.equals("String")) return new TokenInfo(8, "TIPO_STRING");
        if (lexema.equals("if")) return new TokenInfo(0, "IF");
        if (lexema.equals("else")) return new TokenInfo(5, "ELSE");
        if (lexema.equals("for")) return new TokenInfo(22, "FOR");
        if (lexema.equals("while")) return new TokenInfo(26, "WHILE");
        if (lexema.equals("print")) return new TokenInfo(1, "PRINT");
        if (lexema.equals("main")) return new TokenInfo(25, "MAIN");
        if (lexema.equals("inputInt")) return new TokenInfo(2, "INPUT_INT");
        if (lexema.equals("inputFloat")) return new TokenInfo(3, "INPUT_FLOAT");
        if (lexema.equals("inputString")) return new TokenInfo(4, "INPUT_STRING");
        
        // Operadores de incremento/decremento (PRIMERO - antes de operadores simples)
        if (lexema.equals("++")) return new TokenInfo(23, "INCREMENTO");
        if (lexema.equals("--")) return new TokenInfo(24, "DECREMENTO");
        
        // Operadores de asignación y comparación
        if (lexema.equals("=")) return new TokenInfo(13, "ASIGNACION");
        if (lexema.equals("==")) return new TokenInfo(12, "COMPARADOR");
        if (lexema.equals("!=")) return new TokenInfo(12, "COMPARADOR");
        if (lexema.equals("<")) return new TokenInfo(12, "COMPARADOR");
        if (lexema.equals(">")) return new TokenInfo(12, "COMPARADOR");
        if (lexema.equals("<=")) return new TokenInfo(12, "COMPARADOR");
        if (lexema.equals(">=")) return new TokenInfo(12, "COMPARADOR");
        
        // Operadores aritméticos
        if (lexema.equals("+") || lexema.equals("-") || lexema.equals("*") || lexema.equals("/")) 
            return new TokenInfo(14, "OPERADOR");
        
        // Delimitadores
        if (lexema.equals("(")) return new TokenInfo(16, "PARENTESIS_IZQ");
        if (lexema.equals(")")) return new TokenInfo(17, "PARENTESIS_DER");
        if (lexema.equals("{")) return new TokenInfo(18, "LLAVE_IZQ");
        if (lexema.equals("}")) return new TokenInfo(19, "LLAVE_DER");
        if (lexema.equals(";")) return new TokenInfo(20, "PUNTO_COMA");
        
        // Números
        if (lexema.matches("\\d+\\.\\d+")) return new TokenInfo(10, "FLOAT");
        if (lexema.matches("\\d+")) return new TokenInfo(11, "NUM");
        
        // Cadenas
        if (lexema.startsWith("\"") && lexema.endsWith("\"")) 
            return new TokenInfo(21, "CADENA");
        
        // Identificador
        if (lexema.matches("[a-zA-Z_][a-zA-Z0-9_]*")) 
            return new TokenInfo(9, "ID");
        
        return new TokenInfo(-1, "DESCONOCIDO");
    }
    
    /**
     * Verifica si un carácter es delimitador
     * Nota: + y - se manejan especialmente para ++ y --
     */
    private boolean esDelimitador(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || 
               c == ';' || c == '=' || c == '+' || c == '-' || 
               c == '*' || c == '/' || c == '<' || c == '>' || c == '!';
    }
    
    /**
     * Inicia la animación
     */
    public void iniciar() {
        if (pasos.isEmpty()) {
            textAreaInfo.setText("No hay pasos para animar");
            return;
        }
        
        pasoActual = 0;
        pausado = false;
        
        timeline = new Timeline(new KeyFrame(Duration.millis(velocidadMs), event -> {
            if (!pausado && pasoActual < pasos.size()) {
                mostrarPaso(pasos.get(pasoActual));
                pasoActual++;
                
                if (pasoActual >= pasos.size()) {
                    detener();
                }
            }
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Muestra un paso de la animación
     */
    private void mostrarPaso(PasoEscaneo paso) {
        
        if (automataVisualizer != null) {
            automataVisualizer.actualizarEstado(
                paso.caracter, 
                paso.lexemaAcumulado != null ? paso.lexemaAcumulado : "", 
                paso.tipoToken
            );
        }        // Actualizar TextFlow con el código resaltado
        textFlowCodigo.getChildren().clear();
        
        for (int i = 0; i < codigoFuente.length(); i++) {
            Text t = new Text(String.valueOf(codigoFuente.charAt(i)));
            
            if (i == paso.posicion) {
                // Carácter actual - resaltar en amarillo
                t.setFill(Color.YELLOW);
                t.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: black;");
            } else if (i < paso.posicion) {
                // Ya procesado - verde
                t.setFill(Color.LIGHTGREEN);
            } else {
                // Por procesar - blanco
                t.setFill(Color.WHITE);
            }
            
            textFlowCodigo.getChildren().add(t);
        }
        
        // Actualizar información
        StringBuilder info = new StringBuilder();
        info.append("═══════════════════════════════════════════\n");
        info.append("POSICIÓN: ").append(paso.posicion).append("\n");
        info.append("CARÁCTER: '").append(paso.caracter).append("'\n");
        info.append("LÍNEA: ").append(paso.linea).append("\n");
        info.append("═══════════════════════════════════════════\n\n");
        info.append("ESTADO: ").append(paso.descripcion).append("\n\n");
        
        if (paso.lexemaAcumulado != null && !paso.lexemaAcumulado.isEmpty()) {
            info.append("LEXEMA ACUMULADO: '").append(paso.lexemaAcumulado).append("'\n");
        }
        
        if (paso.tipoToken >= 0) {
            info.append("\n✓ TOKEN IDENTIFICADO\n");
            info.append("  Tipo: ").append(paso.tipoToken).append("\n");
            info.append("  Nombre: ").append(paso.nombreToken).append("\n");
            info.append("  Lexema: '").append(paso.lexemaAcumulado).append("'\n");
        }
        
        textAreaInfo.setText(info.toString());
        
        // Actualizar lista de tokens identificados
        if (paso.tipoToken >= 0) {
            textAreaTokens.appendText(String.format("%-3d | %-20s | %s\n", 
                paso.tipoToken, 
                paso.nombreToken, 
                paso.lexemaAcumulado));
        }
    }
    
    /**
     * Pausa la animación
     */
    public void pausar() {
        pausado = true;
    }
    
    /**
     * Reanuda la animación
     */
    public void reanudar() {
        pausado = false;
    }
    
    /**
     * Detiene la animación
     */
    public void detener() {
        if (timeline != null) {
            timeline.stop();
        }
    }
    
    /**
     * Avanza un paso manualmente
     */
    public void siguientePaso() {
        if (pasoActual < pasos.size()) {
            mostrarPaso(pasos.get(pasoActual));
            pasoActual++;
        }
    }
    
    /**
     * Retrocede un paso manualmente
     */
    public void pasoAnterior() {
        if (pasoActual > 0) {
            pasoActual--;
            mostrarPaso(pasos.get(pasoActual));
        }
    }
    
    /**
     * Cambia la velocidad de la animación
     */
    public void setVelocidad(int ms) {
        this.velocidadMs = ms;
        if (timeline != null) {
            detener();
            iniciar();
        }
    }
    
    // ========================================================================
    // CLASES AUXILIARES
    // ========================================================================
    
    /**
     * Representa un paso en el proceso de escaneo
     */
    private static class PasoEscaneo {
        int posicion;
        char caracter;
        String descripcion;
        String lexemaAcumulado;
        int linea;
        int tipoToken;
        String nombreToken;
        
        public PasoEscaneo(int posicion, char caracter, String descripcion, 
                          String lexemaAcumulado, int linea, int tipoToken, String nombreToken) {
            this.posicion = posicion;
            this.caracter = caracter;
            this.descripcion = descripcion;
            this.lexemaAcumulado = lexemaAcumulado;
            this.linea = linea;
            this.tipoToken = tipoToken;
            this.nombreToken = nombreToken;
        }
    }
    
    /**
     * Información de un token
     */
    private static class TokenInfo {
        int tipo;
        String nombre;
        
        public TokenInfo(int tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre;
        }
    }
}