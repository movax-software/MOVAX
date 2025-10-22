package com.compiler.Engine.animations;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Visualizador de autómata finito para mostrar el proceso de reconocimiento de tokens
 */
public class AutomataVisualizer {
    
    private Canvas canvas;
    private GraphicsContext gc;
    private EstadoAutomata estadoActual;
    private String lexemaAcumulado;
    private char caracterActual;
    
    // Estados del autómata
    public enum EstadoAutomata {
        INICIAL,
        LEYENDO_PALABRA,
        LEYENDO_NUMERO,
        LEYENDO_DECIMAL,
        LEYENDO_OPERADOR,
        LEYENDO_COMPARADOR,
        LEYENDO_CADENA,
        LEYENDO_INCREMENTO,
        TOKEN_ACEPTADO,
        ERROR
    }
    
    public AutomataVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.estadoActual = EstadoAutomata.INICIAL;
        this.lexemaAcumulado = "";
        this.caracterActual = ' ';
        
        // Dibujar estado inicial
        dibujar();
    }
    
    /**
     * Actualiza el estado del autómata según el carácter y lexema
     */
    public void actualizarEstado(char c, String lexema, int tipoToken) {
        this.caracterActual = c;
        this.lexemaAcumulado = lexema;
        
        System.out.println("🔍 AutomataVisualizer.actualizarEstado() llamado:");
        System.out.println("   Carácter: '" + c + "'");
        System.out.println("   Lexema: \"" + lexema + "\"");
        System.out.println("   TipoToken: " + tipoToken);
        
        // Determinar el estado según el carácter y contexto
        EstadoAutomata estadoAnterior = estadoActual;
        
        if (tipoToken >= 0) {
            estadoActual = EstadoAutomata.TOKEN_ACEPTADO;
        } else if (lexema == null || lexema.isEmpty()) {
            estadoActual = EstadoAutomata.INICIAL;
        } else if (lexema.startsWith("\"")) {
            estadoActual = EstadoAutomata.LEYENDO_CADENA;
        } else if (lexema.matches("\\d+\\.\\d*")) {
            estadoActual = EstadoAutomata.LEYENDO_DECIMAL;
        } else if (lexema.matches("\\d+")) {
            estadoActual = EstadoAutomata.LEYENDO_NUMERO;
        } else if (lexema.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            estadoActual = EstadoAutomata.LEYENDO_PALABRA;
        } else if (lexema.matches("[+]{2}|[-]{2}")) {
            estadoActual = EstadoAutomata.LEYENDO_INCREMENTO;
        } else if (lexema.matches("[<>=!]+")) {
            estadoActual = EstadoAutomata.LEYENDO_COMPARADOR;
        } else if (lexema.matches("[+\\-*/(){};<>!=]")) {
            estadoActual = EstadoAutomata.LEYENDO_OPERADOR;
        } else {
            // Si no coincide con ningún patrón, mantener el estado o ir a error
            if (lexema.trim().length() > 0) {
                estadoActual = EstadoAutomata.ERROR;
            }
        }
        
        System.out.println("   Estado anterior: " + estadoAnterior);
        System.out.println("   Estado nuevo: " + estadoActual);
        System.out.println();
        
        dibujar();
    }
    
    /**
     * Dibuja el autómata en el canvas
     */
    private void dibujar() {
        if (canvas == null || gc == null) {
            System.err.println("⚠️ Canvas o GraphicsContext es null");
            return;
        }
        
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        
        if (width <= 0 || height <= 0) {
            System.err.println("⚠️ Canvas tiene tamaño inválido: " + width + "x" + height);
            return;
        }
        
        // Limpiar canvas
        gc.setFill(Color.web("#161b22"));
        gc.fillRect(0, 0, width, height);
        
        // Título
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));  // ⬆️ Aumentado de 16 a 20
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("AUTÓMATA FINITO", width / 2, 30);  // ⬆️ Ajustado posición Y
        
        // Dibujar según el estado actual
        switch (estadoActual) {
            case INICIAL:
                dibujarEstadoInicial(width, height);
                break;
            case LEYENDO_PALABRA:
                dibujarAutomataPalabra(width, height);
                break;
            case LEYENDO_NUMERO:
                dibujarAutomataNumero(width, height);
                break;
            case LEYENDO_DECIMAL:
                dibujarAutomataDecimal(width, height);
                break;
            case LEYENDO_CADENA:
                dibujarAutomataCadena(width, height);
                break;
            case LEYENDO_INCREMENTO:
                dibujarAutomataIncremento(width, height);
                break;
            case LEYENDO_OPERADOR:
                dibujarAutomataOperador(width, height);
                break;
            case LEYENDO_COMPARADOR:
                dibujarAutomataComparador(width, height);
                break;
            case TOKEN_ACEPTADO:
                dibujarTokenAceptado(width, height);
                break;
            case ERROR:
                dibujarError(width, height);
                break;
        }
        
        // Información del lexema actual
        dibujarInfoLexema(width, height);
    }
    
    /**
     * Dibuja el estado inicial
     */
    private void dibujarEstadoInicial(double width, double height) {
        double cx = width / 2;
        double cy = height / 2 - 30;
        
        // Estado inicial (círculo doble)
        gc.setStroke(Color.LIGHTGREEN);
        gc.setLineWidth(4);  // ⬆️ Aumentado de 3 a 4
        gc.strokeOval(cx - 40, cy - 40, 80, 80);  // ⬆️ Aumentado de 30/60 a 40/80
        gc.strokeOval(cx - 45, cy - 45, 90, 90);  // ⬆️ Aumentado de 35/70 a 45/90
        
        gc.setFill(Color.web("#4CAF50"));
        gc.fillOval(cx - 40, cy - 40, 80, 80);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));  // ⬆️ Aumentado de 14 a 18
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("q0", cx, cy + 8);
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));  // ⬆️ Aumentado de 12 a 16
        gc.fillText("Estado Inicial", cx, cy + 70);
    }
    
    /**
     * Dibuja el autómata para palabras/identificadores
     */
    private void dibujarAutomataPalabra(double width, double height) {
        double startX = Math.max(100, width * 0.2);  // ⬆️ Aumentado de 80 a 100
        double y = height / 2 - 20;
        double spacing = Math.min(160, width * 0.3);  // ⬆️ Aumentado de 120 a 160
        
        // q0 -> q1 (letra)
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "a-z, A-Z, _");  // ⬆️ Ajustado offset
        dibujarEstado(startX + spacing, y, "q1", true, true);
        
        // q1 loop (letra o dígito)
        dibujarFlechaLoop(startX + spacing, y - 50, "a-z, 0-9, _");  // ⬆️ Ajustado posición
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Identificador", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para números
     */
    private void dibujarAutomataNumero(double width, double height) {
        double startX = Math.max(100, width * 0.2);  // ⬆️ Aumentado de 80 a 100
        double y = height / 2 - 20;
        double spacing = Math.min(160, width * 0.3);  // ⬆️ Aumentado de 120 a 160
        
        // q0 -> q1 (dígito)
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "0-9");
        dibujarEstado(startX + spacing, y, "q1", true, true);
        
        // q1 loop (dígito)
        dibujarFlechaLoop(startX + spacing, y - 50, "0-9");
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Número Entero", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para números decimales
     */
    private void dibujarAutomataDecimal(double width, double height) {
        double startX = Math.max(80, width * 0.1);
        double y = height / 2 - 20;
        double spacing = Math.min(130, width * 0.22);  // ⬆️ Aumentado de 100 a 130
        
        // q0 -> q1 -> q2 -> q3
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "0-9");
        
        dibujarEstado(startX + spacing, y, "q1", false, false);
        dibujarFlechaLoop(startX + spacing, y-50, "0-9");

        dibujarFlecha(startX + spacing + 35, y, startX + spacing * 2 - 35, y, ".");
        
        dibujarEstado(startX + spacing * 2, y, "q2", false, false);
        dibujarFlecha(startX + spacing * 2 + 35, y, startX + spacing * 3 - 35, y, "0-9");
        
        dibujarEstado(startX + spacing * 3, y, "q3", true, true);
        dibujarFlechaLoop(startX + spacing * 3, y-50, "0-9");

        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Número Decimal", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para cadenas
     */
    private void dibujarAutomataCadena(double width, double height) {
        double startX = Math.max(100, width * 0.2);  // ⬆️ Aumentado de 80 a 100
        double y = height / 2 - 20;
        double spacing = Math.min(160, width * 0.3);  // ⬆️ Aumentado de 120 a 160
        
        // q0 -> q1 -> q2
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "\"");
        
        dibujarEstado(startX + spacing, y, "q1", false, false);
        dibujarFlechaLoop(startX + spacing, y - 50, "cualquier");
        dibujarFlecha(startX + spacing + 35, y, startX + spacing * 2 - 35, y, "\"");
        
        dibujarEstado(startX + spacing * 2, y, "q2", true, true);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Cadena", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para incremento/decremento
     */
    private void dibujarAutomataIncremento(double width, double height) {
        double startX = Math.max(100, width * 0.2);  // ⬆️ Aumentado de 80 a 100
        double y = height / 2 - 20;
        double spacing = Math.min(160, width * 0.3);  // ⬆️ Aumentado de 120 a 160
        
        // q0 -> q1 -> q2
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "+ / -");
        
        dibujarEstado(startX + spacing, y, "q1", false, true);
        dibujarFlecha(startX + spacing + 35, y, startX + spacing * 2 - 35, y, "+ / -");
        
        dibujarEstado(startX + spacing * 2, y, "q2", true, true);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Incremento/Decremento", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para operadores
     */
    private void dibujarAutomataOperador(double width, double height) {
        double cx = width / 2;
        double cy = height / 2 - 20;
        double spacing = Math.min(140, width * 0.25);  // ⬆️ Aumentado de 100 a 140
        
        dibujarEstado(cx - spacing, cy, "q0", false, false);
        dibujarFlecha(cx - spacing + 35, cy, cx - 35, cy, "+, -, *, /, (, ), {, }, ;");
        dibujarEstado(cx, cy, "q1", true, true);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Operador", width / 2, height - 30);
    }
    
    /**
     * Dibuja el autómata para comparadores
     */
    private void dibujarAutomataComparador(double width, double height) {
        double startX = Math.max(100, width * 0.2);  // ⬆️ Aumentado de 80 a 100
        double y = height / 2 - 20;
        double spacing = Math.min(160, width * 0.3);  // ⬆️ Aumentado de 120 a 160
        
        dibujarEstado(startX, y, "q0", false, false);
        dibujarFlecha(startX + 35, y, startX + spacing - 35, y, "<, >, =, !");
        
        dibujarEstado(startX + spacing, y, "q1", false, true);
        dibujarFlecha(startX + spacing + 35, y, startX + spacing * 2 - 35, y, "=");
        
        dibujarEstado(startX + spacing * 2, y, "q2", true, true);
        
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Comparador", width / 2, height - 30);
    }
    
    /**
     * Dibuja token aceptado
     */
    private void dibujarTokenAceptado(double width, double height) {
        double cx = width / 2;
        double cy = height / 2 - 30;
        
        gc.setFill(Color.web("#4CAF50"));
        gc.fillOval(cx - 40, cy - 40, 80, 80);  // Aumentado de 80x80 a 100x100
        
        gc.setStroke(Color.LIGHTGREEN);
        gc.setLineWidth(4);  // Aumentado de 4 a 5
        gc.strokeOval(cx - 50, cy - 50, 100, 100);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));  //  Aumentado de 24 a 36
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("✓", cx, cy + 10);
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));  // Aumentado de 14 a 18
        gc.fillText("TOKEN ACEPTADO", cx, cy + 70);
    }
    
    /**
     * Dibuja estado de error
     */
    private void dibujarError(double width, double height) {
        double cx = width / 2;
        double cy = height / 2 - 30;
        
        gc.setFill(Color.web("#F44336"));
        gc.fillOval(cx - 50, cy - 50, 100, 100);  // ⬆️ Aumentado de 80x80 a 100x100
        
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);  // ⬆️ Aumentado de 3 a 4
        gc.strokeOval(cx - 50, cy - 50, 100, 100);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));  // ⬆️ Aumentado de 24 a 36
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("✗", cx, cy + 15);
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));  // ⬆️ Aumentado de 14 a 18
        gc.fillText("ERROR", cx, cy + 70);
    }
    
    /**
     * Dibuja un estado del autómata
     */
    private void dibujarEstado(double x, double y, String nombre, boolean activo, boolean aceptacion) {
        double radio = 35;  // ⬆️ Aumentado de 25 a 35
        
        // Círculo del estado
        if (activo) {
            gc.setFill(Color.web("#2196F3"));
            gc.setStroke(Color.CYAN);
        } else {
            gc.setFill(Color.web("#161b22"));
            gc.setStroke(Color.GRAY);
        }
        
        gc.setLineWidth(3);  // ⬆️ Aumentado de 2 a 3
        gc.fillOval(x - radio, y - radio, radio * 2, radio * 2);
        gc.strokeOval(x - radio, y - radio, radio * 2, radio * 2);
        
        // Círculo doble para estado de aceptación
        if (aceptacion) {
            gc.strokeOval(x - radio + 5, y - radio + 5, (radio - 5) * 2, (radio - 5) * 2);
        }
        
        // Nombre del estado
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));  // ⬆️ Aumentado de 12 a 16
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(nombre, x, y + 6);
    }
    
    /**
     * Dibuja una flecha entre estados
     */
    private void dibujarFlecha(double x1, double y1, double x2, double y2, String etiqueta) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(3);  // ⬆️ Aumentado de 2 a 3
        gc.strokeLine(x1, y1, x2, y2);
        
        // Punta de flecha
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double arrowSize = 15;  // ⬆️ Aumentado de 10 a 15
        gc.strokeLine(x2, y2, 
                     x2 - arrowSize * Math.cos(angle - Math.PI / 6),
                     y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        gc.strokeLine(x2, y2,
                     x2 - arrowSize * Math.cos(angle + Math.PI / 6),
                     y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        
        // Etiqueta
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2 - 10;
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));  // ⬆️ Aumentado de 10 a 13
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(etiqueta, midX, midY);
    }
    
    /**
     * Dibuja una flecha de loop (auto-transición)
     */
    private void dibujarFlechaLoop(double x, double y, String etiqueta) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(3);  // ⬆️ Aumentado de 2 a 3
        
        // Arco más grande
        gc.strokeArc(x - 20, y + 3, 40, 30, 0, 180, javafx.scene.shape.ArcType.OPEN);  // ⬆️ Aumentado
        
        // Etiqueta
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));  // ⬆️ Aumentado de 10 a 13
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(etiqueta, x, y - 15);
    }
    
    /**
     * Dibuja información del lexema actual
     */
    private void dibujarInfoLexema(double width, double height) {
        double boxHeight = 70;
        double boxY = height - boxHeight - 10;
        
        gc.setFill(Color.web("#161b22"));
        gc.fillRect(10, boxY, width - 20, boxHeight);
        
        gc.setStroke(Color.web("#4CAF50"));
        gc.setLineWidth(2);
        gc.strokeRect(10, boxY, width - 20, boxHeight);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.LEFT);
        
        String charDisplay = caracterActual == ' ' ? "espacio" : 
                           caracterActual == '\n' ? "\\n" :
                           caracterActual == '\t' ? "\\t" :
                           String.valueOf(caracterActual);
        
        gc.fillText("Carácter actual: '" + charDisplay + "'", 20, boxY + 20);
        gc.fillText("Lexema: \"" + lexemaAcumulado + "\"", 20, boxY + 40);
        
        gc.setFill(Color.YELLOW);
        gc.fillText("Estado: " + estadoActual, 20, boxY + 60);
    }
    
    /**
     * Resetea el visualizador
     */
    public void reset() {
        estadoActual = EstadoAutomata.INICIAL;
        lexemaAcumulado = "";
        caracterActual = ' ';
        dibujar();
    }
}