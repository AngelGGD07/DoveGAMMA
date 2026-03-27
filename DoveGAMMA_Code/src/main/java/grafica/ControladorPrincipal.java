package grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import logica.GrafoTransporte;
import logica.Ruta;
import logica.persistencia.GestorDB;

import java.util.Map;

public class ControladorPrincipal {

    private static final String ESTILO_EXITO =
            "-fx-text-fill: #7acc7a; -fx-background-color: #0a2a0a; -fx-background-radius: 6; " +
                    "-fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';";

    private static final String ESTILO_ERROR =
            "-fx-text-fill: #e07070; -fx-background-color: #2a0a0a; -fx-background-radius: 6; " +
                    "-fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';";

    @FXML private TabPane  tabPrincipal;

    @FXML private TableView<FilaParada>           tablaParadas;
    @FXML private TableColumn<FilaParada, String> colParadaId;
    @FXML private TableColumn<FilaParada, String> colParadaNombre;
    @FXML private TableColumn<FilaParada, String> colParadaX;
    @FXML private TableColumn<FilaParada, String> colParadaY;

    @FXML private TableView<FilaRuta>             tablaRutas;
    @FXML private TableColumn<FilaRuta, String>   colRutaOrigen;
    @FXML private TableColumn<FilaRuta, String>   colRutaDestino;
    @FXML private TableColumn<FilaRuta, String>   colRutaTiempo;
    @FXML private TableColumn<FilaRuta, String>   colRutaDistancia;
    @FXML private TableColumn<FilaRuta, String>   colRutaCosto;
    @FXML private TableColumn<FilaRuta, String>   colRutaTransbordo; // ya es String, no Boolean

    @FXML private VBox      formAgregarParada;
    @FXML private TextField txtIdParada;
    @FXML private TextField txtNombreParada;
    @FXML private TextField txtXParada;
    @FXML private TextField txtYParada;

    @FXML private VBox      formModParada;
    @FXML private TextField txtModIdParada;
    @FXML private TextField txtModNombreParada;

    @FXML private VBox      formAgregarRuta;
    @FXML private TextField txtOrigenRuta;
    @FXML private TextField txtDestinoRuta;
    @FXML private TextField txtTiempoRuta;
    @FXML private TextField txtDistanciaRuta;
    @FXML private TextField txtCostoRuta;
    @FXML private TextField txtTransbordoRuta;    // antes era CheckBox

    @FXML private VBox      formModRuta;
    @FXML private TextField txtModOrigenRuta;
    @FXML private TextField txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta;
    @FXML private TextField txtModDistanciaRuta;
    @FXML private TextField txtModCostoRuta;
    @FXML private TextField txtModTransbordoRuta; // antes era CheckBox

    @FXML private Label     lblMensaje;

    @FXML private StackPane contenedorGrafo;

    @FXML private VBox      panelListados;
    @FXML private StackPane panelGrafo;

    @FXML private Button    btnTogglePanel;

    @FXML private VBox      panelDetallesParada;
    @FXML private Label     lblDetalleParadaTitulo;
    @FXML private TextArea  txtDetallesRutasParada;

    private final ObservableList<FilaParada> listaParadas = FXCollections.observableArrayList();
    private final ObservableList<FilaRuta>   listaRutas   = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        inicializarVisualizacionGrafo();
        configurarTablas();
        cargarDatosDesdeBD();
    }

    private void inicializarVisualizacionGrafo() {
        AdaptadorVisual.getInstance().inicializarPanel();
        PanelVisualizacion panel = AdaptadorVisual.getInstance().getVisualizationPanel();

        contenedorGrafo.getChildren().clear();
        contenedorGrafo.getChildren().add(panel);
        javafx.scene.layout.StackPane.setAlignment(panel, javafx.geometry.Pos.CENTER);

        panel.prefWidthProperty().bind(contenedorGrafo.widthProperty());
        panel.prefHeightProperty().bind(contenedorGrafo.heightProperty());
        panel.iniciarVisualizacion();
    }

    private void configurarTablas() {
        colParadaId.setCellValueFactory(data -> data.getValue().idProperty());
        colParadaNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colParadaX.setCellValueFactory(data -> data.getValue().xProperty());
        colParadaY.setCellValueFactory(data -> data.getValue().yProperty());
        tablaParadas.setItems(listaParadas);

        colRutaOrigen.setCellValueFactory(data -> data.getValue().origenProperty());
        colRutaDestino.setCellValueFactory(data -> data.getValue().destinoProperty());
        colRutaTiempo.setCellValueFactory(data -> data.getValue().tiempoProperty());
        colRutaDistancia.setCellValueFactory(data -> data.getValue().distanciaProperty());
        colRutaCosto.setCellValueFactory(data -> data.getValue().costoProperty());
        colRutaTransbordo.setCellValueFactory(data -> data.getValue().transbordoProperty());

        // badge morado si tiene transbordos, texto apagado si es 0
        colRutaTransbordo.setCellFactory(col -> new TableCell<FilaRuta, String>() {
            @Override
            protected void updateItem(String valor, boolean vacio) {
                super.updateItem(valor, vacio);
                if (vacio || valor == null) { setGraphic(null); setText(null); return; }
                int cant = Integer.parseInt(valor);
                if (cant > 0) {
                    Label badge = new Label(valor);
                    badge.setStyle("-fx-background-color: #3a1a50; -fx-text-fill: #b080e0; " +
                            "-fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-font-size: 10; -fx-font-weight: BOLD;");
                    setGraphic(badge);
                    setText(null);
                } else {
                    setText("0");
                    setStyle("-fx-text-fill: #4a3a6a;");
                    setGraphic(null);
                }
            }
        });
        tablaRutas.setItems(listaRutas);

        tablaParadas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lblDetalleParadaTitulo.setText("Datos de la Parada: " + newSel.getNombre());

                StringBuilder detalles = new StringBuilder();
                boolean tieneRutas = false;
                for (Ruta r : AdaptadorVisual.getInstance().getBackend().obtenerVecinos(newSel.getId())) {
                    tieneRutas = true;
                    String nombreDest = AdaptadorVisual.getInstance().getStopName(r.getIdDestino());
                    detalles.append("➡ ").append(nombreDest)
                            .append(" (").append(r.getTiempo()).append("min, $").append(r.getCosto()).append(")\n");
                }
                if (!tieneRutas) detalles.append("No hay salidas desde esta parada.");

                txtDetallesRutasParada.setText(detalles.toString());
                panelDetallesParada.setVisible(true);
                panelDetallesParada.setManaged(true);
            } else {
                panelDetallesParada.setVisible(false);
                panelDetallesParada.setManaged(false);
            }
        });
    }

    @FXML
    private void togglePanelListados() {
        boolean mostrandoListados = panelListados.isVisible();
        panelListados.setVisible(!mostrandoListados);
        panelGrafo.setVisible(mostrandoListados);
        btnTogglePanel.setText(mostrandoListados ? "Mostrar Listados" : "Mostrar Grafo");
    }

    @FXML
    private void toggleFormAgregarParada() {
        formModParada.setVisible(false);
        formModParada.setManaged(false);

        boolean mostrar = !formAgregarParada.isVisible();
        formAgregarParada.setVisible(mostrar);
        formAgregarParada.setManaged(mostrar);

        if (!mostrar) limpiarCampos(txtIdParada, txtNombreParada, txtXParada, txtYParada);
        ocultarMensaje();
    }

    @FXML
    private void toggleFormModParada() {
        FilaParada fila = tablaParadas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una parada de la tabla primero."); return; }

        txtModIdParada.setText(fila.getId());
        txtModNombreParada.setText(fila.getNombre());

        formAgregarParada.setVisible(false);
        formAgregarParada.setManaged(false);

        boolean mostrar = !formModParada.isVisible();
        formModParada.setVisible(mostrar);
        formModParada.setManaged(mostrar);
        ocultarMensaje();
    }

    @FXML
    private void cancelarFormParada() {
        formAgregarParada.setVisible(false);
        formAgregarParada.setManaged(false);
        formModParada.setVisible(false);
        formModParada.setManaged(false);
        limpiarCampos(txtIdParada, txtNombreParada, txtXParada, txtYParada,
                txtModIdParada, txtModNombreParada);
        ocultarMensaje();
    }

    @FXML
    private void toggleFormAgregarRuta() {
        formModRuta.setVisible(false);
        formModRuta.setManaged(false);

        boolean mostrar = !formAgregarRuta.isVisible();
        formAgregarRuta.setVisible(mostrar);
        formAgregarRuta.setManaged(mostrar);

        if (!mostrar) limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                txtDistanciaRuta, txtCostoRuta, txtTransbordoRuta);
        ocultarMensaje();
    }

    @FXML
    private void toggleFormModRuta() {
        FilaRuta fila = tablaRutas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una ruta de la tabla primero."); return; }

        txtModOrigenRuta.setText(fila.getOrigen());
        txtModDestinoRuta.setText(fila.getDestino());
        txtModTiempoRuta.setText(fila.getTiempo());
        txtModDistanciaRuta.setText(fila.getDistancia());
        txtModCostoRuta.setText(fila.getCosto());
        txtModTransbordoRuta.setText(String.valueOf(fila.getTransbordo()));

        formAgregarRuta.setVisible(false);
        formAgregarRuta.setManaged(false);

        boolean mostrar = !formModRuta.isVisible();
        formModRuta.setVisible(mostrar);
        formModRuta.setManaged(mostrar);
        ocultarMensaje();
    }

    @FXML
    private void cancelarFormRuta() {
        formAgregarRuta.setVisible(false);
        formAgregarRuta.setManaged(false);
        formModRuta.setVisible(false);
        formModRuta.setManaged(false);
        limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                txtDistanciaRuta, txtCostoRuta, txtTransbordoRuta,
                txtModOrigenRuta, txtModDestinoRuta, txtModTiempoRuta,
                txtModDistanciaRuta, txtModCostoRuta, txtModTransbordoRuta);
        ocultarMensaje();
    }

    @FXML
    private void agregarParada() {
        String id     = txtIdParada.getText().trim();
        String nombre = txtNombreParada.getText().trim();
        String valX   = txtXParada.getText().trim();
        String valY   = txtYParada.getText().trim();

        if (camposVacios(id, nombre, valX, valY)) { mostrarError("Todos los campos son obligatorios."); return; }

        try {
            double x = Double.parseDouble(valX);
            double y = Double.parseDouble(valY);

            if (x < 0 || y < 0) { mostrarError("X e Y deben ser positivos."); return; }

            boolean ok = AdaptadorVisual.getInstance().agregarParada(id, nombre, x, y);
            if (ok) {
                refrescarTablaParadas();
                limpiarCampos(txtIdParada, txtNombreParada, txtXParada, txtYParada);
                formAgregarParada.setVisible(false);
                formAgregarParada.setManaged(false);
                mostrarExito("✔  Parada agregada: " + nombre);
            } else {
                mostrarError("Ya existe una parada con ese ID.");
            }
        } catch (NumberFormatException e) {
            mostrarError("X e Y deben ser números válidos.");
        }
    }

    @FXML
    private void modificarParada() {
        String id          = txtModIdParada.getText().trim();
        String nuevoNombre = txtModNombreParada.getText().trim();

        if (camposVacios(id, nuevoNombre)) { mostrarError("El nombre no puede estar vacío."); return; }
        if (nuevoNombre.length() < 2)       { mostrarError("El nombre debe tener al menos 2 caracteres."); return; }

        boolean ok = AdaptadorVisual.getInstance().modificarNombreParada(id, nuevoNombre);
        if (ok) {
            refrescarTablaParadas();
            formModParada.setVisible(false);
            formModParada.setManaged(false);
            limpiarCampos(txtModIdParada, txtModNombreParada);
            mostrarExito("✔  Parada actualizada: " + id);
        } else {
            mostrarError("Parada no encontrada.");
        }
    }

    @FXML
    private void eliminarParadaSeleccionada() {
        FilaParada fila = tablaParadas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una parada de la tabla primero."); return; }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar la parada \"" + fila.getNombre() + "\"?");
        confirmacion.setContentText("Se eliminarán también todas las rutas conectadas. No se puede deshacer.");

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo == ButtonType.OK) {
                boolean ok = AdaptadorVisual.getInstance().eliminarParada(fila.getId());
                if (ok) {
                    refrescarTablaParadas();
                    refrescarTablaRutas();
                    AdaptadorVisual.getInstance().refrescarVisualizacion();
                    mostrarExito("✔  Parada eliminada: " + fila.getId());
                } else {
                    mostrarError("No se pudo eliminar la parada.");
                }
            }
        });
    }

    /*
       Función: agregarRuta
       Argumentos: ninguno (lee los TextField del form)
       Objetivo: Validar los campos y agregar una ruta nueva al grafo
       Retorno: void
    */
    @FXML
    private void agregarRuta() {
        String origen    = txtOrigenRuta.getText().trim();
        String destino   = txtDestinoRuta.getText().trim();
        String valTiempo = txtTiempoRuta.getText().trim();
        String valDist   = txtDistanciaRuta.getText().trim();
        String valCosto  = txtCostoRuta.getText().trim();
        String valTrans  = txtTransbordoRuta.getText().trim();

        if (camposVacios(origen, destino, valTiempo, valDist, valCosto)) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }
        if (origen.equals(destino)) { mostrarError("Origen y destino no pueden ser iguales."); return; }

        try {
            double tiempo      = Double.parseDouble(valTiempo);
            double distancia   = Double.parseDouble(valDist);
            double costo       = Double.parseDouble(valCosto);
            int    transbordo  = camposVacios(valTrans) ? 0 : Integer.parseInt(valTrans);

            if (tiempo <= 0 || distancia <= 0 || costo <= 0) {
                mostrarError("Tiempo, distancia y costo deben ser mayores que 0.");
                return;
            }
            if (transbordo < 0) { mostrarError("Los transbordos no pueden ser negativos."); return; }

            boolean ok = AdaptadorVisual.getInstance()
                    .agregarRutaConTransbordo(origen, destino, tiempo, distancia, costo, transbordo);

            if (ok) {
                refrescarTablaRutas();
                limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                        txtDistanciaRuta, txtCostoRuta, txtTransbordoRuta);
                formAgregarRuta.setVisible(false);
                formAgregarRuta.setManaged(false);
                mostrarExito("✔  Ruta creada: " + origen + " → " + destino);
            } else {
                mostrarError("Paradas no encontradas. Agrega las paradas primero.");
            }

        } catch (NumberFormatException e) {
            mostrarError("Verifica que todos los valores numéricos sean válidos.");
        }
    }

    /*
       Función: modificarRuta
       Argumentos: ninguno (lee los TextField del form de modificación)
       Objetivo: Validar y actualizar los datos de una ruta existente
       Retorno: void
    */
    @FXML
    private void modificarRuta() {
        String origen    = txtModOrigenRuta.getText().trim();
        String destino   = txtModDestinoRuta.getText().trim();
        String valTiempo = txtModTiempoRuta.getText().trim();
        String valDist   = txtModDistanciaRuta.getText().trim();
        String valCosto  = txtModCostoRuta.getText().trim();
        String valTrans  = txtModTransbordoRuta.getText().trim();

        if (camposVacios(origen, destino, valTiempo, valDist, valCosto)) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            double tiempo     = Double.parseDouble(valTiempo);
            double distancia  = Double.parseDouble(valDist);
            double costo      = Double.parseDouble(valCosto);
            int    transbordo = camposVacios(valTrans) ? 0 : Integer.parseInt(valTrans);

            if (tiempo <= 0 || distancia <= 0 || costo <= 0) {
                mostrarError("Los valores deben ser mayores que 0.");
                return;
            }
            if (transbordo < 0) { mostrarError("Los transbordos no pueden ser negativos."); return; }

            boolean ok = AdaptadorVisual.getInstance()
                    .modificarRutaConTransbordo(origen, destino, tiempo, distancia, costo, transbordo);

            if (ok) {
                refrescarTablaRutas();
                formModRuta.setVisible(false);
                formModRuta.setManaged(false);
                limpiarCampos(txtModOrigenRuta, txtModDestinoRuta,
                        txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta, txtModTransbordoRuta);
                mostrarExito("✔  Ruta actualizada: " + origen + " → " + destino);
            } else {
                mostrarError("Ruta no encontrada.");
            }

        } catch (NumberFormatException e) {
            mostrarError("Verifica que todos los valores numéricos sean válidos.");
        }
    }

    @FXML
    private void eliminarRutaSeleccionada() {
        FilaRuta fila = tablaRutas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una ruta de la tabla primero."); return; }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar ruta " + fila.getOrigen() + " → " + fila.getDestino() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo == ButtonType.OK) {
                boolean ok = AdaptadorVisual.getInstance()
                        .eliminarRuta(fila.getOrigen(), fila.getDestino());
                if (ok) {
                    refrescarTablaRutas();
                    AdaptadorVisual.getInstance().refrescarVisualizacion();
                    mostrarExito("✔  Ruta eliminada: " + fila.getOrigen() + " → " + fila.getDestino());
                } else {
                    mostrarError("No se pudo eliminar la ruta.");
                }
            }
        });
    }

    @FXML
    private void limpiarTodo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar limpieza");
        confirmacion.setHeaderText("¿Limpiar todo el grafo?");
        confirmacion.setContentText("Se eliminarán todas las paradas y rutas de la sesión.");

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo == ButtonType.OK) {
                AdaptadorVisual.getInstance().limpiarTodo();
                inicializarVisualizacionGrafo();
                listaParadas.clear();
                listaRutas.clear();
                mostrarExito("✔  Grafo limpiado.");
            }
        });
    }

    private void refrescarTablaParadas() {
        listaParadas.clear();
        AdaptadorVisual ada = AdaptadorVisual.getInstance();
        for (Map.Entry<String, String> entry : ada.getNombresParadas().entrySet()) {
            String   id     = entry.getKey();
            double[] coords = ada.getCoordenadas(id);
            listaParadas.add(new FilaParada(id, entry.getValue(), coords[0], coords[1]));
        }
    }

    /*
       Función: refrescarTablaRutas
       Argumentos: ninguno
       Objetivo: Reconstruir la lista de rutas leyendo directo del grafo
       Retorno: void
    */
    private void refrescarTablaRutas() {
        listaRutas.clear();
        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        for (String idParada : grafo.obtenerIdsParadas()) {
            for (Ruta ruta : grafo.obtenerVecinos(idParada)) {
                // transbordo viene directo del objeto Ruta, ya no necesita Set aparte
                listaRutas.add(new FilaRuta(
                        idParada, ruta.getIdDestino(),
                        ruta.getTiempo(), ruta.getDistancia(), ruta.getCosto(),
                        ruta.getTransbordo()
                ));
            }
        }
    }

    private void cargarDatosDesdeBD() {
        GestorDB db = AdaptadorVisual.getInstance().getDatabaseManager();
        try {
            java.sql.ResultSet rsParadas = db.cargarParadas();
            while (rsParadas.next()) {
                AdaptadorVisual.getInstance().agregarParada(
                        rsParadas.getString("id"),
                        rsParadas.getString("nombre"),
                        rsParadas.getDouble("x"),
                        rsParadas.getDouble("y")
                );
            }

            java.sql.ResultSet rsRutas = db.cargarRutas();
            while (rsRutas.next()) {
                // getInt en vez de getBoolean
                AdaptadorVisual.getInstance().agregarRutaConTransbordo(
                        rsRutas.getString("origen"),
                        rsRutas.getString("destino"),
                        rsRutas.getDouble("tiempo"),
                        rsRutas.getDouble("distancia"),
                        rsRutas.getDouble("costo"),
                        rsRutas.getInt("transbordo")
                );
            }

            refrescarTablaParadas();
            refrescarTablaRutas();

        } catch (java.sql.SQLException e) {
            System.out.println("Error cargando BD: " + e.getMessage());
        }
    }

    private void mostrarExito(String msg) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(ESTILO_EXITO);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void mostrarError(String msg) {
        lblMensaje.setText("⚠  " + msg);
        lblMensaje.setStyle(ESTILO_ERROR);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void ocultarMensaje() {
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    private boolean camposVacios(String... campos) {
        for (String c : campos) { if (c == null || c.isEmpty()) return true; }
        return false;
    }

    private void limpiarCampos(TextField... campos) {
        for (TextField c : campos) c.clear();
    }
}