package grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;


public class ControladorPrincipal {

    // === Estilos de mensajes ===
    private static final String ESTILO_EXITO =
            "-fx-text-fill: #7acc7a; -fx-background-color: #0a2a0a; -fx-background-radius: 6; " +
                    "-fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';";

    private static final String ESTILO_ERROR =
            "-fx-text-fill: #e07070; -fx-background-color: #2a0a0a; -fx-background-radius: 6; " +
                    "-fx-padding: 8; -fx-font-size: 11; -fx-font-family: 'Segoe UI';";

    private static final String[] CRITERIOS_RUTA = {"tiempo", "distancia", "costo", "transbordos"};

    // === Componentes del TabPane ===
    @FXML private TabPane  tabPrincipal;
    @FXML private Button   btnNavCalcular;

    // === Tabla Paradas ===
    @FXML private TableView<FilaParada>           tablaParadas;
    @FXML private TableColumn<FilaParada, String> colParadaId;
    @FXML private TableColumn<FilaParada, String> colParadaNombre;
    @FXML private TableColumn<FilaParada, String> colParadaX;
    @FXML private TableColumn<FilaParada, String> colParadaY;

    // === Tabla Rutas ===
    @FXML private TableView<FilaRuta>              tablaRutas;
    @FXML private TableColumn<FilaRuta, String>    colRutaOrigen;
    @FXML private TableColumn<FilaRuta, String>    colRutaDestino;
    @FXML private TableColumn<FilaRuta, String>    colRutaTiempo;
    @FXML private TableColumn<FilaRuta, String>    colRutaDistancia;
    @FXML private TableColumn<FilaRuta, String>    colRutaCosto;
    @FXML private TableColumn<FilaRuta, Boolean>   colRutaTransbordo;

    // === Forms de Paradas ===
    @FXML private VBox      formAgregarParada;
    @FXML private TextField txtIdParada;
    @FXML private TextField txtNombreParada;
    @FXML private TextField txtXParada;
    @FXML private TextField txtYParada;

    @FXML private VBox      formModParada;
    @FXML private TextField txtModIdParada;
    @FXML private TextField txtModNombreParada;

    // === Forms de Rutas ===
    @FXML private VBox      formAgregarRuta;
    @FXML private TextField txtOrigenRuta;
    @FXML private TextField txtDestinoRuta;
    @FXML private TextField txtTiempoRuta;
    @FXML private TextField txtDistanciaRuta;
    @FXML private TextField txtCostoRuta;
    @FXML private CheckBox  chkTransbordoRuta;

    @FXML private VBox      formModRuta;
    @FXML private TextField txtModOrigenRuta;
    @FXML private TextField txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta;
    @FXML private TextField txtModDistanciaRuta;
    @FXML private TextField txtModCostoRuta;
    @FXML private CheckBox  chkModTransbordoRuta;

    // === Calcular ===
    @FXML private TextField txtCalcInicio;
    @FXML private TextField txtCalcFin;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private TextArea  txtResultado;
    @FXML private VBox      panelResultado;

    // === Mensaje global ===
    @FXML private Label     lblMensaje;

    // === Grafo ===
    @FXML private StackPane contenedorGrafo;

    // Listas que alimentan las tablas
    private final ObservableList<FilaParada> listaParadas = FXCollections.observableArrayList();
    private final ObservableList<FilaRuta>   listaRutas   = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        inicializarVisualizacionGrafo();
        configurarTablas();
        cmbCriterio.setItems(FXCollections.observableArrayList(CRITERIOS_RUTA));
        cmbCriterio.getSelectionModel().selectFirst();
        cargarDatosDesdeBD();
    }

    // =====================================================
    // SETUP
    // =====================================================

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
        // columnas de paradas
        colParadaId.setCellValueFactory(data -> data.getValue().idProperty());
        colParadaNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colParadaX.setCellValueFactory(data -> data.getValue().xProperty());
        colParadaY.setCellValueFactory(data -> data.getValue().yProperty());
        tablaParadas.setItems(listaParadas);

        // columnas de rutas
        colRutaOrigen.setCellValueFactory(data -> data.getValue().origenProperty());
        colRutaDestino.setCellValueFactory(data -> data.getValue().destinoProperty());
        colRutaTiempo.setCellValueFactory(data -> data.getValue().tiempoProperty());
        colRutaDistancia.setCellValueFactory(data -> data.getValue().distanciaProperty());
        colRutaCosto.setCellValueFactory(data -> data.getValue().costoProperty());

        // columna transbordo con checkbox deshabilitado (solo lectura)
        colRutaTransbordo.setCellValueFactory(data -> data.getValue().transbordoProperty().asObject());
        colRutaTransbordo.setCellFactory(col -> new TableCell<FilaRuta, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setDisable(true);
                cb.setStyle("-fx-opacity: 0.8;");
            }
            @Override
            protected void updateItem(Boolean valor, boolean vacio) {
                super.updateItem(valor, vacio);
                if (vacio || valor == null) {
                    setGraphic(null);
                } else {
                    cb.setSelected(valor);
                    setGraphic(cb);
                }
            }
        });
        tablaRutas.setItems(listaRutas);
    }

    // =====================================================
    // NAVEGACIÓN
    // =====================================================

    @FXML
    private void irACalcular() {
        tabPrincipal.getSelectionModel().select(2); // tab index 2 = Calcular
    }

    // =====================================================
    // TOGGLE FORMULARIOS - Paradas
    // =====================================================

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
        if (fila == null) {
            mostrarError("Selecciona una parada de la tabla primero.");
            return;
        }

        // pre-llenar con los datos de la fila seleccionada
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

    // =====================================================
    // TOGGLE FORMULARIOS - Rutas
    // =====================================================

    @FXML
    private void toggleFormAgregarRuta() {
        formModRuta.setVisible(false);
        formModRuta.setManaged(false);

        boolean mostrar = !formAgregarRuta.isVisible();
        formAgregarRuta.setVisible(mostrar);
        formAgregarRuta.setManaged(mostrar);

        if (!mostrar) limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                txtDistanciaRuta, txtCostoRuta);
        ocultarMensaje();
    }

    @FXML
    private void toggleFormModRuta() {
        FilaRuta fila = tablaRutas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            mostrarError("Selecciona una ruta de la tabla primero.");
            return;
        }

        // pre-llenar con los datos de la fila seleccionada
        txtModOrigenRuta.setText(fila.getOrigen());
        txtModDestinoRuta.setText(fila.getDestino());
        txtModTiempoRuta.setText(fila.getTiempo());
        txtModDistanciaRuta.setText(fila.getDistancia());
        txtModCostoRuta.setText(fila.getCosto());
        chkModTransbordoRuta.setSelected(fila.isTransbordo());

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
                txtDistanciaRuta, txtCostoRuta,
                txtModOrigenRuta, txtModDestinoRuta, txtModTiempoRuta,
                txtModDistanciaRuta, txtModCostoRuta);
        ocultarMensaje();
    }

    // =====================================================
    // CRUD - Paradas
    // =====================================================

    @FXML
    private void agregarParada() {
        String id      = txtIdParada.getText().trim();
        String nombre  = txtNombreParada.getText().trim();
        String valX    = txtXParada.getText().trim();
        String valY    = txtYParada.getText().trim();

        if (camposVacios(id, nombre, valX, valY)) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            double x = Double.parseDouble(valX);
            double y = Double.parseDouble(valY);

            if (x < 0 || y < 0) {
                mostrarError("X e Y deben ser positivos.");
                return;
            }

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
        String id         = txtModIdParada.getText().trim();
        String nuevoNombre = txtModNombreParada.getText().trim();

        if (camposVacios(id, nuevoNombre)) {
            mostrarError("El nombre no puede estar vacío.");
            return;
        }

        if (nuevoNombre.length() < 2) {
            mostrarError("El nombre debe tener al menos 2 caracteres.");
            return;
        }

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
        if (fila == null) {
            mostrarError("Selecciona una parada de la tabla primero.");
            return;
        }

        // Confirmación antes de borrar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar la parada \"" + fila.getNombre() + "\"?");
        confirmacion.setContentText("Se eliminarán también todas las rutas conectadas a esta parada. Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo == ButtonType.OK) {
                boolean ok = AdaptadorVisual.getInstance().eliminarParada(fila.getId());
                if (ok) {
                    refrescarTablaParadas();
                    refrescarTablaRutas(); // las rutas de esa parada desaparecen
                    AdaptadorVisual.getInstance().refrescarVisualizacion();
                    mostrarExito("✔  Parada eliminada: " + fila.getId());
                } else {
                    mostrarError("No se pudo eliminar la parada.");
                }
            }
        });
    }

    // =====================================================
    // CRUD - Rutas
    // =====================================================

    @FXML
    private void agregarRuta() {
        String origen    = txtOrigenRuta.getText().trim();
        String destino   = txtDestinoRuta.getText().trim();
        String valTiempo = txtTiempoRuta.getText().trim();
        String valDist   = txtDistanciaRuta.getText().trim();
        String valCosto  = txtCostoRuta.getText().trim();

        if (camposVacios(origen, destino, valTiempo, valDist, valCosto)) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        if (origen.equals(destino)) {
            mostrarError("Origen y destino no pueden ser iguales.");
            return;
        }

        try {
            double tiempo    = Double.parseDouble(valTiempo);
            double distancia = Double.parseDouble(valDist);
            double costo     = Double.parseDouble(valCosto);

            if (tiempo <= 0 || distancia <= 0 || costo <= 0) {
                mostrarError("Tiempo, distancia y costo deben ser mayores que 0.");
                return;
            }

            boolean transbordo = chkTransbordoRuta.isSelected();
            boolean ok = AdaptadorVisual.getInstance()
                    .agregarRutaConTransbordo(origen, destino, tiempo, distancia, costo, transbordo);

            if (ok) {
                refrescarTablaRutas();
                limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                        txtDistanciaRuta, txtCostoRuta);
                chkTransbordoRuta.setSelected(false);
                formAgregarRuta.setVisible(false);
                formAgregarRuta.setManaged(false);
                mostrarExito("✔  Ruta creada: " + origen + " → " + destino);
            } else {
                mostrarError("Paradas no encontradas. Agrega las paradas primero.");
            }

        } catch (NumberFormatException e) {
            mostrarError("Tiempo, distancia y costo deben ser números válidos.");
        }
    }

    @FXML
    private void modificarRuta() {
        String origen    = txtModOrigenRuta.getText().trim();
        String destino   = txtModDestinoRuta.getText().trim();
        String valTiempo = txtModTiempoRuta.getText().trim();
        String valDist   = txtModDistanciaRuta.getText().trim();
        String valCosto  = txtModCostoRuta.getText().trim();

        if (camposVacios(origen, destino, valTiempo, valDist, valCosto)) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            double tiempo    = Double.parseDouble(valTiempo);
            double distancia = Double.parseDouble(valDist);
            double costo     = Double.parseDouble(valCosto);

            if (tiempo <= 0 || distancia <= 0 || costo <= 0) {
                mostrarError("Los valores deben ser mayores que 0.");
                return;
            }

            boolean transbordo = chkModTransbordoRuta.isSelected();
            boolean ok = AdaptadorVisual.getInstance()
                    .modificarRutaConTransbordo(origen, destino, tiempo, distancia, costo, transbordo);

            if (ok) {
                refrescarTablaRutas();
                formModRuta.setVisible(false);
                formModRuta.setManaged(false);
                limpiarCampos(txtModOrigenRuta, txtModDestinoRuta,
                        txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta);
                mostrarExito("✔  Ruta actualizada: " + origen + " → " + destino);
            } else {
                mostrarError("Ruta no encontrada.");
            }

        } catch (NumberFormatException e) {
            mostrarError("Tiempo, distancia y costo deben ser números válidos.");
        }
    }

    @FXML
    private void eliminarRutaSeleccionada() {
        FilaRuta fila = tablaRutas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            mostrarError("Selecciona una ruta de la tabla primero.");
            return;
        }

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

    // =====================================================
    // CALCULAR RUTA (Dijkstra)
    // =====================================================

    @FXML
    private void calcularRuta() {
        String idInicio = txtCalcInicio.getText().trim();
        String idFin    = txtCalcFin.getText().trim();
        String criterio = cmbCriterio.getValue();

        if (camposVacios(idInicio, idFin)) {
            mostrarError("Escribe el ID de inicio y fin.");
            return;
        }

        if (idInicio.equals(idFin)) {
            mostrarError("Inicio y fin deben ser diferentes.");
            return;
        }

        logica.GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();
        if (!grafo.obtenerIdsParadas().contains(idInicio) || !grafo.obtenerIdsParadas().contains(idFin)) {
            mostrarError("El ID de inicio o fin no existe en el sistema.");
            return;
        }

        String resultado = AdaptadorVisual.getInstance().calcularRuta(idInicio, idFin, criterio);
        txtResultado.setText(resultado);

        // resaltar en el mapa
        logica.CalculadorRuta calc = new logica.CalculadorRuta();
        List<String> ruta = calc.calcularDijkstra(grafo, idInicio, idFin, criterio);
        if (ruta != null && !ruta.isEmpty()) {
            AdaptadorVisual.getInstance().getVisualizationPanel().resaltarRuta(ruta);
        }

        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
        ocultarMensaje();
    }

    // =====================================================
    // LIMPIAR TODO
    // =====================================================

    @FXML
    private void limpiarTodo() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar limpieza");
        confirmacion.setHeaderText("¿Limpiar todo el grafo?");
        confirmacion.setContentText("Se eliminarán todas las paradas y rutas de la sesión actual.");

        confirmacion.showAndWait().ifPresent(tipo -> {
            if (tipo == ButtonType.OK) {
                AdaptadorVisual.getInstance().limpiarTodo();
                inicializarVisualizacionGrafo();
                listaParadas.clear();
                listaRutas.clear();
                txtResultado.clear();
                panelResultado.setVisible(false);
                panelResultado.setManaged(false);
                mostrarExito("✔  Grafo limpiado.");
            }
        });
    }

    // =====================================================
    // HELPERS - tablas
    // =====================================================

    private void refrescarTablaParadas() {
        listaParadas.clear();
        AdaptadorVisual ada = AdaptadorVisual.getInstance();
        for (Map.Entry<String, String> entry : ada.getNombresParadas().entrySet()) {
            String   id     = entry.getKey();
            double[] coords = ada.getCoordenadas(id);
            listaParadas.add(new FilaParada(id, entry.getValue(), coords[0], coords[1]));
        }
    }

    private void refrescarTablaRutas() {
        listaRutas.clear();
        AdaptadorVisual     ada  = AdaptadorVisual.getInstance();
        logica.GrafoTransporte grafo = ada.getBackend();

        for (String idParada : grafo.obtenerIdsParadas()) {
            for (logica.Ruta ruta : grafo.obtenerVecinos(idParada)) {
                String  idArista   = idParada + "-" + ruta.getIdDestino();
                boolean transbordo = ada.tieneTransbordo(idArista);
                listaRutas.add(new FilaRuta(
                        idParada, ruta.getIdDestino(),
                        ruta.getTiempo(), ruta.getDistancia(), ruta.getCosto(),
                        transbordo
                ));
            }
        }
    }

    // =====================================================
    // CARGA DESDE BD
    // =====================================================

    private void cargarDatosDesdeBD() {
        logica.persistencia.GestorDB db = AdaptadorVisual.getInstance().getDatabaseManager();
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
                AdaptadorVisual.getInstance().agregarRuta(
                        rsRutas.getString("origen"),
                        rsRutas.getString("destino"),
                        rsRutas.getDouble("tiempo"),
                        rsRutas.getDouble("distancia"),
                        rsRutas.getDouble("costo")
                );
            }

            refrescarTablaParadas();
            refrescarTablaRutas();

        } catch (java.sql.SQLException e) {
            System.out.println("Error cargando BD: " + e.getMessage());
        }
    }

    // =====================================================
    // HELPERS - UI
    // =====================================================

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
        for (String c : campos) {
            if (c == null || c.isEmpty()) return true;
        }
        return false;
    }

    private void limpiarCampos(TextField... campos) {
        for (TextField c : campos) c.clear();
    }
}