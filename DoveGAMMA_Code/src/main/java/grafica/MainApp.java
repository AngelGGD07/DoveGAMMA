package grafica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/main.fxml");
            if (fxmlLocation == null) {
                throw new IllegalStateException("No se pudo encontrar el archivo /main.fxml en src/main/resources");
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            Scene scene = new Scene(root, 1000, 700);

            // Sin esto las tablas se quedaban blancas — el CSS nunca llegaba al scene
            URL css = getClass().getResource("/smartgraph.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            try {
                Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Nota: No se encontró icon.png, usando ícono por defecto.");
            }

            primaryStage.setTitle("DoveGAMMA - Gestión de Rutas de Transporte");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fatal al iniciar la interfaz gráfica.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}