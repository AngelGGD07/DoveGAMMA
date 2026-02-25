// MainApp.java
package grafica;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import logica.GrafoTransporte;

public class MainApp extends Application {

    // Paleta de colores
    public static final String DARK_PURPLE  = "#1a0a2e";
    public static final String MID_PURPLE   = "#4a1a5e";
    public static final String TERRACOTA    = "#a65d48";
    public static final String BEIGE        = "#d4a574";
    public static final String LIGHT_BEIGE  = "#e8c9a8";
    public static final String ERROR_RED    = "#c0392b";
    public static final String OK_GREEN     = "#27ae60";

    private PanelVisualizacion panelVisual;

    // Solo DOS combos — se comparten en toda la app
    private ComboBox<String> cbOrigen  = new ComboBox<>();
    private ComboBox<String> cbDestino = new ComboBox<>();

    // Para mostrar el resultado del cálculo
    private TextArea txtResultado;

    // Status bar abajo del todo
    private Label lblStatus;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DARK_PURPLE + ";");

        root.setTop(crearHeader());
        root.setLeft(crearPanelIzquierdo());
        root.setCenter(crearCentro());
        root.setBottom(crearStatusBar());

        AdaptadorVisual.getInstancia().setPanelVisual(panelVisual);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("DoveGAMMA — Sistema de Gestión de Rutas");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ─── HEADER ───────────────────────────────────────────────────────────────
    private HBox crearHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 24, 14, 24));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #0d0520, " + MID_PURPLE + ");" +
                        "-fx-border-color: " + TERRACOTA + ";" +
                        "-fx-border-width: 0 0 2 0;"
        );

        Label emoji = new Label("🕊");
        emoji.setStyle("-fx-font-size: 26px;");

        VBox textos = new VBox(2);
        Label titulo = new Label("DoveGAMMA");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + BEIGE + ";");
        Label subtitulo = new Label("Sistema de Gestión de Rutas de Transporte");
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #9a7a5a;");
        textos.getChildren().addAll(titulo, subtitulo);

        // Spacer para empujar botones a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnLimpiar = crearBotonChico("↺  Limpiar grafo");
        btnLimpiar.setOnAction(e -> {
            panelVisual.limpiarTodo();
            cbOrigen.getItems().clear();
            cbDestino.getItems().clear();
            setStatus("Grafo limpiado.", false);
        });

        header.getChildren().addAll(emoji, textos, spacer, btnLimpiar);
        return header;
    }

    // ─── PANEL IZQUIERDO ──────────────────────────────────────────────────────
    private ScrollPane crearPanelIzquierdo() {
        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(18));
        contenido.setPrefWidth(340);
        contenido.setStyle("-fx-background-color: " + MID_PURPLE + ";");

        Label lblTitulo = new Label("Panel de Control");
        lblTitulo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; " +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";"
        );

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + TERRACOTA + ";");

        contenido.getChildren().addAll(
                lblTitulo, sep,
                crearSeccionParada(),
                crearSeparadorSeccion(),
                crearSeccionRuta(),
                crearSeparadorSeccion(),
                crearSeccionCalcular()
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background-color: " + MID_PURPLE + ";" +
                        "-fx-background: " + MID_PURPLE + ";"
        );
        return scroll;
    }

    private VBox crearSeccionParada() {
        VBox seccion = new VBox(8);

        Label lblSec = crearLabelSeccion("📍 Nueva Parada");

        TextField txtId     = crearCampo("ID único  (ej: P001)");
        TextField txtNombre = crearCampo("Nombre de la parada");
        TextField txtX      = crearCampo("Coordenada X  (0 – 860)");
        TextField txtY      = crearCampo("Coordenada Y  (0 – 600)");

        // label de error inline — empieza invisible
        Label lblError = crearLabelError();

        Button btnAgregar = crearBoton("➕  Agregar Parada");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);

        btnAgregar.setOnAction(e -> {
            lblError.setText("");
            if (!validarNoVacios(txtId, txtNombre, txtX, txtY)) {
                lblError.setText("⚠  Todos los campos son obligatorios.");
                return;
            }
            try {
                String id     = txtId.getText().trim();
                String nombre = txtNombre.getText().trim();
                double x      = Double.parseDouble(txtX.getText().trim());
                double y      = Double.parseDouble(txtY.getText().trim());

                boolean ok = AdaptadorVisual.getInstancia().agregarParada(id, nombre, x, y);
                if (ok) {
                    String item = id + " - " + nombre;
                    cbOrigen.getItems().add(item);
                    cbDestino.getItems().add(item);
                    limpiar(txtId, txtNombre, txtX, txtY);
                    setStatus("✔  Parada agregada: " + nombre, false);
                } else {
                    lblError.setText("⚠  Backend no conectado.");
                }
            } catch (NumberFormatException ex) {
                lblError.setText("⚠  X e Y deben ser números.");
            }
        });

        seccion.getChildren().addAll(
                lblSec,
                crearLabel("ID:"), txtId,
                crearLabel("Nombre:"), txtNombre,
                crearLabel("Pos. X:"), txtX,
                crearLabel("Pos. Y:"), txtY,
                lblError, btnAgregar
        );
        return seccion;
    }

    private VBox crearSeccionRuta() {
        VBox seccion = new VBox(8);

        Label lblSec = crearLabelSeccion("🛣  Nueva Ruta");

        // Usamos los MISMOS combos que se comparten con Calcular
        cbOrigen.setPromptText("Origen");
        cbDestino.setPromptText("Destino");
        estilizarCombo(cbOrigen);
        estilizarCombo(cbDestino);
        cbOrigen.setMaxWidth(Double.MAX_VALUE);
        cbDestino.setMaxWidth(Double.MAX_VALUE);

        TextField txtTiempo    = crearCampo("Tiempo (minutos)");
        TextField txtDistancia = crearCampo("Distancia (km)");
        TextField txtCosto     = crearCampo("Costo (RD$)");

        Label lblError = crearLabelError();

        Button btnCrear = crearBoton("🔗  Crear Ruta");
        btnCrear.setMaxWidth(Double.MAX_VALUE);

        btnCrear.setOnAction(e -> {
            lblError.setText("");

            if (cbOrigen.getValue() == null || cbDestino.getValue() == null) {
                lblError.setText("⚠  Selecciona origen y destino.");
                return;
            }
            if (cbOrigen.getValue().equals(cbDestino.getValue())) {
                lblError.setText("⚠  Origen y destino distintos.");
                return;
            }
            if (!validarNoVacios(txtTiempo, txtDistancia, txtCosto)) {
                lblError.setText("⚠  Completa tiempo, distancia y costo.");
                return;
            }

            try {
                String origen   = cbOrigen.getValue().split(" - ")[0];
                String destino  = cbDestino.getValue().split(" - ")[0];
                double tiempo   = Double.parseDouble(txtTiempo.getText().trim());
                double distancia = Double.parseDouble(txtDistancia.getText().trim());
                double costo    = Double.parseDouble(txtCosto.getText().trim());

                AdaptadorVisual.getInstancia().agregarRuta(origen, destino, tiempo, distancia, costo);
                limpiar(txtTiempo, txtDistancia, txtCosto);
                setStatus("✔  Ruta creada: " + origen + " → " + destino, false);

            } catch (NumberFormatException ex) {
                lblError.setText("⚠  Tiempo, distancia y costo deben ser números.");
            }
        });

        seccion.getChildren().addAll(
                lblSec,
                crearLabel("Origen:"), cbOrigen,
                crearLabel("Destino:"), cbDestino,
                crearLabel("Tiempo (min):"), txtTiempo,
                crearLabel("Distancia (km):"), txtDistancia,
                crearLabel("Costo (RD$):"), txtCosto,
                lblError, btnCrear
        );
        return seccion;
    }

    private VBox crearSeccionCalcular() {
        VBox seccion = new VBox(8);

        Label lblSec = crearLabelSeccion("🧮 Calcular Ruta");

        ComboBox<String> cbInicio = new ComboBox<>();
        ComboBox<String> cbFin    = new ComboBox<>();
        cbInicio.setPromptText("Parada inicio");
        cbFin.setPromptText("Parada fin");
        estilizarCombo(cbInicio);
        estilizarCombo(cbFin);
        cbInicio.setMaxWidth(Double.MAX_VALUE);
        cbFin.setMaxWidth(Double.MAX_VALUE);

        cbOrigen.getItems().addListener(
                (javafx.collections.ListChangeListener<String>) change -> {
                    cbInicio.setItems(cbOrigen.getItems());
                    cbFin.setItems(cbOrigen.getItems());
                }
        );

        // CheckBoxes — todos activados por defecto, combinables
        CheckBox ckTiempo     = crearCheckBox("⏱  Menor tiempo");
        CheckBox ckDistancia  = crearCheckBox("📏  Menor distancia");
        CheckBox ckCosto      = crearCheckBox("💰  Menor costo");
        CheckBox ckTransbordo = crearCheckBox("🔄  Menos transbordos");

        VBox grupoChecks = new VBox(10, ckTiempo, ckDistancia, ckCosto, ckTransbordo);
        grupoChecks.setPadding(new Insets(12));
        grupoChecks.setStyle(
                "-fx-background-color: #12082a;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: #3a2050;" +
                        "-fx-border-radius: 6px;"
        );

        Label lblError = crearLabelError();

        Button btnCalc = crearBotonDestacado("CALCULAR  ▶");
        btnCalc.setMaxWidth(Double.MAX_VALUE);

        btnCalc.setOnAction(e -> {
            lblError.setText("");

            if (cbInicio.getValue() == null || cbFin.getValue() == null) {
                lblError.setText("⚠  Selecciona inicio y fin.");
                return;
            }
            if (cbInicio.getValue().equals(cbFin.getValue())) {
                lblError.setText("⚠  Inicio y fin distintos.");
                return;
            }
            if (!ckTiempo.isSelected() && !ckDistancia.isSelected()
                    && !ckCosto.isSelected() && !ckTransbordo.isSelected()) {
                lblError.setText("⚠  Marca al menos un criterio.");
                return;
            }

            String idInicio = cbInicio.getValue().split(" - ")[0];
            String idFin    = cbFin.getValue().split(" - ")[0];

            // Armar lista de criterios activos
            StringBuilder criterios = new StringBuilder();
            if (ckTiempo.isSelected())     criterios.append("tiempo, ");
            if (ckDistancia.isSelected())  criterios.append("distancia, ");
            if (ckCosto.isSelected())      criterios.append("costo, ");
            if (ckTransbordo.isSelected()) criterios.append("transbordos, ");
            String listaCriterios = criterios.toString().replaceAll(", $", "");

            GrafoTransporte backend = AdaptadorVisual.getInstancia().getBackend();
            if (backend == null) {
                txtResultado.setText("⚠  Backend no conectado.\nModo visual únicamente.");
                return;
            }

            // Cuando la lógica esté lista, descomenta lo que aplique:
            // if (ckTiempo.isSelected()) {
            //     List<String> ruta = backend.dijkstra(idInicio, idFin, "tiempo");
            //     panelVisual.resaltarRuta(ruta);
            //     txtResultado.setText("Ruta (tiempo):\n" + ruta.toString());
            // }
            // if (ckDistancia.isSelected()) { ... }
            // if (ckCosto.isSelected())     { ... }
            // if (ckTransbordo.isSelected()){ ... }

            setStatus("Criterios: " + listaCriterios + " | " + idInicio + " → " + idFin, false);
        });

        seccion.getChildren().addAll(
                lblSec,
                crearLabel("Inicio:"), cbInicio,
                crearLabel("Fin:"), cbFin,
                crearLabel("Optimizar por (puedes marcar varios):"),
                grupoChecks,
                lblError, btnCalc
        );
        return seccion;
    }

    // ─── CENTRO (grafo + resultado) ───────────────────────────────────────────
    private BorderPane crearCentro() {
        BorderPane centro = new BorderPane();

        panelVisual = new PanelVisualizacion();
        ScrollPane scrollGrafo = new ScrollPane(panelVisual);
        scrollGrafo.setFitToWidth(true);
        scrollGrafo.setFitToHeight(true);
        scrollGrafo.setPannable(true);
        scrollGrafo.setStyle(
                "-fx-background-color: #0a0714;" +
                        "-fx-background: #0a0714;"
        );
        centro.setCenter(scrollGrafo);

        // Panel de resultado abajo del grafo
        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(4);
        txtResultado.setWrapText(true);
        txtResultado.setPromptText("El resultado de la ruta calculada aparecerá aquí...");
        txtResultado.setStyle(
                "-fx-control-inner-background: #12082a;" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-prompt-text-fill: #5a4a6a;" +
                        "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 15px;" +
                        "-fx-border-color: " + TERRACOTA + ";" +
                        "-fx-border-width: 2 0 0 0;"
        );
        centro.setBottom(txtResultado);

        return centro;
    }

    // ─── STATUS BAR ───────────────────────────────────────────────────────────
    private HBox crearStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 18, 6, 18));
        bar.setStyle(
                "-fx-background-color: #0a0714;" +
                        "-fx-border-color: " + MID_PURPLE + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        lblStatus = new Label("Listo.");
        lblStatus.setStyle("-fx-text-fill: #7a6a5a; -fx-font-size: 14px;");
        bar.getChildren().add(lblStatus);
        return bar;
    }

    // ─── HELPERS DE UI ────────────────────────────────────────────────────────
    private TextField crearCampo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color: #12082a;" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-prompt-text-fill: #5a4a6a;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 9px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-color: #3a2050;" +
                        "-fx-border-radius: 5px;"
        );
        // Iluminar borde al enfocar
        tf.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                tf.setStyle(tf.getStyle().replace("#3a2050", TERRACOTA));
            } else {
                tf.setStyle(tf.getStyle().replace(TERRACOTA, "#3a2050"));
            }
        });
        return tf;
    }

    private Label crearLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #9a8a7a; -fx-font-size: 13px;");
        return lbl;
    }

    private Label crearLabelSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle(
                "-fx-text-fill: " + BEIGE + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );
        return lbl;
    }

    private Label crearLabelError() {
        Label lbl = new Label("");
        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        lbl.setWrapText(true);
        return lbl;
    }

    private CheckBox crearCheckBox(String texto) {
        CheckBox cb = new CheckBox(texto);
        cb.setSelected(true); // todos activados por defecto
        cb.setStyle("-fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px;");
        return cb;
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        String estiloBase =
                "-fx-background-color: " + TERRACOTA + ";" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-cursor: hand;";
        String estiloHover =
                "-fx-background-color: " + BEIGE + ";" +
                        "-fx-text-fill: " + DARK_PURPLE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, " + BEIGE + ", 8, 0, 0, 0);";
        btn.setStyle(estiloBase);
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));
        return btn;
    }

    // Botón grande especial para calcular
    private Button crearBotonDestacado(String texto) {
        Button btn = new Button(texto);
        String estiloBase =
                "-fx-background-color: linear-gradient(to right, " + MID_PURPLE + ", " + TERRACOTA + ");" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;";
        String estiloHover =
                "-fx-background-color: linear-gradient(to right, #6a2a7e, " + BEIGE + ");" +
                        "-fx-text-fill: " + DARK_PURPLE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, " + TERRACOTA + ", 12, 0, 0, 2);";
        btn.setStyle(estiloBase);
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloBase));
        return btn;
    }

    // Botón pequeño para la barra de header
    private Button crearBotonChico(String texto) {
        Button btn = new Button(texto);
        String base =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + BEIGE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 7 16;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + TERRACOTA + ";" +
                        "-fx-border-radius: 6px;" +
                        "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: " + TERRACOTA + ";" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 7 16;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + TERRACOTA + ";" +
                        "-fx-border-radius: 6px;" +
                        "-fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Separator crearSeparadorSeccion() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #3a2050; -fx-opacity: 0.5;");
        return s;
    }

    private void estilizarCombo(ComboBox<String> cb) {
        cb.setStyle(
                "-fx-background-color: #12082a;" +
                        "-fx-mark-color: " + BEIGE + ";" +
                        "-fx-border-color: #3a2050;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );
    }

    private boolean validarNoVacios(TextField... campos) {
        for (TextField campo : campos) {
            if (campo.getText().trim().isEmpty()) return false;
        }
        return true;
    }

    private void limpiar(TextField... campos) {
        for (TextField campo : campos) campo.clear();
    }

    // Actualiza la barra de estado de abajo
    private void setStatus(String mensaje, boolean esError) {
        String color = esError ? ERROR_RED : "#7a9a6a";
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        lblStatus.setText(mensaje);
    }

    public static void main(String[] args) {
        launch(args);
    }
}