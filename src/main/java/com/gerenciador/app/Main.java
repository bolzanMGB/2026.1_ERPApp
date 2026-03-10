package com.gerenciador.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            System.setProperty("glass.win.uiScale", "1.0");

            DatabaseSetup.inicializarBanco();
            DadosRepositorio.carregarDadosDoBanco();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gerenciador/fxml/main.fxml"));
            Parent root = loader.load();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            double width = Math.min(1100, bounds.getWidth() * 0.95);
            double height = Math.min(750, bounds.getHeight() * 0.9);

            Scene scene = new Scene(root, width, height);

            if (getClass().getResource("/com/gerenciador/css/global.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/com/gerenciador/css/global.css").toExternalForm());
            }

            stage.setTitle("Gerenciador Empresa X");
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}