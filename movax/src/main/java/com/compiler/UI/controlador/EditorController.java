package com.compiler.UI.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import com.compiler.Engine.ast.ParseTreeToASTConverter;
import com.compiler.Engine.ast.ProgramaNode;
import com.compiler.Engine.compiler.escaner.Escaner;
import com.compiler.Engine.compiler.parser.NodoParseTree;
import com.compiler.Engine.compiler.parser.Parser;
import com.compiler.Engine.ast.ParseTreeToASTConverterDebug;
import com.compiler.Engine.semantic.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import com.compiler.Engine.*;
import com.compiler.Engine.animations.VentanaAnimacionEscanerSinFXML;

public class EditorController {
    @FXML
    private ScrollPane scrollLexico, scrollSintactico, scrollSemantico, scrollCodigoGenerado;

    @FXML
    private TabPane tabResultados; 
    private CodeArea codeAreaLexico, codeAreaSintactico, codeAreaSemantico, codeAreaCodigoGenerado;
    private NodoParseTree nodoParse;

    @FXML
    private VBox contenedorEditor;

    @FXML
    private SplitPane splitVertical;
    
    @FXML
    private TreeView<File> treeArchivos;
    
    private int tamanoFuente = 14; // Tamaño inicial
    private int tamanoFuenteResultados = 14; // Tamaño inicial para resultados

    @FXML
    private VBox panelDerecho;

    private CodeArea codeArea;

    private File carpetaBase = new File("bollo/");

    private Escaner escaner;
        
    @FXML
    private void initialize() {
        // 1. Inicializar CodeArea principal
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStyleClass().add("code-area");
        
        // Manejar la tecla Tab para insertar espacios
        codeArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume(); // Evitar el comportamiento por defecto
                
                IndexRange selection = codeArea.getSelection();
                int selectionLength = selection.getLength();
                
                System.out.println("Tab presionado - Selección length: " + selectionLength);
                
                // Si hay texto seleccionado (más de un carácter), indentar/desindentar
                if (selectionLength > 0) {
                    System.out.println("Indentando selección");
                    if (event.isShiftDown()) {
                        // Shift+Tab: Desindentar
                        desindentarSeleccion(codeArea);
                    } else {
                        // Tab: Indentar
                        indentarSeleccion(codeArea);
                    }
                } else {
                    // No hay selección: insertar 4 espacios
                    System.out.println("Insertando 4 espacios");
                    int caretPosition = codeArea.getCaretPosition();
                    codeArea.replaceText(caretPosition, caretPosition, "    ");
                }
            }
        });
        
        // Actualizar tamaño de fuente (esto aplicará el estilo inicial)
        actualizarTamanoFuente();
        
        // Agregar al contenedor del editor
        contenedorEditor.getChildren().add(codeArea);
        VBox.setVgrow(codeArea, Priority.ALWAYS);
        
        // 2. Inicializar CodeAreas de resultados (solo lectura)
        codeAreaLexico = crearCodeAreaResultado();
        codeAreaSintactico = crearCodeAreaResultado();
        codeAreaSemantico = crearCodeAreaResultado();
        codeAreaCodigoGenerado = crearCodeAreaResultado();
        
        // Agregar a los ScrollPanes
        scrollLexico.setContent(codeAreaLexico);
        scrollSintactico.setContent(codeAreaSintactico);
        scrollSemantico.setContent(codeAreaSemantico);
        scrollCodigoGenerado.setContent(codeAreaCodigoGenerado);

        cargarExplorador(carpetaBase);
        configurarClickArchivo();
    }


    private void cargarExplorador(File carpeta) {
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.err.println("La carpeta base no existe o no es un directorio: " + carpeta.getAbsolutePath());
            return;
        }
        TreeItem<File> rootItem = crearNodo(carpeta);
        treeArchivos.setRoot(rootItem);
        treeArchivos.setShowRoot(true);
        rootItem.setExpanded(true);
        
    }

    private TreeItem<File> crearNodo(File file) {
        TreeItem<File> item = new TreeItem<>(file);
        
        if (file.equals(carpetaBase)) {
            item.setValue(new File("Proyecto"));
        } else {
            item.setValue(file);
        }

        if (file.isDirectory()) {
            item.setExpanded(false); 
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    item.getChildren().add(crearNodo(child));
                }
                item.getChildren().sort(Comparator.comparing(fItem -> fItem.getValue().getName()));
            }
        }
        return item;
    }

    private void configurarClickArchivo() {
        treeArchivos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                File f = newSel.getValue(); 
                
                if (f.isFile()) {
                    try {
                        String contenido = new String(Files.readAllBytes(f.toPath()));
                        codeArea.replaceText(contenido);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @FXML
    private void refrescarExplorador() {
        cargarExplorador(carpetaBase);
    }

    @FXML
    private void compilarCodigo() {
        System.out.println("Compilando código:\n" + codeArea.getText());
    }

    @FXML
    private void guardarCodigo() {
        TreeItem<File> seleccionado = treeArchivos.getSelectionModel().getSelectedItem();
        
        if (seleccionado != null) {
            File f = seleccionado.getValue();
            
            if (f.isFile()) {
                try {
                    Files.write(f.toPath(), codeArea.getText().getBytes());
                    System.out.println("Archivo guardado: " + f.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No se puede guardar: No se ha seleccionado un archivo válido.");
            }
        } else {
            System.out.println("No se puede guardar: No se ha seleccionado ningún archivo.");
        }
    }
    
    @FXML
    private void aumentarTexto() {
        // Aumentar tamaño del editor principal
        tamanoFuente += 2;
        if (tamanoFuente > 30) {
            tamanoFuente = 30;
        }

        // Aumentar tamaño de los resultados
        tamanoFuenteResultados += 2;
        if (tamanoFuenteResultados > 24) {
            tamanoFuenteResultados = 24;
        }
        
        actualizarTamanoFuente();
    }

    @FXML
    private void disminuirTexto() {
        // Disminuir tamaño del editor principal
        tamanoFuente -= 2;
        if (tamanoFuente < 10) {
            tamanoFuente = 10;
        }
        
        // Disminuir tamaño de los resultados
        tamanoFuenteResultados -= 2;
        if (tamanoFuenteResultados < 8) {
            tamanoFuenteResultados = 8;
        }
        
        actualizarTamanoFuente();
    }

    @FXML
    private void ejecutarPrograma() {
        System.out.println("Ejecutando programa...");
    }
    
    @FXML
    private void actualizarTamanoFuente() {
        // Actualizar CodeArea principal - INCLUIR tab-size aquí
        codeArea.setStyle(
            "-fx-font-size: " + tamanoFuente + "px;" +
            "-fx-highlight-fill: #06360095;" +
            "-fx-highlight-text-fill: #ffffff;" +
            "-rtfx-selection-background-color: #06360095;" +
            "-fx-tab-size: 4;"
        );

        // Actualizar todos los CodeAreas de resultados
        actualizarEstiloResultado(codeAreaLexico);
        actualizarEstiloResultado(codeAreaSintactico);
        actualizarEstiloResultado(codeAreaSemantico);
        actualizarEstiloResultado(codeAreaCodigoGenerado);
    }

    private void actualizarEstiloResultado(CodeArea ca) {
        if (ca == null) return;
        
        // Obtener el estilo actual para preservar el background-color
        String estiloActual = ca.getStyle();
        String backgroundColor = "#0d1117"; // Color por defecto
        
        // Extraer el color de fondo actual si existe
        if (estiloActual.contains("-fx-background-color:")) {
            int startIndex = estiloActual.indexOf("-fx-background-color:") + 21;
            int endIndex = estiloActual.indexOf(";", startIndex);
            if (endIndex > startIndex) {
                backgroundColor = estiloActual.substring(startIndex, endIndex).trim();
            }
        }
        
        // Aplicar nuevo estilo con el tamaño de fuente actualizado
        ca.setStyle(
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: " + tamanoFuenteResultados + "px;" +
            "-fx-background-color: " + backgroundColor + ";"
        );
    }

    private CodeArea crearCodeAreaResultado() {
        CodeArea ca = new CodeArea();
        ca.setEditable(false);
        ca.getStyleClass().add("code-area-resultado");
        ca.setStyle(
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: " + tamanoFuenteResultados + "px;" +
            "-fx-background-color: #0d1117;"
        );
        return ca;
    }

    @FXML
    private void escanearCodigo() {
        this.escaner = new Escaner(codeArea.getText());

        if (codeArea.getText().equals("")) {
            codeAreaLexico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #0d1117;"
            );
            codeAreaLexico.replaceText("");
            return;
        }
        
        codeAreaLexico.setStyle(
            "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
            "-fx-font-size: " + tamanoFuenteResultados + "px;" +
            "-fx-background-color: #00390b6e;"
        );
        
        escaner.Scan();
        escaner.writeScan();
        
        if (escaner.gethasError()) {
            codeAreaLexico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #3b1515ff;"
            );
        }
        tabResultados.getSelectionModel().select(0);
        // Posicionar el scroll al principio de codeAreaLexico
        codeAreaLexico.moveTo(0);
        codeAreaLexico.requestFollowCaret();
    }
    @FXML 
    private void onAnimarEscaner() {
        String codigo = codeArea.getText();
        
        if (codigo == null || codigo.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Código vacío");
            alert.setHeaderText("No hay código para analizar");
            alert.setContentText("Escribe código antes de animar.");
            alert.showAndWait();
            return;
        }
        
        Stage currentStage = (Stage) codeArea.getScene().getWindow();
        VentanaAnimacionEscanerSinFXML.mostrar(codigo, currentStage);
    }

    @FXML
    private void parsearCodigo() {
        try {            
            tabResultados.getSelectionModel().select(1);
            Parser parser = new Parser(escaner, this.codeAreaSintactico);
            boolean exitoParser = parser.P();
            
            if (codeArea.getText().equals("")) {
                this.codeAreaSintactico.replaceText("");
                this.codeAreaSintactico.setStyle(
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                    "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                    "-fx-background-color: #0d1117;"
                );
                return;
            }
            
            // Validar que se haya ejecutado el escáner primero
            if (escaner == null) {
                this.codeAreaSintactico.replaceText(
                    "⚠ ERROR\n" +
                    "═══════════════════════════════════\n\n" +
                    "Debe ejecutar primero el análisis léxico.\n" +
                    "Presione el botón 'Escáner' antes de parsear."
                );
                this.codeAreaSintactico.setStyle(
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                    "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                    "-fx-background-color: #3b1515ff;"
                );
                return;
            }
            StringBuilder resultado = new StringBuilder();
            
            if (exitoParser && !parser.isParserError()) {
                NodoParseTree arbol = parser.getArbolSintactico();


                this.codeAreaSintactico.setStyle(
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                    "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                    "-fx-background-color: #00390b6e;"
                );
                this.setArbolSintactico(arbol);

                resultado.append("✓ ANÁLISIS SINTÁCTICO EXITOSO\n");
                resultado.append("═══════════════════════════════════\n\n");
                resultado.append("ÁRBOL SINTÁCTICO:\n\n");
                resultado.append(arbol.imprimirArbol());
                resultado.append("\n═══════════════════════════════════\n");
                resultado.append("Total de tokens procesados: ").append(escaner.getTokens().size());
                
            } else {
                this.codeAreaSintactico.setStyle(
                    "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                    "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                    "-fx-background-color: #3b1515ff;"
                );
                
                resultado.append("✗ ERROR SINTÁCTICO\n");
                resultado.append("═══════════════════════════════════\n\n");
                resultado.append("Se encontraron errores durante el análisis sintáctico.\n\n");
                
                NodoParseTree arbol = parser.getArbolSintactico();
                if (arbol != null) {
                    resultado.append("ÁRBOL PARCIAL (hasta el punto del error):\n\n");
                    resultado.append(arbol.imprimirArbol());
                    resultado.append("\n");
                }
                
                resultado.append("═══════════════════════════════════\n");
                resultado.append("Revise la consola para más detalles del error.");
            }
            codeAreaSintactico.replaceText(resultado.toString());

            
        } catch (IndexOutOfBoundsException e) {
            this.codeAreaSintactico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #3b1515ff;"
            );
            
            codeAreaSintactico.replaceText(
                "✗ ERROR\n" +
                "═══════════════════════════════════\n\n" +
                "Error de índice durante el análisis.\n" +
                "Verifica que el código termine con $$\n\n" +
                "Detalles: " + e.getMessage()
            );
            e.printStackTrace();
            
        } catch (Exception e) {
            this.codeAreaSintactico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #3b1515ff;"
            );
            
            codeAreaSintactico.replaceText(
                "✗ ERROR INESPERADO\n" +
                "═══════════════════════════════════\n\n" +
                "Ocurrió un error inesperado:\n" +
                e.getMessage() + "\n\n" +
                "Revise la consola para más detalles."
            );
            e.printStackTrace();
        }
    }
    
    @FXML
    private void analizarSemantico() {
        codeAreaSemantico.replaceText("Análisis semántico:\n...");

        ParseTreeToASTConverter converter = new ParseTreeToASTConverter();
        NodoParseTree parseTree = this.getArbolSintactico();

        AnalizadorSemanticoAST analizadorSem = new AnalizadorSemanticoAST();
        ProgramaNode ast = converter.convertir(parseTree);

        ResultadoSemantico resultado = analizadorSem.analizar(ast);


        this.tabResultados.getSelectionModel().select(2);
        if (!resultado.isExitoso()) {
            codeAreaSemantico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #3b1515ff;"
            );
        } else {
            this.codeAreaSemantico.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace;" +
                "-fx-font-size: " + tamanoFuenteResultados + "px;" +
                "-fx-background-color: #00390b6e;"
            );
        }   

        this.codeAreaSemantico.replaceText(resultado.toString() + "\n\n\n" + ast.toTreeString(1));

    }

    @FXML
    private void generarEnsamblador() {
        codeAreaCodigoGenerado.replaceText("Código ensamblador:\n...");
    }

    @FXML
    private void generarObjeto() {
        codeAreaCodigoGenerado.appendText("\n\nCódigo objeto:\n...");
    }

    //UTILERIA
    // Método para indentar selección
    private void indentarSeleccion(CodeArea codeArea) {
        IndexRange selection = codeArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();
        
        // Encontrar el inicio de la primera línea
        int lineStart = start;
        while (lineStart > 0 && codeArea.getText(lineStart - 1, lineStart).charAt(0) != '\n') {
            lineStart--;
        }
        
        // Encontrar el final de la última línea
        int lineEnd = end;
        String fullText = codeArea.getText();
        while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        
        // Obtener todas las líneas
        String[] lines = codeArea.getText(lineStart, lineEnd).split("\n", -1);
        
        // Indentar cada línea
        StringBuilder indented = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            indented.append("    ").append(lines[i]);
            if (i < lines.length - 1) {
                indented.append("\n");
            }
        }
        
        // Reemplazar el texto
        codeArea.replaceText(lineStart, lineEnd, indented.toString());
        
        // Restaurar la selección ajustada
        codeArea.selectRange(lineStart, lineStart + indented.length());
    }

    // Método para desindentar selección
    private void desindentarSeleccion(CodeArea codeArea) {
        IndexRange selection = codeArea.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();
        
        // Encontrar el inicio de la primera línea
        int lineStart = start;
        while (lineStart > 0 && codeArea.getText(lineStart - 1, lineStart).charAt(0) != '\n') {
            lineStart--;
        }
        
        // Encontrar el final de la última línea
        int lineEnd = end;
        String fullText = codeArea.getText();
        while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
            lineEnd++;
        }
        
        // Obtener todas las líneas
        String[] lines = codeArea.getText(lineStart, lineEnd).split("\n", -1);
        
        // Desindentar cada línea (remover hasta 4 espacios al inicio)
        StringBuilder deindented = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Remover hasta 4 espacios o tabs del inicio
            if (line.startsWith("    ")) {
                deindented.append(line.substring(4));
            } else if (line.startsWith("\t")) {
                deindented.append(line.substring(1));
            } else {
                // Remover espacios individuales (hasta 4)
                int spacesToRemove = 0;
                while (spacesToRemove < 4 && spacesToRemove < line.length() && line.charAt(spacesToRemove) == ' ') {
                    spacesToRemove++;
                }
                deindented.append(line.substring(spacesToRemove));
            }
            
            if (i < lines.length - 1) {
                deindented.append("\n");
            }
        }
        
        // Reemplazar el texto
        codeArea.replaceText(lineStart, lineEnd, deindented.toString());
        
        // Restaurar la selección ajustada
        codeArea.selectRange(lineStart, lineStart + deindented.length());
    }
    
    public NodoParseTree getArbolSintactico() {
        return this.nodoParse;
    }


    public void setArbolSintactico(NodoParseTree nodoParseTree) {
        this.nodoParse = nodoParseTree;
    }


}