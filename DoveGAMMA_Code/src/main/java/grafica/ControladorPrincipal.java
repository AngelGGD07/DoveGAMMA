package grafica;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

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
    @FXML private TableColumn<FilaRuta, String>   colRutaTransbordo;

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
    @FXML private TextField txtTransbordoRuta;

    @FXML private VBox      formModRuta;
    @FXML private TextField txtModOrigenRuta;
    @FXML private TextField txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta;
    @FXML private TextField txtModDistanciaRuta;
    @FXML private TextField txtModCostoRuta;
    @FXML private TextField txtModTransbordoRuta;

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

    /*
       Función: initialize
       Argumentos: ninguno
       Objetivo: Se encarga de inicializar el lienzo
                 del grafo, enlazar las celdas de las tablas y disparar la carga
                 inicial de los datos persistidos en MySQL.
       Retorno: void
    */
    @FXML
    public void initialize() {
        inicializarVisualizacionGrafo();
        configurarTablas();
        cargarDatosDesdeBD();
    }

    /*
       Función: inicializarVisualizacionGrafo
       Argumentos: ninguno
       Objetivo: Obtener la instancia gráfica del grafo desde el
                 AdaptadorVisual y anclarla al contenedor de la interfaz.
       Retorno: void
    */
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

    /*
       Función: configurarTablas
       Argumentos: ninguno
       Objetivo: Enlazar las columnas de las TableView con las propiedades internas de
                 las clases FilaParada y FilaRuta.
       Retorno: void
    */
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

    /*
       Función: agregarParada
       Argumentos: ninguno
       Objetivo: Capturar y agregar los datos ingresados por el usuario.
       Retorno: void
    */
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

    /*
       Función: modificarParada
       Argumentos: ninguno
       Objetivo: Capturar el nuevo nombre ingresado por el usuario y actualizar
       la parada.
       Retorno: void
    */
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

    // =========================================================================================
    // INYECCIÓN DE ESTILO A LAS ALERTAS DE CONFIRMACIÓN
    // =========================================================================================
    private void estilizarAlerta(Alert alerta) {
        // Hacemos que la ventana sea transparente para quitar los bordes blancos del OS
        alerta.initStyle(StageStyle.TRANSPARENT);
        alerta.setGraphic(null); // Quita el icono genérico de pregunta

        DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.getScene().setFill(Color.TRANSPARENT);

        // Estilo principal del fondo y bordes
        dialogPane.setStyle(
                "-fx-background-color: #0c0918; " +
                        "-fx-border-color: #2a1a40; -fx-border-width: 1; " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 24, 0, 0, 6);"
        );

        // Obliga a JavaFX a procesar el CSS interno antes de modificar sus nodos
        dialogPane.applyCss();

        // Títulos
        for (Node node : dialogPane.lookupAll(".label")) {
            node.setStyle("-fx-text-fill: #d4a574; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-font-weight: BOLD;");
        }

        // Subtítulo / Contenido (color un poco más tenue)
        Node contentLabel = dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: #a0a8d8; -fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-font-weight: NORMAL;");
        }

        // Secciones del alert (Para que el color no se corte en los bordes)
        Node header = dialogPane.lookup(".header-panel");
        if (header != null) {
            header.setStyle("-fx-background-color: #120d22; -fx-background-radius: 11 11 0 0;");
        }

        Node buttonBar = dialogPane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setStyle("-fx-background-color: #120d22; -fx-background-radius: 0 0 11 11;");
        }

        // Botón Aceptar / OK
        Node btnOk = dialogPane.lookupButton(ButtonType.OK);
        if (btnOk != null) {
            btnOk.setStyle(
                    "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                            "-fx-padding: 6 18 6 18;"
            );
        }

        // Botón Cancelar
        Node btnCancel = dialogPane.lookupButton(ButtonType.CANCEL);
        if (btnCancel != null) {
            btnCancel.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #7888e8; " +
                            "-fx-border-color: #2a2a50; -fx-border-radius: 6; -fx-border-width: 1; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 18 6 18;"
            );
        }
    }

    /*
       Función: eliminarParadaSeleccionada
       Argumentos: ninguno
       Objetivo: Eliminar una parada y las rutas de esta.
       Retorno: void
    */
    @FXML
    private void eliminarParadaSeleccionada() {
        FilaParada fila = tablaParadas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una parada de la tabla primero."); return; }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar la parada \"" + fila.getNombre() + "\"?");
        confirmacion.setContentText("Se eliminarán también todas las rutas conectadas. No se puede deshacer.");

        estilizarAlerta(confirmacion); // <--- Aplicamos la estética

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
       Argumentos: ninguno
       Objetivo: Capturar los pesos y atributos para la creación de una arista.
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

    @FXML
    /*
       Función: modificarRuta
       Argumentos: ninguno
       Objetivo: Recolectar y añadir los nuevos pesos (tiempo, distancia, costo,
                 transbordos) ingresados en el formulario de edición de rutas.
       Retorno: void
    */
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

    /*
       Función: eliminarRutaSeleccionada
       Argumentos: ninguno
       Objetivo: Obtener la arista (ruta) seleccionada en la tabla y eliminarla.
       Retorno: void
    */
    @FXML
    private void eliminarRutaSeleccionada() {
        FilaRuta fila = tablaRutas.getSelectionModel().getSelectedItem();
        if (fila == null) { mostrarError("Selecciona una ruta de la tabla primero."); return; }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar ruta " + fila.getOrigen() + " → " + fila.getDestino() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        estilizarAlerta(confirmacion); // <--- Aplicamos la estética

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

        estilizarAlerta(confirmacion); // <--- Aplicamos la estética

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

    private void refrescarTablaRutas() {
        listaRutas.clear();
        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        for (String idParada : grafo.obtenerIdsParadas()) {
            for (Ruta ruta : grafo.obtenerVecinos(idParada)) {
                listaRutas.add(new FilaRuta(
                        idParada, ruta.getIdDestino(),
                        ruta.getTiempo(), ruta.getDistancia(), ruta.getCosto(),
                        ruta.getTransbordo()
                ));
            }
        }
    }

    /*
       Función: cargarDatosDesdeBD
       Argumentos: ninguno
       Objetivo: Consultar el gestor de persistencia MySQL e inyectar todas las paradas
                 y rutas guardadas previamente en el grafo visual y en las tablas de
                 la interfaz gráfica.
       Retorno: void
    */
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