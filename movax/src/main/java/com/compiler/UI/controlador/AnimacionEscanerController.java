package com.compiler.UI.controlador;

import com.compiler.Engine.animations.AutomataVisualizer;
import com.compiler.Engine.animations.EscanerAnimado;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * Controlador para la ventana de animación del escáner
 */
public class AnimacionEscanerController {
    
    @FXML private TextFlow textFlowCodigo;
    @FXML private TextArea textAreaInfo;
    @FXML private TextArea textAreaTokens;
    @FXML private Canvas canvasAutomata;  // NUEVO - agregar al FXML
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnDetener;
    @FXML private Button btnSiguiente;
    @FXML private Button btnAnterior;
    @FXML private Slider sliderVelocidad;
    @FXML private Label lblVelocidad;
    
    private EscanerAnimado escanerAnimado;
    private AutomataVisualizer automataVisualizer;
    private String codigoFuente;
    
    @FXML
    public void initialize() {
        // Crear visualizador del autómata
        automataVisualizer = new AutomataVisualizer(canvasAutomata);
        
        // Inicializar el escáner animado CON el visualizador
        escanerAnimado = new EscanerAnimado(textFlowCodigo, textAreaInfo, textAreaTokens, automataVisualizer);
        
        // Configurar estilos
        textFlowCodigo.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10px; -fx-font-family: 'Consolas', monospace; -fx-font-size: 20px;");
        textAreaInfo.setStyle("-fx-control-inner-background: #2d2d2d; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace;");
        textAreaTokens.setStyle("-fx-control-inner-background: #2d2d2d; -fx-text-fill: white; -fx-font-family: 'Consolas', monospace;");
        
        textAreaInfo.setEditable(false);
        textAreaTokens.setEditable(false);
        
        // Configurar slider de velocidad
        sliderVelocidad.setMin(100);
        sliderVelocidad.setMax(2000);
        sliderVelocidad.setValue(300);
        lblVelocidad.setText("300 ms");
        
        sliderVelocidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            int velocidad = newVal.intValue();
            lblVelocidad.setText(velocidad + " ms");
            escanerAnimado.setVelocidad(velocidad);
        });
        
        // Preparar encabezado de tokens
        textAreaTokens.setText("═══════════════════════════════════════════\n");
        textAreaTokens.appendText("ID  | NOMBRE TOKEN         | LEXEMA\n");
        textAreaTokens.appendText("═══════════════════════════════════════════\n");
    }
    /**
     * Establece el código fuente a analizar
     */
    public void setCodigoFuente(String codigo) {
        this.codigoFuente = codigo;
        escanerAnimado.prepararAnimacion(codigo);
        
        // Limpiar área de tokens
        textAreaTokens.clear();
        textAreaTokens.setText("═══════════════════════════════════════════\n");
        textAreaTokens.appendText("ID  | NOMBRE TOKEN         | LEXEMA\n");
        textAreaTokens.appendText("═══════════════════════════════════════════\n");
    }
    
    @FXML
    private void onIniciar() {
        btnIniciar.setDisable(true);
        btnPausar.setDisable(false);
        btnDetener.setDisable(false);
        escanerAnimado.iniciar();
    }
    
    @FXML
    private void onPausar() {
        if (btnPausar.getText().equals("Pausar")) {
            escanerAnimado.pausar();
            btnPausar.setText("Reanudar");
            btnSiguiente.setDisable(false);
            btnAnterior.setDisable(false);
        } else {
            escanerAnimado.reanudar();
            btnPausar.setText("Pausar");
            btnSiguiente.setDisable(true);
            btnAnterior.setDisable(true);
        }
    }
    
    @FXML
    private void onDetener() {
        escanerAnimado.detener();
        btnIniciar.setDisable(false);
        btnPausar.setDisable(true);
        btnDetener.setDisable(true);
        btnSiguiente.setDisable(false);
        btnAnterior.setDisable(false);
        btnPausar.setText("Pausar");
    }
    
    @FXML
    private void onSiguiente() {
        escanerAnimado.siguientePaso();
    }
    
    @FXML
    private void onAnterior() {
        escanerAnimado.pasoAnterior();
    }
}

// ============================================================================
// EJEMPLO DE USO EN TU VENTANA PRINCIPAL
// ============================================================================

/*
// En tu controlador principal donde tienes el código:

@FXML
private void onMostrarAnimacionEscaner() {
    try {
        // Cargar la ventana de animación
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnimacionEscaner.fxml"));
        Parent root = loader.load();
        
        // Obtener el controlador
        AnimacionEscanerController controller = loader.getController();
        
        // Pasar el código fuente
        String codigo = codeArea.getText(); // Tu área de código
        controller.setCodigoFuente(codigo);
        
        // Crear y mostrar la ventana
        Stage stage = new Stage();
        stage.setTitle("Animación del Escáner");
        stage.setScene(new Scene(root, 1000, 600));
        stage.show();
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
*/