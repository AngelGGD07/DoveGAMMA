// MainApp.java - Aplicación principal con tu paleta de colores
package grafica;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logica.GrafoTransporte;

import java.util.List;

public class MainApp extends Application {

    // Tu paleta de colores exacta
    public static final String DARK_PURPLE = "#1a0a2e";
    public static final String MID_PURPLE = "#4a1a5e";
    public static final String TERRACOTA = "#a65d48";
    public static final String BEIGE = "#d4a574";
    public static final String LIGHT_BEIGE = "#e8c9a8";

    private PanelVisualizacion panelVisual;
    private ComboBox<String> cbOrigen, cbDestino, cbCalcInicio, cbCalcFin;
    private TextArea txtResultado;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DARK_PURPLE + ";");

        // Panel Izquierdo: Creación
        VBox panelCreacion = crearPanelCreacion();
        root.setLeft(panelCreacion);

        // Panel Centro: Visualización
        panelVisual = new PanelVisualizacion();
        root.setCenter(panelVisual);

        // Panel Abajo: Control de Rutas
        HBox panelControl = crearPanelControl();
        root.setBottom(panelControl);

        // Conectar adaptador
        AdaptadorVisual.getInstancia().setPanelVisual(panelVisual);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("DoveGAMMA - Sistema de Rutas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox crearPanelCreacion() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: " + MID_PURPLE + ";");

        Label lblTitulo = new Label("🚌 Panel de Creación");
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + LIGHT_BEIGE + ";");

        // Formulario Parada
        TitledPane tpParada = crearFormularioParada();
        TitledPane tpRuta = crearFormularioRuta();

        panel.getChildren().addAll(lblTitulo, tpParada, tpRuta);
        return panel;
    }

    private TitledPane crearFormularioParada() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: " + DARK_PURPLE + ";");

        TextField txtId = crearCampoTexto("ID único (ej: P001)");
        TextField txtNombre = crearCampoTexto("Nombre de parada");
        TextField txtX = crearCampoTexto("Coordenada X (0-800)");
        TextField txtY = crearCampoTexto("Coordenada Y (0-600)");

        Button btnAgregar = crearBoton("➕ Agregar Parada", TERRACOTA);

        btnAgregar.setOnAction(e -> {
            if (validarCampos(txtId, txtNombre, txtX, txtY)) {
                try {
                    String id = txtId.getText().trim();
                    String nombre = txtNombre.getText().trim();
                    double x = Double.parseDouble(txtX.getText());
                    double y = Double.parseDouble(txtY.getText());

                    // LLAMAMOS AL ADAPTADOR (Que traduce hacia la lógica de tu compañero)
                    boolean exito = AdaptadorVisual.getInstancia().agregarParada(id, nombre, x, y);

                    if (exito) {
                        actualizarComboBoxes(id, nombre);
                        limpiarCampos(txtId, txtNombre, txtX, txtY);
                        mostrarMensaje("✅ Parada agregada: " + nombre);
                    } else {
                        mostrarError("Backend no conectado");
                    }
                } catch (NumberFormatException ex) {
                    mostrarError("Coordenadas deben ser números");
                }
            }
        });

        content.getChildren().addAll(
                crearLabel("ID:"), txtId,
                crearLabel("Nombre:"), txtNombre,
                crearLabel("Coordenada X:"), txtX,
                crearLabel("Coordenada Y:"), txtY,
                btnAgregar
        );

        TitledPane tp = new TitledPane("📍 Nueva Parada", content);
        tp.setExpanded(true);
        tp.setStyle("-fx-text-fill: " + DARK_PURPLE + ";");
        return tp;
    }

    private TitledPane crearFormularioRuta() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: " + DARK_PURPLE + ";");

        cbOrigen = new ComboBox<>();
        cbOrigen.setPromptText("Seleccionar origen");
        estilizarComboBox(cbOrigen);

        cbDestino = new ComboBox<>();
        cbDestino.setPromptText("Seleccionar destino");
        estilizarComboBox(cbDestino);

        TextField txtTiempo = crearCampoTexto("Tiempo en minutos");
        TextField txtDistancia = crearCampoTexto("Distancia en km");

        Button btnCrear = crearBoton("🔗 Crear Ruta", TERRACOTA);

        btnCrear.setOnAction(e -> {
            if (cbOrigen.getValue() == null || cbDestino.getValue() == null) {
                mostrarError("Seleccione origen y destino");
                return;
            }
            if (cbOrigen.getValue().equals(cbDestino.getValue())) {
                mostrarError("Origen y destino deben ser diferentes");
                return;
            }
            if (txtTiempo.getText().isEmpty() || txtDistancia.getText().isEmpty()) {
                mostrarError("Complete tiempo y distancia");
                return;
            }

            try {
                String origen = cbOrigen.getValue().split(" - ")[0]; // Extraer ID
                String destino = cbDestino.getValue().split(" - ")[0];
                double tiempo = Double.parseDouble(txtTiempo.getText());
                double distancia = Double.parseDouble(txtDistancia.getText());

                GrafoTransporte backend = AdaptadorVisual.getInstancia().getBackend();
                if (backend != null) {
                    boolean exito = backend.agregarRuta(origen, destino, tiempo, distancia);
                    if (exito) {
                        AdaptadorVisual.getInstancia().notificarNuevaRuta(origen, destino, tiempo, distancia);
                        limpiarCampos(txtTiempo, txtDistancia);
                        mostrarMensaje("✅ Ruta creada");
                    }
                } else {
                    panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia);
                    limpiarCampos(txtTiempo, txtDistancia);
                }
            } catch (Exception ex) {
                mostrarError("Valores numéricos inválidos");
            }
        });

        content.getChildren().addAll(
                crearLabel("Origen:"), cbOrigen,
                crearLabel("Destino:"), cbDestino,
                crearLabel("Tiempo (min):"), txtTiempo,
                crearLabel("Distancia (km):"), txtDistancia,
                btnCrear
        );

        TitledPane tp = new TitledPane("🛣️ Nueva Ruta", content);
        tp.setExpanded(true);
        tp.setStyle("-fx-text-fill: " + DARK_PURPLE + ";");
        return tp;
    }

    private HBox crearPanelControl() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: " + MID_PURPLE + ";");

        VBox seleccion = new VBox(10);
        seleccion.setAlignment(Pos.CENTER_LEFT);

        cbCalcInicio = new ComboBox<>();
        cbCalcInicio.setPromptText("Inicio");
        estilizarComboBox(cbCalcInicio);

        cbCalcFin = new ComboBox<>();
        cbCalcFin.setPromptText("Fin");
        estilizarComboBox(cbCalcFin);

        ToggleGroup criterioGroup = new ToggleGroup();
        RadioButton rbTiempo = new RadioButton("⏱️ Menor Tiempo");
        RadioButton rbDistancia = new RadioButton("📏 Menor Distancia");
        rbTiempo.setToggleGroup(criterioGroup);
        rbDistancia.setToggleGroup(criterioGroup);
        rbTiempo.setSelected(true);
        rbTiempo.setStyle("-fx-text-fill: " + LIGHT_BEIGE + ";");
        rbDistancia.setStyle("-fx-text-fill: " + LIGHT_BEIGE + ";");

        Button btnCalcular = crearBoton("🧮 Calcular Mejor Ruta", TERRACOTA);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(5);
        txtResultado.setPrefColumnCount(50);
        txtResultado.setStyle(
                "-fx-control-inner-background: " + DARK_PURPLE + ";" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 13px;" +
                        "-fx-border-color: " + BEIGE + ";" +
                        "-fx-border-width: 2px;"
        );

        btnCalcular.setOnAction(e -> {
            if (cbCalcInicio.getValue() == null || cbCalcFin.getValue() == null) {
                mostrarError("Seleccione inicio y fin");
                return;
            }

            String inicio = cbCalcInicio.getValue().split(" - ")[0];
            String fin = cbCalcFin.getValue().split(" - ")[0];
            String criterio = rbTiempo.isSelected() ? "tiempo" : "distancia";

            GrafoTransporte backend = AdaptadorVisual.getInstancia().getBackend();
            if (backend != null) {
                // Llamar al Dijkstra del backend
                List<String> ruta = backend.calcularRutaOptima(inicio, fin, criterio);
                if (ruta == null || ruta.isEmpty()) {
                    txtResultado.setText("❌ No existe ruta entre los puntos seleccionados");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("🎯 Ruta óptima por ").append(criterio.toUpperCase()).append(":\n\n");
                    sb.append("📍 Inicio: ").append(cbCalcInicio.getValue()).append("\n");
                    for (int i = 1; i < ruta.size() - 1; i++) {
                        sb.append("   ↓ ").append(ruta.get(i)).append("\n");
                    }
                    sb.append("🏁 Fin: ").append(cbCalcFin.getValue()).append("\n\n");
                    sb.append("Total de paradas: ").append(ruta.size());
                    txtResultado.setText(sb.toString());

                    // Resaltar ruta en el visualizador
                    panelVisual.resaltarRuta(ruta);
                }
            } else {
                txtResultado.setText("⚠️ Backend no conectado. Modo visual únicamente.");
            }
        });

        seleccion.getChildren().addAll(
                crearLabel("Inicio:"), cbCalcInicio,
                crearLabel("Fin:"), cbCalcFin,
                new HBox(15, rbTiempo, rbDistancia),
                btnCalcular
        );

        panel.getChildren().addAll(seleccion, txtResultado);
        return panel;
    }

    // Métodos auxiliares de UI
    private TextField crearCampoTexto(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color: " + BEIGE + ";" +
                        "-fx-text-fill: " + DARK_PURPLE + ";" +
                        "-fx-prompt-text-fill: #666;" +
                        "-fx-padding: 8px;" +
                        "-fx-background-radius: 5px;"
        );
        return tf;
    }

    private Label crearLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-weight: bold;");
        return lbl;
    }

    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + BEIGE + ";" +
                        "-fx-text-fill: " + DARK_PURPLE + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, " + BEIGE + ", 10, 0, 0, 0);"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void estilizarComboBox(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color: " + BEIGE + ";" +
                        "-fx-mark-color: " + DARK_PURPLE + ";"
        );
    }

    private void actualizarComboBoxes(String id, String nombre) {
        String item = id + " - " + nombre;
        Platform.runLater(() -> {
            cbOrigen.getItems().add(item);
            cbDestino.getItems().add(item);
            cbCalcInicio.getItems().add(item);
            cbCalcFin.getItems().add(item);
        });
    }

    private boolean validarCampos(TextField... campos) {
        for (TextField campo : campos) {
            if (campo.getText().trim().isEmpty()) {
                mostrarError("Todos los campos son obligatorios");
                return false;
            }
        }
        return true;
    }

    private void limpiarCampos(TextField... campos) {
        for (TextField campo : campos) campo.clear();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarMensaje(String msg) {
        txtResultado.setText(msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}