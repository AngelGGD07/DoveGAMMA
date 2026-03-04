// ControladorPrincipal.java
package grafica;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import logica.GrafoTransporte;

import java.util.List;

public class ControladorPrincipal {

    // Sidebar
    @FXML private Canvas canvasLogo;
    @FXML private Button btnNavAgregar, btnNavModificar, btnNavEliminar, btnNavCalcular;
    @FXML private VBox   subMenuAgregar, subMenuModificar, subMenuEliminar;

    // Formularios
    @FXML private VBox formAgregarParada, formAgregarRuta;
    @FXML private VBox formModParada,     formModRuta;
    @FXML private VBox formElimParada,    formElimRuta;
    @FXML private VBox formCalcular,      panelResultado;

    // Agregar parada
    @FXML private TextField txtIdParada, txtNombreParada, txtXParada, txtYParada;

    // Agregar ruta
    @FXML private TextField txtOrigenRuta, txtDestinoRuta;
    @FXML private TextField txtTiempoRuta, txtDistanciaRuta, txtCostoRuta;

    // Modificar parada
    @FXML private TextField txtModIdParada, txtModNombreParada;

    // Modificar ruta
    @FXML private TextField txtModOrigenRuta, txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta;

    // Eliminar
    @FXML private TextField txtDelIdParada;
    @FXML private TextField txtDelOrigenRuta, txtDelDestinoRuta;

    // Calcular
    @FXML private TextField        txtCalcInicio, txtCalcFin;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private TextArea         txtResultado;

    // Toast de mensajes
    @FXML private Label lblMensaje;

    // Contenedor del grafo
    @FXML private StackPane contenedorGrafo;

    // Array con todos los forms para esconderlos fácil
    private VBox[] todosLosForms;

    // ── INITIALIZE ───────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Arrancar backend
        GrafoTransporte grafo = new GrafoTransporte();
        PanelVisualizacion panelVisual = new PanelVisualizacion();
        AdaptadorVisual.getInstancia().setBackend(grafo);
        AdaptadorVisual.getInstancia().setPanelVisual(panelVisual);

        // Meter el panel del grafo en el StackPane del FXML
        contenedorGrafo.getChildren().add(panelVisual);
        StackPane.setAlignment(panelVisual, javafx.geometry.Pos.TOP_LEFT);

        // Hacer que el panel llene todo el contenedor
        panelVisual.prefWidthProperty().bind(contenedorGrafo.widthProperty());
        panelVisual.prefHeightProperty().bind(contenedorGrafo.heightProperty());

        todosLosForms = new VBox[]{
                formAgregarParada, formAgregarRuta,
                formModParada,     formModRuta,
                formElimParada,    formElimRuta,
                formCalcular
        };

        // Opciones del combo de criterio
        cmbCriterio.setItems(FXCollections.observableArrayList(
                "tiempo", "distancia", "costo", "transbordos"
        ));
        cmbCriterio.getSelectionModel().selectFirst();

        dibujarLogo();
    }

    // ── LOGO EN CANVAS ────────────────────────────────────────────────────────
    private void dibujarLogo() {
        GraphicsContext gc = canvasLogo.getGraphicsContext2D();
        double w = canvasLogo.getWidth();
        double h = canvasLogo.getHeight();

        gc.setFill(Color.web("#120d22"));
        gc.fillRect(0, 0, w, h);

        double[][] pts = {{20,15},{78,15},{50,45},{18,62},{82,62}};
        int[][] con    = {{0,1},{0,2},{1,2},{2,3},{2,4},{3,4}};

        gc.setStroke(Color.web("#a65d48"));
        gc.setLineWidth(1.5);
        for (int[] c : con)
            gc.strokeLine(pts[c[0]][0], pts[c[0]][1], pts[c[1]][0], pts[c[1]][1]);

        gc.setFill(Color.web("#d4a574"));
        for (double[] p : pts)
            gc.fillOval(p[0]-5, p[1]-5, 10, 10);

        // nodo central resaltado
        gc.setFill(Color.web("#e8c9a8"));
        gc.fillOval(pts[2][0]-7, pts[2][1]-7, 14, 14);
    }

    // ── NAV: SUBMENÚS ─────────────────────────────────────────────────────────
    @FXML private void mostrarPanelAgregar()  { toggleSub(subMenuAgregar);   activarBtn(btnNavAgregar);   }
    @FXML private void mostrarPanelModificar(){ toggleSub(subMenuModificar);  activarBtn(btnNavModificar); }
    @FXML private void mostrarPanelEliminar() { toggleSub(subMenuEliminar);   activarBtn(btnNavEliminar);  }

    @FXML
    private void mostrarPanelCalcular() {
        cerrarSubMenus();
        activarBtn(btnNavCalcular);
        mostrarForm(formCalcular);
    }

    // ── NAV: BOTONES DE FORMULARIOS ───────────────────────────────────────────
    @FXML private void mostrarFormAgregarParada() { mostrarForm(formAgregarParada); }
    @FXML private void mostrarFormAgregarRuta()   { mostrarForm(formAgregarRuta);   }
    @FXML private void mostrarFormModParada()     { mostrarForm(formModParada);     }
    @FXML private void mostrarFormModRuta()       { mostrarForm(formModRuta);       }
    @FXML private void mostrarFormElimParada()    { mostrarForm(formElimParada);    }
    @FXML private void mostrarFormElimRuta()      { mostrarForm(formElimRuta);      }

    // ── ACCIONES ──────────────────────────────────────────────────────────────
    @FXML
    private void agregarParada() {
        String id     = txtIdParada.getText().trim();
        String nombre = txtNombreParada.getText().trim();
        String xStr   = txtXParada.getText().trim();
        String yStr   = txtYParada.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || xStr.isEmpty() || yStr.isEmpty()) {
            error("Todos los campos son obligatorios."); return;
        }
        try {
            boolean ok = AdaptadorVisual.getInstancia().agregarParada(
                    id, nombre,
                    Double.parseDouble(xStr),
                    Double.parseDouble(yStr)
            );
            if (ok) {
                limpiar(txtIdParada, txtNombreParada, txtXParada, txtYParada);
                exito("Parada agregada: " + nombre);
            } else {
                error("Ya existe una parada con ese ID o ese nombre.");
            }
        } catch (NumberFormatException e) {
            error("X e Y deben ser numeros.");
        }
    }

    @FXML
    private void agregarRuta() {
        String o = txtOrigenRuta.getText().trim();
        String d = txtDestinoRuta.getText().trim();
        String t = txtTiempoRuta.getText().trim();
        String di = txtDistanciaRuta.getText().trim();
        String c = txtCostoRuta.getText().trim();

        if (o.isEmpty() || d.isEmpty() || t.isEmpty() || di.isEmpty() || c.isEmpty()) {
            error("Todos los campos son obligatorios."); return;
        }
        if (o.equals(d)) { error("Origen y destino deben ser diferentes."); return; }

        try {
            boolean ok = AdaptadorVisual.getInstancia().agregarRuta(
                    o, d,
                    Double.parseDouble(t),
                    Double.parseDouble(di),
                    Double.parseDouble(c)
            );
            if (ok) {
                limpiar(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta, txtDistanciaRuta, txtCostoRuta);
                exito("Ruta creada: " + o + " -> " + d);
            } else {
                error("Paradas no encontradas. Agrega las paradas primero.");
            }
        } catch (NumberFormatException e) {
            error("Tiempo, distancia y costo deben ser numeros.");
        }
    }

    @FXML
    private void modificarParada() {
        String id     = txtModIdParada.getText().trim();
        String nombre = txtModNombreParada.getText().trim();

        if (id.isEmpty() || nombre.isEmpty()) { error("Todos los campos son obligatorios."); return; }

        boolean ok = AdaptadorVisual.getInstancia().modificarParada(id, nombre);
        if (ok) {
            limpiar(txtModIdParada, txtModNombreParada);
            exito("Parada actualizada: " + id);
        } else {
            error("Parada no encontrada o el nuevo nombre ya existe.");
        }
    }

    @FXML
    private void modificarRuta() {
        String o  = txtModOrigenRuta.getText().trim();
        String d  = txtModDestinoRuta.getText().trim();
        String t  = txtModTiempoRuta.getText().trim();
        String di = txtModDistanciaRuta.getText().trim();
        String c  = txtModCostoRuta.getText().trim();

        if (o.isEmpty() || d.isEmpty() || t.isEmpty() || di.isEmpty() || c.isEmpty()) {
            error("Todos los campos son obligatorios."); return;
        }
        try {
            boolean ok = AdaptadorVisual.getInstancia().modificarRuta(
                    o, d,
                    Double.parseDouble(t),
                    Double.parseDouble(di),
                    Double.parseDouble(c)
            );
            if (ok) {
                limpiar(txtModOrigenRuta, txtModDestinoRuta, txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta);
                exito("Ruta actualizada: " + o + " -> " + d);
            } else {
                error("Ruta no encontrada. Verifica IDs y la direccion.");
            }
        } catch (NumberFormatException e) {
            error("Tiempo, distancia y costo deben ser numeros.");
        }
    }

    @FXML
    private void eliminarParada() {
        String id = txtDelIdParada.getText().trim();
        if (id.isEmpty()) { error("Escribe el ID de la parada."); return; }

        if (AdaptadorVisual.getInstancia().eliminarParada(id)) {
            txtDelIdParada.clear();
            exito("Parada eliminada: " + id);
        } else {
            error("Parada no encontrada: " + id);
        }
    }

    @FXML
    private void eliminarRuta() {
        String o = txtDelOrigenRuta.getText().trim();
        String d = txtDelDestinoRuta.getText().trim();
        if (o.isEmpty() || d.isEmpty()) { error("Escribe ID de origen y destino."); return; }

        if (AdaptadorVisual.getInstancia().eliminarRuta(o, d)) {
            limpiar(txtDelOrigenRuta, txtDelDestinoRuta);
            exito("Ruta eliminada: " + o + " -> " + d);
        } else {
            error("No existe ruta de " + o + " a " + d + " en esa direccion.");
        }
    }

    @FXML
    private void calcularRuta() {
        String idI = txtCalcInicio.getText().trim();
        String idF = txtCalcFin.getText().trim();
        String cri = cmbCriterio.getValue();

        if (idI.isEmpty() || idF.isEmpty()) { error("Escribe el ID de inicio y fin."); return; }
        if (idI.equals(idF)) { error("Inicio y fin deben ser diferentes."); return; }

        String resultado = AdaptadorVisual.getInstancia().calcularRuta(idI, idF, cri);
        txtResultado.setText(resultado);

        // Resaltar ruta en el grafo
        List<String> camino = AdaptadorVisual.getInstancia().getBackend()
                .calcularDijkstra(idI, idF, cri);
        if (!camino.isEmpty())
            AdaptadorVisual.getInstancia().getPanelVisual().resaltarRuta(camino);

        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
        ocultarMsg();
    }

    @FXML
    private void limpiarTodo() {
        AdaptadorVisual.getInstancia().limpiarTodo();
        txtResultado.clear();
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
        exito("Grafo limpiado.");
    }

    // ── HELPERS NAV ───────────────────────────────────────────────────────────
    private void toggleSub(VBox sub) {
        boolean abierto = sub.isVisible();
        cerrarSubMenus();
        if (!abierto) { sub.setVisible(true); sub.setManaged(true); }
    }

    private void cerrarSubMenus() {
        for (VBox s : new VBox[]{subMenuAgregar, subMenuModificar, subMenuEliminar}) {
            s.setVisible(false); s.setManaged(false);
        }
    }

    private void mostrarForm(VBox formTarget) {
        for (VBox f : todosLosForms) { f.setVisible(false); f.setManaged(false); }
        formTarget.setVisible(true);
        formTarget.setManaged(true);
        ocultarMsg();
    }

    private void activarBtn(Button activo) {
        String BASE   = "-fx-background-color: #2a1a40; -fx-text-fill: #d4a574; -fx-font-size: 13; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-background-radius: 8; -fx-cursor: hand;";
        String ACTIVO = "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; -fx-font-size: 13; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-background-radius: 8; -fx-cursor: hand;";
        for (Button b : new Button[]{btnNavAgregar, btnNavModificar, btnNavEliminar, btnNavCalcular})
            b.setStyle(b == activo ? ACTIVO : BASE);
    }

    // ── HELPERS MENSAJES ──────────────────────────────────────────────────────
    private void exito(String msg) {
        lblMensaje.setText("✔  " + msg);
        lblMensaje.setStyle("-fx-text-fill: #7acc7a; -fx-background-color: #0a2a0a; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void error(String msg) {
        lblMensaje.setText("⚠  " + msg);
        lblMensaje.setStyle("-fx-text-fill: #e07070; -fx-background-color: #2a0a0a; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void ocultarMsg() { lblMensaje.setVisible(false); lblMensaje.setManaged(false); }

    private void limpiar(TextField... campos) { for (TextField tf : campos) tf.clear(); }
}