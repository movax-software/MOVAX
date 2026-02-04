package com.compiler.UI.controlador;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void iniciarSesion() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        System.out.println("Usuario: " + usuario);
        System.out.println("Contraseña: " + password);
        // Aquí puedes validar credenciales o abrir otra ventana
        if (usuario.equals("") && password.equals("")) {
            try {
                abrirEditor(); // llamamos a una versión sin parámetros
                this.txtUsuario.getScene().getWindow().hide();
            } catch (IOException e) {
                e.printStackTrace();
            }
            } else {
                System.out.println("Credenciales incorrectas");
            }
    }

    @FXML
    private void abrirEditor() throws IOException{
        System.out.println("Abriendo editor...");
        // Lógica para abrir el editor
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/compiler/UI/Editor.fxml"));
        System.out.println(fxmlLoader.toString());
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Visual Dannys Editor");
        stage.setMaximized(true);
        stage.show();   
    }
}
