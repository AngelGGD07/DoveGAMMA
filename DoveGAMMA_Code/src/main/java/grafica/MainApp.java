package grafica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point for DoveGAMMA route management system.
 *
 * Responsibilities:
 * - Initialize JavaFX application
 * - Load main FXML layout and CSS styling
 * - Configure primary stage (window) properties
 * - Manage application lifecycle (start/stop)
 *
 * @author DoveGAMMA Development Team
 * @version 1.0
 */
public class MainApp extends Application {

    private static final String APPLICATION_TITLE = "DoveGAMMA - Sistema de Gestión de Rutas";
    private static final String FXML_MAIN_LAYOUT = "/main.fxml";
    private static final String ICON_APPLICATION = "/icon.png";

    private static final double WINDOW_WIDTH = 1200.0;
    private static final double WINDOW_HEIGHT = 720.0;
    private static final double WINDOW_MIN_WIDTH = 1280.0;
    private static final double WINDOW_MIN_HEIGHT = 729.0;


    public static void main(String[] commandLineArguments) {
        launch(commandLineArguments);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Scene mainScene = createMainScene();
        configureStage(primaryStage, mainScene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        closeDatabaseConnection();
    }

    private Scene createMainScene() throws IOException {
        FXMLLoader fxmlLoader = createFXMLLoader();
        return new Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private FXMLLoader createFXMLLoader() {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(FXML_MAIN_LAYOUT));
        return loader;
    }

    private void configureStage(Stage stage, Scene scene) {
        stage.setTitle(APPLICATION_TITLE);
        stage.setScene(scene);

        setApplicationIcon(stage);
        setWindowConstraints(stage);
    }

    private void setApplicationIcon(Stage stage) {
        try {
            Image applicationIcon = loadApplicationIcon();
            stage.getIcons().add(applicationIcon);
        } catch (Exception exception) {
            // Icon loading is non-critical; log and continue
            System.err.println("Warning: Could not load application icon: " + exception.getMessage());
        }
    }

    private Image loadApplicationIcon() throws Exception {
        return new Image(MainApp.class.getResourceAsStream(ICON_APPLICATION));
    }

    private void setWindowConstraints(Stage stage) {
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
    }

    private void closeDatabaseConnection() {
        try {
            AdaptadorVisual.getInstance().getDatabaseManager().cerrar();
        } catch (Exception exception) {
            System.err.println("Error closing database connection: " + exception.getMessage());
        }
    }
}