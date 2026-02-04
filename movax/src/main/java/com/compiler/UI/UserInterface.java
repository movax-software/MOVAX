package com.compiler.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserInterface extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/compiler/UI/login.fxml"));
        VBox root = loader.load();

        Scene scene = new Scene(root, 600, 400);    
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        primaryStage.setTitle("Login FXML");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
