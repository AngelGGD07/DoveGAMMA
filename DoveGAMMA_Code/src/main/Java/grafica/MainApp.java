package grafica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // La barra diagonal "/" le dice que busque directamente en la raíz de la carpeta resources
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/main.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("Mi Aplicación");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}