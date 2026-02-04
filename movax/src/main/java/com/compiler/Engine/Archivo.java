package com.compiler.Engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Archivo {

    File Codigo = null;
    public Archivo(){

    }

    public void WriteFile(JTextArea jtextA, File file, JLabel label){
        
        if (file != null) {
            label.setText(file.getAbsolutePath()); // Muestra la ruta en el JLabel

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                jtextA.setText(""); // Limpiar el JTextArea
                String line;
                while ((line = reader.readLine()) != null) {
                    jtextA.append(line + "\n");
                }
                jtextA.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error al leer el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        } else {
            JOptionPane.showMessageDialog(null, "No se seleccionó ningún archivo", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
                
    
    public void browseFile(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Selecciona un archivo .txt");

        // Filtro para archivos .txt
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));

        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            this.Codigo = fileChooser.getSelectedFile();
            setCodigo(Codigo);
        }
    }

    public File getCodigo() {
        return Codigo;
    }

    public void setCodigo(File codigo) {
        Codigo = codigo;
    }
    
}
