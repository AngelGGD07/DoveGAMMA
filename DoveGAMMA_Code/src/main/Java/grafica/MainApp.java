package grafica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 720);

        // Ícono de la app — el archivo va en src/main/resources/icon.png
        Image icono = new Image(MainApp.class.getResourceAsStream("/icon.png"));
        stage.getIcons().add(icono);

        stage.setTitle("DoveGAMMA - Sistema de Gestión de Rutas");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}