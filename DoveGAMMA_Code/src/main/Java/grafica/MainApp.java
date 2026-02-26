// MainApp.java
package grafica;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import logica.GrafoTransporte;

import java.util.List;

public class MainApp extends Application {

    public static final String DARK_PURPLE = "#1a0a2e";
    public static final String MID_PURPLE  = "#4a1a5e";
    public static final String TERRACOTA   = "#a65d48";
    public static final String BEIGE       = "#d4a574";
    public static final String LIGHT_BEIGE = "#e8c9a8";
    public static final String ERROR_RED   = "#c0392b";

    private PanelVisualizacion panelVisual;
    private TextArea txtResultado;
    private Label lblStatus;

    // Una sola lista compartida para todos los combos de la app
    private ObservableList<String> listaParadas = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // Crear grafo y conectarlo al adaptador ANTES de construir la UI
        GrafoTransporte grafo = new GrafoTransporte();
        panelVisual = new PanelVisualizacion();
        AdaptadorVisual.getInstancia().setBackend(grafo);
        AdaptadorVisual.getInstancia().setPanelVisual(panelVisual);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DARK_PURPLE + ";");

        root.setTop(crearHeader());
        root.setLeft(crearPanelIzquierdo());
        root.setCenter(crearCentro());
        root.setBottom(crearStatusBar());

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("DoveGAMMA - Sistema de Gestion de Rutas");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox crearHeader() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 24, 14, 24));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #0d0520, " + MID_PURPLE + ");" +
                        "-fx-border-color: " + TERRACOTA + ";" +
                        "-fx-border-width: 0 0 2 0;"
        );

        Label emoji = new Label("D");
        emoji.setStyle("-fx-font-size: 26px; -fx-text-fill: " + BEIGE + ";");

        VBox textos = new VBox(2);
        Label titulo = new Label("DoveGAMMA");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + BEIGE + ";");
        Label subtitulo = new Label("Sistema de Gestion de Rutas de Transporte");
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #9a7a5a;");
        textos.getChildren().addAll(titulo, subtitulo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnLimpiar = crearBotonChico("Limpiar todo");
        btnLimpiar.setOnAction(e -> {
            AdaptadorVisual.getInstancia().limpiarTodo();
            listaParadas.clear();
            if (txtResultado != null) txtResultado.clear();
            setStatus("Grafo limpiado.", false);
        });

        header.getChildren().addAll(emoji, textos, spacer, btnLimpiar);
        return header;
    }

    private ScrollPane crearPanelIzquierdo() {
        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(18));
        contenido.setPrefWidth(340);
        contenido.setStyle("-fx-background-color: " + MID_PURPLE + ";");

        Label lblTitulo = new Label("Panel de Control");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + LIGHT_BEIGE + ";");

        contenido.getChildren().addAll(
                lblTitulo,
                crearSep(),
                crearSeccionParada(),
                crearSep(),
                crearSeccionRuta(),
                crearSep(),
                crearSeccionEliminar(),
                crearSep(),
                crearSeccionCalcular()
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + MID_PURPLE + "; -fx-background: " + MID_PURPLE + ";");
        return scroll;
    }

    // ─── NUEVA PARADA ─────────────────────────────────────────────────────────
    private VBox crearSeccionParada() {
        VBox sec = new VBox(8);
        TextField txtId     = crearCampo("ID unico  (ej: P001)");
        TextField txtNombre = crearCampo("Nombre de la parada");
        TextField txtX      = crearCampo("Coordenada X  (0-860)");
        TextField txtY      = crearCampo("Coordenada Y  (0-600)");
        Label lblErr = crearLabelError();
        Button btn = crearBoton("+ Agregar Parada");
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnAction(e -> {
            lblErr.setText("");
            if (!noVacios(txtId, txtNombre, txtX, txtY)) {
                lblErr.setText("Todos los campos son obligatorios.");
                return;
            }
            try {
                String id     = txtId.getText().trim();
                String nombre = txtNombre.getText().trim();
                double x      = Double.parseDouble(txtX.getText().trim());
                double y      = Double.parseDouble(txtY.getText().trim());

                if (AdaptadorVisual.getInstancia().agregarParada(id, nombre, x, y)) {
                    listaParadas.add(id + " - " + nombre);
                    limpiar(txtId, txtNombre, txtX, txtY);
                    setStatus("Parada agregada: " + nombre, false);
                } else {
                    lblErr.setText("Backend no conectado.");
                }
            } catch (NumberFormatException ex) {
                lblErr.setText("X e Y deben ser numeros.");
            }
        });

        sec.getChildren().addAll(
                crearLblSec("Nueva Parada"),
                crearLbl("ID:"), txtId,
                crearLbl("Nombre:"), txtNombre,
                crearLbl("Pos. X:"), txtX,
                crearLbl("Pos. Y:"), txtY,
                lblErr, btn
        );
        return sec;
    }

    // ─── NUEVA RUTA ───────────────────────────────────────────────────────────
    private VBox crearSeccionRuta() {
        VBox sec = new VBox(8);
        ComboBox<String> cbO = crearCombo("Parada origen");
        ComboBox<String> cbD = crearCombo("Parada destino");
        cbO.setItems(listaParadas);
        cbD.setItems(listaParadas);

        TextField txtT = crearCampo("Tiempo (minutos)");
        TextField txtDist = crearCampo("Distancia (km)");
        TextField txtC = crearCampo("Costo (RD$)");
        Label lblErr = crearLabelError();
        Button btn = crearBoton("Crear Ruta");
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnAction(e -> {
            lblErr.setText("");
            if (cbO.getValue() == null || cbD.getValue() == null) {
                lblErr.setText("Selecciona origen y destino."); return;
            }
            if (cbO.getValue().equals(cbD.getValue())) {
                lblErr.setText("Origen y destino deben ser diferentes."); return;
            }
            if (!noVacios(txtT, txtDist, txtC)) {
                lblErr.setText("Completa tiempo, distancia y costo."); return;
            }
            try {
                String o = cbO.getValue().split(" - ")[0];
                String d = cbD.getValue().split(" - ")[0];
                double t  = Double.parseDouble(txtT.getText().trim());
                double di = Double.parseDouble(txtDist.getText().trim());
                double c  = Double.parseDouble(txtC.getText().trim());

                AdaptadorVisual.getInstancia().agregarRuta(o, d, t, di, c);
                limpiar(txtT, txtDist, txtC);
                setStatus("Ruta creada: " + o + " -> " + d, false);
            } catch (NumberFormatException ex) {
                lblErr.setText("Tiempo, distancia y costo deben ser numeros.");
            }
        });

        sec.getChildren().addAll(
                crearLblSec("Nueva Ruta"),
                crearLbl("Origen:"), cbO,
                crearLbl("Destino:"), cbD,
                crearLbl("Tiempo (min):"), txtT,
                crearLbl("Distancia (km):"), txtDist,
                crearLbl("Costo (RD$):"), txtC,
                lblErr, btn
        );
        return sec;
    }

    // ─── ELIMINAR ─────────────────────────────────────────────────────────────
    private VBox crearSeccionEliminar() {
        VBox sec = new VBox(8);

        // Eliminar parada
        TextField txtIdElim = crearCampo("ID de parada a eliminar");
        Label lblErrP = crearLabelError();
        Button btnElimP = crearBotonPeligro("Eliminar Parada");
        btnElimP.setMaxWidth(Double.MAX_VALUE);

        btnElimP.setOnAction(e -> {
            lblErrP.setText("");
            String id = txtIdElim.getText().trim();
            if (id.isEmpty()) { lblErrP.setText("Escribe el ID."); return; }
            if (AdaptadorVisual.getInstancia().eliminarParada(id)) {
                listaParadas.removeIf(item -> item.startsWith(id + " - "));
                panelVisual.limpiarTodo();
                txtIdElim.clear();
                setStatus("Parada eliminada: " + id, false);
            } else {
                lblErrP.setText("Parada no encontrada: " + id);
            }
        });

        // Eliminar ruta
        ComboBox<String> cbOElim = crearCombo("Origen de la ruta");
        ComboBox<String> cbDElim = crearCombo("Destino de la ruta");
        cbOElim.setItems(listaParadas);
        cbDElim.setItems(listaParadas);
        Label lblErrR = crearLabelError();
        Button btnElimR = crearBotonPeligro("Eliminar Ruta");
        btnElimR.setMaxWidth(Double.MAX_VALUE);

        btnElimR.setOnAction(e -> {
            lblErrR.setText("");
            if (cbOElim.getValue() == null || cbDElim.getValue() == null) {
                lblErrR.setText("Selecciona origen y destino."); return;
            }
            String o = cbOElim.getValue().split(" - ")[0];
            String d = cbDElim.getValue().split(" - ")[0];
            AdaptadorVisual.getInstancia().eliminarRuta(o, d);
            panelVisual.limpiarTodo();
            setStatus("Ruta eliminada: " + o + " -> " + d, false);
        });

        sec.getChildren().addAll(
                crearLblSec("Eliminar"),
                crearLbl("ID de parada:"), txtIdElim,
                lblErrP, btnElimP,
                crearLbl("Ruta origen:"), cbOElim,
                crearLbl("Ruta destino:"), cbDElim,
                lblErrR, btnElimR
        );
        return sec;
    }

    // ─── CALCULAR RUTA ────────────────────────────────────────────────────────
    private VBox crearSeccionCalcular() {
        VBox sec = new VBox(8);

        ComboBox<String> cbIni = crearCombo("Parada inicio");
        ComboBox<String> cbFin = crearCombo("Parada fin");
        cbIni.setItems(listaParadas);
        cbFin.setItems(listaParadas);

        CheckBox ckT = crearCheck("Menor tiempo");
        CheckBox ckD = crearCheck("Menor distancia");
        CheckBox ckC = crearCheck("Menor costo");
        CheckBox ckTr = crearCheck("Menos transbordos");

        VBox grupoChecks = new VBox(10, ckT, ckD, ckC, ckTr);
        grupoChecks.setPadding(new Insets(12));
        grupoChecks.setStyle(
                "-fx-background-color: #12082a;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: #3a2050;" +
                        "-fx-border-radius: 6px;"
        );

        Label lblErr = crearLabelError();
        Button btnCalc = crearBotonDestacado("CALCULAR");
        btnCalc.setMaxWidth(Double.MAX_VALUE);

        btnCalc.setOnAction(e -> {
            lblErr.setText("");
            if (cbIni.getValue() == null || cbFin.getValue() == null) {
                lblErr.setText("Selecciona inicio y fin."); return;
            }
            if (cbIni.getValue().equals(cbFin.getValue())) {
                lblErr.setText("Inicio y fin deben ser diferentes."); return;
            }
            if (!ckT.isSelected() && !ckD.isSelected() && !ckC.isSelected() && !ckTr.isSelected()) {
                lblErr.setText("Marca al menos un criterio."); return;
            }

            String idI = cbIni.getValue().split(" - ")[0];
            String idF = cbFin.getValue().split(" - ")[0];
            StringBuilder resultado = new StringBuilder();
            List<String> primerCamino = null;

            if (ckT.isSelected()) {
                resultado.append(AdaptadorVisual.getInstancia().calcularRuta(idI, idF, "tiempo")).append("\n\n");
                primerCamino = AdaptadorVisual.getInstancia().getBackend().calcularDijkstra(idI, idF, "tiempo");
            }
            if (ckD.isSelected()) {
                resultado.append(AdaptadorVisual.getInstancia().calcularRuta(idI, idF, "distancia")).append("\n\n");
                if (primerCamino == null)
                    primerCamino = AdaptadorVisual.getInstancia().getBackend().calcularDijkstra(idI, idF, "distancia");
            }
            if (ckC.isSelected()) {
                resultado.append(AdaptadorVisual.getInstancia().calcularRuta(idI, idF, "costo")).append("\n\n");
                if (primerCamino == null)
                    primerCamino = AdaptadorVisual.getInstancia().getBackend().calcularDijkstra(idI, idF, "costo");
            }
            if (ckTr.isSelected()) {
                // transbordos = peso 1 por arista — ver nota para compañero
                resultado.append(AdaptadorVisual.getInstancia().calcularRuta(idI, idF, "transbordos")).append("\n\n");
                if (primerCamino == null)
                    primerCamino = AdaptadorVisual.getInstancia().getBackend().calcularDijkstra(idI, idF, "transbordos");
            }

            txtResultado.setText(resultado.toString().trim());

            if (primerCamino != null && !primerCamino.isEmpty())
                panelVisual.resaltarRuta(primerCamino);

            setStatus("Calculo completado.", false);
        });

        sec.getChildren().addAll(
                crearLblSec("Calcular Ruta"),
                crearLbl("Inicio:"), cbIni,
                crearLbl("Fin:"), cbFin,
                crearLbl("Optimizar por (puedes marcar varios):"),
                grupoChecks,
                lblErr, btnCalc
        );
        return sec;
    }

    // ─── CENTRO ───────────────────────────────────────────────────────────────
    private BorderPane crearCentro() {
        BorderPane centro = new BorderPane();
        ScrollPane scrollGrafo = new ScrollPane(panelVisual);
        scrollGrafo.setFitToWidth(true);
        scrollGrafo.setFitToHeight(true);
        scrollGrafo.setPannable(true);
        scrollGrafo.setStyle("-fx-background-color: #0a0714; -fx-background: #0a0714;");
        centro.setCenter(scrollGrafo);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(5);
        txtResultado.setWrapText(true);
        txtResultado.setPromptText("El resultado del calculo aparecera aqui...");
        txtResultado.setStyle(
                "-fx-control-inner-background: #12082a;" +
                        "-fx-text-fill: " + LIGHT_BEIGE + ";" +
                        "-fx-prompt-text-fill: #5a4a6a;" +
                        "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 14px;" +
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
        bar.setStyle("-fx-background-color: #0a0714; -fx-border-color: " + MID_PURPLE + "; -fx-border-width: 1 0 0 0;");
        lblStatus = new Label("Listo.");
        lblStatus.setStyle("-fx-text-fill: #7a6a5a; -fx-font-size: 14px;");
        bar.getChildren().add(lblStatus);
        return bar;
    }

    // ─── HELPERS UI ───────────────────────────────────────────────────────────
    private TextField crearCampo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        String base = "-fx-background-color: #12082a; -fx-text-fill: " + LIGHT_BEIGE + "; -fx-prompt-text-fill: #5a4a6a; -fx-font-size: 14px; -fx-padding: 9px; -fx-background-radius: 5px; -fx-border-color: #3a2050; -fx-border-radius: 5px;";
        tf.setStyle(base);
        tf.focusedProperty().addListener((o, old, focused) ->
                tf.setStyle(focused ? base.replace("#3a2050", TERRACOTA) : base));
        return tf;
    }

    private ComboBox<String> crearCombo(String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("-fx-background-color: #12082a; -fx-mark-color: " + BEIGE + "; -fx-font-size: 14px; -fx-border-color: #3a2050; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        return cb;
    }

    private Label crearLbl(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: #9a8a7a; -fx-font-size: 13px;");
        return lbl;
    }

    private Label crearLblSec(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: " + BEIGE + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        return lbl;
    }

    private Label crearLabelError() {
        Label lbl = new Label("");
        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        lbl.setWrapText(true);
        return lbl;
    }

    private CheckBox crearCheck(String texto) {
        CheckBox cb = new CheckBox(texto);
        cb.setSelected(true);
        cb.setStyle("-fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px;");
        return cb;
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color: " + TERRACOTA + "; -fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 6px; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + BEIGE + "; -fx-text-fill: " + DARK_PURPLE + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 6px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + BEIGE + ", 8, 0, 0, 0);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button crearBotonPeligro(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color: #6b2020; -fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 6px; -fx-cursor: hand;";
        String hover = "-fx-background-color: #8b2828; -fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 6px; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button crearBotonDestacado(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color: linear-gradient(to right, " + MID_PURPLE + ", " + TERRACOTA + "); -fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 16; -fx-background-radius: 8px; -fx-cursor: hand;";
        String hover = "-fx-background-color: linear-gradient(to right, #6a2a7e, " + BEIGE + "); -fx-text-fill: " + DARK_PURPLE + "; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 16; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + TERRACOTA + ", 12, 0, 0, 2);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Button crearBotonChico(String texto) {
        Button btn = new Button(texto);
        String base  = "-fx-background-color: transparent; -fx-text-fill: " + BEIGE + "; -fx-font-size: 14px; -fx-padding: 7 16; -fx-background-radius: 6px; -fx-border-color: " + TERRACOTA + "; -fx-border-radius: 6px; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + TERRACOTA + "; -fx-text-fill: " + LIGHT_BEIGE + "; -fx-font-size: 14px; -fx-padding: 7 16; -fx-background-radius: 6px; -fx-border-color: " + TERRACOTA + "; -fx-border-radius: 6px; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private Separator crearSep() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #3a2050; -fx-opacity: 0.6;");
        return s;
    }

    private boolean noVacios(TextField... campos) {
        for (TextField c : campos) if (c.getText().trim().isEmpty()) return false;
        return true;
    }

    private void limpiar(TextField... campos) {
        for (TextField c : campos) c.clear();
    }

    private void setStatus(String msg, boolean esError) {
        String color = esError ? ERROR_RED : "#7a9a6a";
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        lblStatus.setText(msg);
    }

    public static void main(String[] args) { launch(args); }
}