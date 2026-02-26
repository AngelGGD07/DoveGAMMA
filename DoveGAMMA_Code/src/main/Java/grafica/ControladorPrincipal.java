// ControladorPrincipal.java
package grafica;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import logica.GrafoTransporte;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorPrincipal implements Initializable {

    // ── campos del form AGREGAR ──
    @FXML private TextField txtIdParada, txtNombreParada, txtXParada, txtYParada;
    @FXML private TextField txtOrigenRuta, txtDestinoRuta, txtTiempoRuta, txtDistanciaRuta, txtCostoRuta;

    // ── campos del form MODIFICAR ──
    @FXML private TextField txtModIdParada, txtModNombreParada;
    @FXML private TextField txtModOrigenRuta, txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta;

    // ── campos del form ELIMINAR ──
    @FXML private TextField txtDelIdParada;
    @FXML private TextField txtDelOrigenRuta, txtDelDestinoRuta;

    // ── campos del form CALCULAR ──
    @FXML private TextField txtCalcInicio, txtCalcFin;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private TextArea txtResultado;
    @FXML private VBox panelResultado;

    // ── los paneles/forms (se muestran de uno en uno) ──
    @FXML private VBox formAgregarParada, formAgregarRuta;
    @FXML private VBox formModParada, formModRuta;
    @FXML private VBox formElimParada, formElimRuta;
    @FXML private VBox formCalcular;

    // ── submenus del sidebar ──
    @FXML private VBox subMenuAgregar, subMenuModificar, subMenuEliminar;

    // ── botones del sidebar (para cambiar su color cuando están activos) ──
    @FXML private Button btnNavAgregar, btnNavModificar, btnNavEliminar, btnNavCalcular;

    // ── contenedor donde va el PanelVisualizacion ──
    @FXML private javafx.scene.layout.StackPane contenedorGrafo;

    // ── mensajes al usuario ──
    @FXML private Label lblMensaje;

    // ── el logo ──
    @FXML private Canvas canvasLogo;

    private AdaptadorVisual adaptador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        adaptador = AdaptadorVisual.getInstancia();
        adaptador.setBackend(new GrafoTransporte());

        // agrego el panel del grafo al contenedor
        PanelVisualizacion panel = new PanelVisualizacion();
        adaptador.setPanelVisual(panel);
        contenedorGrafo.getChildren().add(panel);

        // opciones del combo
        cmbCriterio.getItems().addAll("tiempo", "distancia", "costo", "transbordos");
        cmbCriterio.setValue("tiempo");

        // dibujo el logo
        dibujarLogo();
    }

    // ── el logo: palomita + Gamma dibujados en canvas ──
    private void dibujarLogo() {
        GraphicsContext gc = canvasLogo.getGraphicsContext2D();

        // fondo
        gc.setFill(Color.web("#120d22"));
        gc.fillRect(0, 0, 100, 70);

        // palomita simple con curvas
        gc.setStroke(Color.web("#d4a574"));
        gc.setLineWidth(2.5);
        gc.beginPath();
        // cuerpo
        gc.moveTo(50, 45);
        gc.bezierCurveTo(35, 35, 20, 28, 25, 20);
        gc.bezierCurveTo(30, 12, 45, 22, 50, 32);
        // ala derecha
        gc.moveTo(50, 32);
        gc.bezierCurveTo(55, 22, 70, 12, 75, 20);
        gc.bezierCurveTo(80, 28, 65, 35, 50, 45);
        // colita
        gc.moveTo(50, 45);
        gc.lineTo(42, 58);
        gc.moveTo(50, 45);
        gc.lineTo(58, 58);
        gc.stroke();

        // cabecita
        gc.setFill(Color.web("#d4a574"));
        gc.fillOval(44, 17, 12, 12);

        // Gamma (Γ) en color beige
        gc.setFill(Color.web("#a65d48"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 22));
        gc.fillText("Γ", 40, 68);
    }

    // ══════════════════════ NAVEGACION DEL SIDEBAR ══════════════════════

    // resetea el color de todos los botones del nav
    private void resetarNav() {
        String estiloBase = "-fx-background-color: #2a1a40; -fx-text-fill: #d4a574; " +
                "-fx-font-size: 13; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";
        btnNavAgregar.setStyle(estiloBase);
        btnNavModificar.setStyle(estiloBase);
        btnNavEliminar.setStyle(estiloBase);
        btnNavCalcular.setStyle(estiloBase);

        // esconder todos los submenús
        ocultarSubMenu(subMenuAgregar);
        ocultarSubMenu(subMenuModificar);
        ocultarSubMenu(subMenuEliminar);

        // esconder todos los forms
        esconderTodosLosForms();
    }

    private String estiloNavActivo() {
        return "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; " +
                "-fx-font-size: 13; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";
    }

    @FXML
    private void mostrarPanelAgregar() {
        resetarNav();
        btnNavAgregar.setStyle(estiloNavActivo());
        mostrarSubMenu(subMenuAgregar);
        mostrarForm(formAgregarParada); // por defecto muestra parada
    }

    @FXML
    private void mostrarPanelModificar() {
        resetarNav();
        btnNavModificar.setStyle(estiloNavActivo());
        mostrarSubMenu(subMenuModificar);
        mostrarForm(formModParada);
    }

    @FXML
    private void mostrarPanelEliminar() {
        resetarNav();
        btnNavEliminar.setStyle(estiloNavActivo());
        mostrarSubMenu(subMenuEliminar);
        mostrarForm(formElimParada);
    }

    @FXML
    private void mostrarPanelCalcular() {
        resetarNav();
        btnNavCalcular.setStyle(estiloNavActivo());
        mostrarForm(formCalcular);
    }

    // sub-botones del menú agregar
    @FXML private void mostrarFormAgregarParada() { mostrarForm(formAgregarParada); }
    @FXML private void mostrarFormAgregarRuta()   { mostrarForm(formAgregarRuta); }

    // sub-botones del menú modificar
    @FXML private void mostrarFormModParada() { mostrarForm(formModParada); }
    @FXML private void mostrarFormModRuta()   { mostrarForm(formModRuta); }

    // sub-botones del menú eliminar
    @FXML private void mostrarFormElimParada() { mostrarForm(formElimParada); }
    @FXML private void mostrarFormElimRuta()   { mostrarForm(formElimRuta); }

    // ══════════════════════ ACCIONES ══════════════════════

    @FXML
    private void agregarParada() {
        String id     = txtIdParada.getText().trim();
        String nombre = txtNombreParada.getText().trim();
        String xTxt   = txtXParada.getText().trim();
        String yTxt   = txtYParada.getText().trim();

        if (id.isEmpty() || nombre.isEmpty() || xTxt.isEmpty() || yTxt.isEmpty()) {
            mostrarMensaje("⚠  Llena todos los campos", false);
            return;
        }

        double x, y;
        try {
            x = Double.parseDouble(xTxt);
            y = Double.parseDouble(yTxt);
        } catch (NumberFormatException e) {
            mostrarMensaje("⚠  X e Y tienen que ser números", false);
            return;
        }

        // el adaptador valida duplicados, devuelve false si ya existe
        boolean ok = adaptador.agregarParada(id, nombre, x, y);
        if (!ok) {
            mostrarMensaje("⚠  Ese ID o nombre ya existe", false);
        } else {
            mostrarMensaje("✓  Parada '" + nombre + "' agregada", true);
            limpiarCampos(txtIdParada, txtNombreParada, txtXParada, txtYParada);
        }
    }

    @FXML
    private void agregarRuta() {
        String origen  = txtOrigenRuta.getText().trim();
        String destino = txtDestinoRuta.getText().trim();
        String tTxt    = txtTiempoRuta.getText().trim();
        String dTxt    = txtDistanciaRuta.getText().trim();
        String cTxt    = txtCostoRuta.getText().trim();

        if (origen.isEmpty() || destino.isEmpty() || tTxt.isEmpty() || dTxt.isEmpty() || cTxt.isEmpty()) {
            mostrarMensaje("⚠  Llena todos los campos", false);
            return;
        }

        try {
            double tiempo    = Double.parseDouble(tTxt);
            double distancia = Double.parseDouble(dTxt);
            double costo     = Double.parseDouble(cTxt);

            boolean ok = adaptador.agregarRuta(origen, destino, tiempo, distancia, costo);
            if (!ok) {
                mostrarMensaje("⚠  Una de esas paradas no existe", false);
            } else {
                mostrarMensaje("✓  Ruta " + origen + " → " + destino + " agregada", true);
                limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta, txtDistanciaRuta, txtCostoRuta);
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("⚠  Tiempo, distancia y costo deben ser números", false);
        }
    }

    @FXML
    private void modificarParada() {
        String id     = txtModIdParada.getText().trim();
        String nombre = txtModNombreParada.getText().trim();

        if (id.isEmpty() || nombre.isEmpty()) {
            mostrarMensaje("⚠  Llena el ID y el nuevo nombre", false);
            return;
        }

        // OJO al socio: agregar modificarParada(id, nombre) en GrafoTransporte y en AdaptadorVisual
        boolean ok = adaptador.modificarParada(id, nombre);
        if (!ok) {
            mostrarMensaje("⚠  No existe esa parada", false);
        } else {
            mostrarMensaje("✓  Parada actualizada", true);
            limpiarCampos(txtModIdParada, txtModNombreParada);
        }
    }

    @FXML
    private void modificarRuta() {
        String origen  = txtModOrigenRuta.getText().trim();
        String destino = txtModDestinoRuta.getText().trim();
        String tTxt    = txtModTiempoRuta.getText().trim();
        String dTxt    = txtModDistanciaRuta.getText().trim();
        String cTxt    = txtModCostoRuta.getText().trim();

        if (origen.isEmpty() || destino.isEmpty() || tTxt.isEmpty() || dTxt.isEmpty() || cTxt.isEmpty()) {
            mostrarMensaje("⚠  Llena todos los campos", false);
            return;
        }

        try {
            double tiempo    = Double.parseDouble(tTxt);
            double distancia = Double.parseDouble(dTxt);
            double costo     = Double.parseDouble(cTxt);

            // OJO al socio: agregar modificarRuta() en GrafoTransporte y AdaptadorVisual
            boolean ok = adaptador.modificarRuta(origen, destino, tiempo, distancia, costo);
            if (!ok) {
                mostrarMensaje("⚠  Esa ruta no existe", false);
            } else {
                mostrarMensaje("✓  Ruta actualizada", true);
                limpiarCampos(txtModOrigenRuta, txtModDestinoRuta, txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta);
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("⚠  Tiempo, distancia y costo deben ser números", false);
        }
    }

    @FXML
    private void eliminarParada() {
        String id = txtDelIdParada.getText().trim();
        if (id.isEmpty()) {
            mostrarMensaje("⚠  Escribe el ID", false);
            return;
        }

        // esto solo elimina la parada, NO las rutas visualmente que tienen otro origen
        boolean ok = adaptador.eliminarParada(id);
        if (!ok) {
            mostrarMensaje("⚠  No existe esa parada", false);
        } else {
            // redibujamos todo el grafo (la forma más simple)
            adaptador.redibujarGrafoCompleto();
            mostrarMensaje("✓  Parada eliminada", true);
            limpiarCampos(txtDelIdParada);
        }
    }

    @FXML
    private void eliminarRuta() {
        String origen  = txtDelOrigenRuta.getText().trim();
        String destino = txtDelDestinoRuta.getText().trim();

        if (origen.isEmpty() || destino.isEmpty()) {
            mostrarMensaje("⚠  Escribe origen y destino", false);
            return;
        }

        // SOLO elimina la ruta, las paradas quedan intactas
        boolean ok = adaptador.eliminarRuta(origen, destino);
        if (!ok) {
            mostrarMensaje("⚠  Esa ruta no existe", false);
        } else {
            mostrarMensaje("✓  Ruta eliminada (paradas intactas)", true);
            limpiarCampos(txtDelOrigenRuta, txtDelDestinoRuta);
        }
    }

    @FXML
    private void calcularRuta() {
        String inicio  = txtCalcInicio.getText().trim();
        String fin     = txtCalcFin.getText().trim();
        String criterio = cmbCriterio.getValue();

        if (inicio.isEmpty() || fin.isEmpty()) {
            mostrarMensaje("⚠  Escribe inicio y destino", false);
            return;
        }

        String resultado = adaptador.calcularRuta(inicio, fin, criterio);
        txtResultado.setText(resultado);

        // mostrar el panel del resultado
        panelResultado.setVisible(true);
        panelResultado.setManaged(true);

        // resaltar en el grafo
        List<String> camino = adaptador.getBackend().calcularDijkstra(inicio, fin, criterio);
        if (!camino.isEmpty()) {
            adaptador.getPanelVisual().resaltarRuta(camino);
        }
    }

    @FXML
    private void limpiarTodo() {
        adaptador.limpiarTodo();
        mostrarMensaje("✓  Grafo limpiado", true);
    }

    // ══════════════════════ HELPERS ══════════════════════

    private void esconderTodosLosForms() {
        VBox[] todos = {
                formAgregarParada, formAgregarRuta,
                formModParada, formModRuta,
                formElimParada, formElimRuta,
                formCalcular
        };
        for (VBox f : todos) {
            f.setVisible(false);
            f.setManaged(false);
        }
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    private void mostrarForm(VBox form) {
        esconderTodosLosForms();
        form.setVisible(true);
        form.setManaged(true);
    }

    private void mostrarSubMenu(VBox sub) {
        sub.setVisible(true);
        sub.setManaged(true);
    }

    private void ocultarSubMenu(VBox sub) {
        sub.setVisible(false);
        sub.setManaged(false);
    }

    // muestra el mensaje con un pequeño fade
    private void mostrarMensaje(String texto, boolean esExito) {
        lblMensaje.setText(texto);
        String color = esExito ? "#1a3a1a" : "#3a1a0a";
        lblMensaje.setStyle(
                "-fx-text-fill: #e8c9a8; -fx-background-color: " + color + "; " +
                        "-fx-background-radius: 6; -fx-padding: 8; " +
                        "-fx-font-size: 11; -fx-font-family: 'Segoe UI';"
        );
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);

        // fade out después de 3 segundos
        FadeTransition ft = new FadeTransition(Duration.seconds(3), lblMensaje);
        ft.setDelay(Duration.seconds(2));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            lblMensaje.setVisible(false);
            lblMensaje.setManaged(false);
        });
        ft.play();
    }

    private void limpiarCampos(TextField... campos) {
        for (TextField c : campos) c.clear();
    }
}