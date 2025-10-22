package com.compiler.Engine.animations;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;

/**
 * Ventana de animación del escáner SIN usar FXML
 * Todo se crea mediante código JavaFX puro
 */
public class VentanaAnimacionEscanerSinFXML {
    
    private EscanerAnimado escanerAnimado;
    private Button btnIniciar, btnPausar, btnDetener, btnSiguiente, btnAnterior, btnAumentarFont, btnDisminuirFont;
    private Slider sliderVelocidad;
    private Label lblVelocidad;
    private int fontSize = 14;
    
    public static void mostrar(String codigoFuente, Stage ownerStage) {
        VentanaAnimacionEscanerSinFXML ventana = new VentanaAnimacionEscanerSinFXML();
        ventana.crear(codigoFuente, ownerStage);
    }
    
    private void crear(String codigoFuente, Stage ownerStage) {
        Stage stage = new Stage();
        stage.setTitle("Animación del Escáner - Análisis Léxico");
        
        // ====================================================================
        // CREAR COMPONENTES
        // ====================================================================
        
        TextFlow textFlowCodigo = new TextFlow();
        TextArea textAreaInfo = new TextArea();
        TextArea textAreaTokens = new TextArea();
        
        // Configurar estilos
        textFlowCodigo.setStyle("-fx-background-color: #161b22; -fx-padding: 10px; -fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: "+fontSize+"px;");
        textAreaInfo.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+fontSize+"px;");
        textAreaTokens.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+fontSize+"px;");
        
        textAreaInfo.setEditable(false);
        textAreaTokens.setEditable(false);
        textAreaInfo.setWrapText(true);
        
        // Crear canvas y visualizador del autómata PRIMERO
        Canvas canvasAutomata = new Canvas(400, 300);  // ⬆️ Aumentado de 400x400 a 600x500
        AutomataVisualizer automataViz = new AutomataVisualizer(canvasAutomata);
        
        // Inicializar escáner animado CON el visualizador
        escanerAnimado = new EscanerAnimado(textFlowCodigo, textAreaInfo, textAreaTokens, automataViz);
        escanerAnimado.prepararAnimacion(codigoFuente);
        
        // Preparar encabezado de tokens
        textAreaTokens.setText("═══════════════════════════════════════════\n");
        textAreaTokens.appendText("ID  | NOMBRE TOKEN         | LEXEMA\n");
        textAreaTokens.appendText("═══════════════════════════════════════════\n");
        
        // ====================================================================
        // CREAR BOTONES
        // ====================================================================
        
        btnIniciar = crearBoton("▶ Iniciar", "#4CAF50");
        btnPausar = crearBoton("⏸ Pausar", "#FF9800");
        btnDetener = crearBoton("⏹ Detener", "#F44336");
        btnAnterior = crearBoton("◀ Anterior", "#2196F3");
        btnSiguiente = crearBoton("Siguiente ▶", "#2196F3");
        btnAumentarFont = crearBoton("A+", "gray");
        btnDisminuirFont = crearBoton("A-", "gray");
        
        btnPausar.setDisable(true);
        btnDetener.setDisable(true);
        btnSiguiente.setDisable(true);
        btnAnterior.setDisable(true);
        
        // ====================================================================
        // EVENTOS DE BOTONES
        // ====================================================================
        
        btnIniciar.setOnAction(e -> {
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnDetener.setDisable(false);
            btnSiguiente.setDisable(true);
            btnAnterior.setDisable(true);
            escanerAnimado.iniciar();
        });

        btnAumentarFont.setOnAction(e -> {
            this.fontSize += 2;
            if (this.fontSize >= 24)
                this.fontSize = 24;

        textFlowCodigo.setStyle("-fx-background-color: #161b22; -fx-padding: 10px; -fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: "+this.fontSize+"px;");
        textAreaInfo.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+this.fontSize+"px;");
        textAreaTokens.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+this.fontSize+"px;");
        });

        btnDisminuirFont.setOnAction(e -> {
            this.fontSize -= 2;
            if (this.fontSize <= 14)
                this.fontSize = 14;

            textFlowCodigo.setStyle("-fx-background-color: #161b22; -fx-padding: 10px; -fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: "+this.fontSize+"px;");
            textAreaInfo.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+this.fontSize+"px;");
            textAreaTokens.setStyle("-fx-control-inner-background: #161b22; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: "+this.fontSize+"px;");
        }); 

        
        btnPausar.setOnAction(e -> {
            if (btnPausar.getText().equals("⏸ Pausar")) {
                escanerAnimado.pausar();
                btnPausar.setText("▶ Reanudar");
                btnSiguiente.setDisable(false);
                btnAnterior.setDisable(false);
            } else {
                escanerAnimado.reanudar();
                btnPausar.setText("⏸ Pausar");
                btnSiguiente.setDisable(true);
                btnAnterior.setDisable(true);
            }
        });
        
        btnDetener.setOnAction(e -> {
            escanerAnimado.detener();
            btnIniciar.setDisable(false);
            btnPausar.setDisable(true);
            btnDetener.setDisable(true);
            btnSiguiente.setDisable(false);
            btnAnterior.setDisable(false);
            btnPausar.setText("⏸ Pausar");
        });
        
        btnSiguiente.setOnAction(e -> escanerAnimado.siguientePaso());
        btnAnterior.setOnAction(e -> escanerAnimado.pasoAnterior());
        
        // ====================================================================
        // SLIDER DE VELOCIDAD
        // ====================================================================
        
        sliderVelocidad = new Slider(100, 2000, 300);
        sliderVelocidad.setPrefWidth(150);
        sliderVelocidad.setShowTickMarks(true);
        sliderVelocidad.setShowTickLabels(false);
        sliderVelocidad.setMajorTickUnit(500);
        
        lblVelocidad = new Label("300 ms");
        lblVelocidad.setStyle("-fx-text-fill: white; -fx-min-width: 60px;");
        
        sliderVelocidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            int velocidad = newVal.intValue();
            lblVelocidad.setText(velocidad + " ms");
            escanerAnimado.setVelocidad(velocidad);
        });
        
        Label lblVelocidadTitulo = new Label("Velocidad:");
        lblVelocidadTitulo.setStyle("-fx-text-fill: white;");
        
        // ====================================================================
        // LAYOUT DE CONTROLES (TOP)
        // ====================================================================
        
        VBox topContainer = new VBox(10);
        topContainer.setStyle("-fx-background-color: #0d1117; -fx-padding: 15px;");
        
        Label titulo = new Label("Animación del Escáner - Análisis Léxico");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER_LEFT);
        
        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);
        
        controles.getChildren().addAll(
            btnIniciar, btnPausar, btnDetener,
            sep1,
            btnAnterior, btnSiguiente,
            sep2,
            lblVelocidadTitulo, sliderVelocidad, lblVelocidad,
            btnAumentarFont, btnDisminuirFont

        );
        
        topContainer.getChildren().addAll(titulo, controles);
        
        // ====================================================================
        // LAYOUT DE CÓDIGO (PANEL SUPERIOR)
        // ====================================================================
        
        VBox vboxCodigo = new VBox(5);
        vboxCodigo.setStyle("-fx-background-color: #161b22;");
        
        Label lblCodigo = new Label("CÓDIGO FUENTE");
        lblCodigo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #0d1117;");
        lblCodigo.setMaxWidth(Double.MAX_VALUE);
        
        ScrollPane scrollCodigo = new ScrollPane(textFlowCodigo);
        scrollCodigo.setFitToWidth(true);
        scrollCodigo.setFitToHeight(true);
        scrollCodigo.setStyle("-fx-background-color: #161b22; -fx-background: #161b22;");
        
        vboxCodigo.getChildren().addAll(lblCodigo, scrollCodigo);
        VBox.setVgrow(scrollCodigo, Priority.ALWAYS);
        
        // ====================================================================
        // PANEL IZQUIERDO: INFORMACIÓN + TOKENS (60%)
        // ====================================================================
        
        // Información del paso actual
        VBox vboxInfo = new VBox(5);
        vboxInfo.setStyle("-fx-background-color: #161b22;");
        
        Label lblInfo = new Label("INFORMACIÓN DEL PASO ACTUAL");
        lblInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #0d1117;");
        lblInfo.setMaxWidth(Double.MAX_VALUE);
        
        vboxInfo.getChildren().addAll(lblInfo, textAreaInfo);
        VBox.setVgrow(textAreaInfo, Priority.ALWAYS);
        
        // Tokens identificados
        VBox vboxTokens = new VBox(5);
        vboxTokens.setStyle("-fx-background-color: #161b22;");
        
        Label lblTokens = new Label("TOKENS IDENTIFICADOS");
        lblTokens.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #0d1117;");
        lblTokens.setMaxWidth(Double.MAX_VALUE);
        
        vboxTokens.getChildren().addAll(lblTokens, textAreaTokens);
        VBox.setVgrow(textAreaTokens, Priority.ALWAYS);
        
        // Split entre info y tokens
        SplitPane splitInfoTokens = new SplitPane(vboxInfo, vboxTokens);
        splitInfoTokens.setOrientation(Orientation.HORIZONTAL);
        splitInfoTokens.setDividerPositions(0.5);
        
        // ====================================================================
        // PANEL DERECHO: AUTÓMATA 
        // ====================================================================
        
        VBox vboxAutomata = new VBox(5);
        vboxAutomata.setStyle("-fx-background-color: #161b22;");
        
        Label lblAutomata = new Label("AUTÓMATA FINITO");
        lblAutomata.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #0d1117;");
        lblAutomata.setMaxWidth(Double.MAX_VALUE);
        
        // SOLUCIÓN: Envolver el canvas en un StackPane con tamaño máximo definido
        StackPane canvasContainer = new StackPane(canvasAutomata);
        canvasContainer.setStyle("-fx-background-color: #161b22;");
        canvasContainer.setMaxHeight(Double.MAX_VALUE);
        
        // Ajustar canvas al tamaño del contenedor SIN causar recursión
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue() - 10;
            if (width > 0 && Math.abs(canvasAutomata.getWidth() - width) > 1) {
                canvasAutomata.setWidth(width);
                automataViz.actualizarEstado(' ', "", -1);
            }
        });
        
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue() - 10;
            if (height > 0 && Math.abs(canvasAutomata.getHeight() - height) > 1) {
                canvasAutomata.setHeight(height);
                automataViz.actualizarEstado(' ', "", -1);
            }
        });
        
        vboxAutomata.getChildren().addAll(lblAutomata, canvasContainer);
        VBox.setVgrow(canvasContainer, Priority.ALWAYS);
        
        // ====================================================================
        // SPLITPANES - 50% izquierda, 50% derecha (más espacio para autómata)
        // ====================================================================
        
        SplitPane splitInferior = new SplitPane(splitInfoTokens, vboxAutomata);
        splitInferior.setDividerPositions(0.6);  
        
        SplitPane splitPrincipal = new SplitPane(vboxCodigo, splitInferior);
        splitPrincipal.setOrientation(Orientation.VERTICAL);
        splitPrincipal.setDividerPositions(0.5);
        
        // ====================================================================
        // LEYENDA (BOTTOM)
        // ====================================================================
        
        HBox leyenda = new HBox(20);
        leyenda.setAlignment(Pos.CENTER);
        leyenda.setPadding(new Insets(10));
        leyenda.setStyle("-fx-background-color: #252525;");
        
        Label leg1 = new Label("🟢 Ya procesado");
        leg1.setStyle("-fx-text-fill: lightgreen; -fx-font-weight: bold;");
        
        Label leg2 = new Label("🟡 Carácter actual");
        leg2.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        
        Label leg3 = new Label("⚪ Por procesar");
        leg3.setStyle("-fx-text-fill: white;");
        
        leyenda.getChildren().addAll(leg1, leg2, leg3);
        
        // ====================================================================
        // LAYOUT PRINCIPAL
        // ====================================================================
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #161b22;");
        root.setTop(topContainer);
        root.setCenter(splitPrincipal);
        root.setBottom(leyenda);
        
        // ====================================================================
        // ESCENA Y STAGE
        // ====================================================================
        
        Scene scene = new Scene(root, 1200, 700);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        if (ownerStage != null) {
            stage.initOwner(ownerStage);
        }
        
        stage.show();
    }
    
    /**
     * Método auxiliar para crear botones con estilo
     */
    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-cursor: hand;",
            color
        ));
        
        // Efectos hover
        btn.setOnMouseEntered(e -> {
            btn.setStyle(String.format(
                "-fx-background-color: derive(%s, 20%%); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-cursor: hand;",
                color
            ));
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-cursor: hand;",
                color
            ));
        });
        
        return btn;
    }
}