package grafica;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import logica.GrafoTransporte;
import logica.CalculadorRuta;

import java.util.List;


public class ControladorPrincipal {

    private static final String STYLE_BUTTON_BASE =
            "-fx-background-color: #2a1a40; " +
                    "-fx-text-fill: #d4a574; " +
                    "-fx-font-size: 13; " +
                    "-fx-font-weight: BOLD; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand;";

    private static final String STYLE_BUTTON_ACTIVE =
            "-fx-background-color: #a65d48; " +
                    "-fx-text-fill: #e8c9a8; " +
                    "-fx-font-size: 13; " +
                    "-fx-font-weight: BOLD; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand;";

    private static final String STYLE_MESSAGE_SUCCESS =
            "-fx-text-fill: #7acc7a; " +
                    "-fx-background-color: #0a2a0a; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 8; " +
                    "-fx-font-size: 11; " +
                    "-fx-font-family: 'Segoe UI';";

    private static final String STYLE_MESSAGE_ERROR =
            "-fx-text-fill: #e07070; " +
                    "-fx-background-color: #2a0a0a; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 8; " +
                    "-fx-font-size: 11; " +
                    "-fx-font-family: 'Segoe UI';";

    private static final String ERROR_ALL_FIELDS_REQUIRED = "Todos los campos son obligatorios.";
    private static final String ERROR_ORIGIN_DESTINATION_SAME = "Origen y destino deben ser diferentes.";
    private static final String ERROR_NUMERIC_FIELDS = "X e Y deben ser numeros.";
    private static final String ERROR_NUMERIC_ROUTE_FIELDS = "Tiempo, distancia y costo deben ser numeros.";
    private static final String ERROR_STOP_NOT_FOUND = "Paradas no encontradas. Agrega las paradas primero.";
    private static final String ERROR_STOP_EXISTS = "Ya existe una parada con ese ID o ese nombre.";
    private static final String ERROR_STOP_NOT_FOUND_OR_NAME_TAKEN = "Parada no encontrada o el nuevo nombre ya existe.";
    private static final String ERROR_ROUTE_NOT_FOUND = "Ruta no encontrada. Verifica IDs y la direccion.";
    private static final String ERROR_ROUTE_NOT_FOUND_DIRECTION = "No existe ruta de %s a %s en esa direccion.";
    private static final String ERROR_STOP_NOT_FOUND_ID = "Parada no encontrada: %s";
    private static final String ERROR_ENTER_STOP_ID = "Escribe el ID de la parada.";
    private static final String ERROR_ENTER_ORIGIN_DESTINATION = "Escribe ID de origen y destino.";
    private static final String ERROR_ENTER_START_END = "Escribe el ID de inicio y fin.";
    private static final String ERROR_START_END_SAME = "Inicio y fin deben ser diferentes.";

    private static final String SUCCESS_STOP_ADDED = "Parada agregada: %s";
    private static final String SUCCESS_ROUTE_CREATED = "Ruta creada: %s -> %s";
    private static final String SUCCESS_STOP_UPDATED = "Parada actualizada: %s";
    private static final String SUCCESS_ROUTE_UPDATED = "Ruta actualizada: %s -> %s";
    private static final String SUCCESS_STOP_DELETED = "Parada eliminada: %s";
    private static final String SUCCESS_ROUTE_DELETED = "Ruta eliminada: %s -> %s";
    private static final String SUCCESS_GRAPH_CLEARED = "Grafo limpiado.";

    private static final String SYMBOL_SUCCESS = "✔";
    private static final String SYMBOL_ERROR = "⚠";
    private static final String ARROW = " -> ";

    private static final String[] ROUTE_CRITERIA = {"tiempo", "distancia", "costo", "transbordos"};

    @FXML private Button btnNavAgregar;
    @FXML private Button btnNavModificar;
    @FXML private Button btnNavEliminar;
    @FXML private Button btnNavCalcular;

    @FXML private VBox subMenuAgregar;
    @FXML private VBox subMenuModificar;
    @FXML private VBox subMenuEliminar;

    @FXML private VBox formAgregarParada;
    @FXML private VBox formAgregarRuta;
    @FXML private VBox formModParada;
    @FXML private VBox formModRuta;
    @FXML private VBox formElimParada;
    @FXML private VBox formElimRuta;
    @FXML private VBox formCalcular;
    @FXML private VBox panelResultado;

    @FXML private TextField txtIdParada;
    @FXML private TextField txtNombreParada;
    @FXML private TextField txtXParada;
    @FXML private TextField txtYParada;

    @FXML private TextField txtOrigenRuta;
    @FXML private TextField txtDestinoRuta;
    @FXML private TextField txtTiempoRuta;
    @FXML private TextField txtDistanciaRuta;
    @FXML private TextField txtCostoRuta;

    @FXML private TextField txtModIdParada;
    @FXML private TextField txtModNombreParada;

    @FXML private TextField txtModOrigenRuta;
    @FXML private TextField txtModDestinoRuta;
    @FXML private TextField txtModTiempoRuta;
    @FXML private TextField txtModDistanciaRuta;
    @FXML private TextField txtModCostoRuta;

    @FXML private TextField txtDelIdParada;

    @FXML private TextField txtDelOrigenRuta;
    @FXML private TextField txtDelDestinoRuta;

    @FXML private TextField txtCalcInicio;
    @FXML private TextField txtCalcFin;
    @FXML private ComboBox<String> cmbCriterio;
    @FXML private TextArea txtResultado;

    @FXML private Label lblMensaje;

    @FXML private StackPane contenedorGrafo;

    private VBox[] allForms;
    private Button[] navigationButtons;


    @FXML
    public void initialize() {
        initializeGraphVisualization();
        initializeFormArray();
        initializeNavigationButtons();
        initializeCriteriaComboBox();
        loadDataFromDatabase();
    }

    private void initializeGraphVisualization() {
        GrafoTransporte graph = new GrafoTransporte();
        PanelVisualizacion visualizationPanel = new PanelVisualizacion();

        AdaptadorVisual.getInstance().setBackend(graph);
        AdaptadorVisual.getInstance().setVisualizationPanel(visualizationPanel);

        contenedorGrafo.getChildren().add(visualizationPanel);
        StackPane.setAlignment(visualizationPanel, javafx.geometry.Pos.TOP_LEFT);

        visualizationPanel.prefWidthProperty().bind(contenedorGrafo.widthProperty());
        visualizationPanel.prefHeightProperty().bind(contenedorGrafo.heightProperty());
    }

    private void initializeFormArray() {
        allForms = new VBox[]{
                formAgregarParada, formAgregarRuta,
                formModParada, formModRuta,
                formElimParada, formElimRuta,
                formCalcular
        };
    }

    private void initializeNavigationButtons() {
        navigationButtons = new Button[]{
                btnNavAgregar, btnNavModificar,
                btnNavEliminar, btnNavCalcular
        };
    }

    private void initializeCriteriaComboBox() {
        cmbCriterio.setItems(FXCollections.observableArrayList(ROUTE_CRITERIA));
        cmbCriterio.getSelectionModel().selectFirst();
    }


    @FXML
    private void mostrarPanelAgregar() {
        toggleSubmenu(subMenuAgregar);
        activateButton(btnNavAgregar);
    }

    @FXML
    private void mostrarPanelModificar() {
        toggleSubmenu(subMenuModificar);
        activateButton(btnNavModificar);
    }

    @FXML
    private void mostrarPanelEliminar() {
        toggleSubmenu(subMenuEliminar);
        activateButton(btnNavEliminar);
    }

    @FXML
    private void mostrarPanelCalcular() {
        closeAllSubmenus();
        activateButton(btnNavCalcular);
        showForm(formCalcular);
    }


    @FXML private void mostrarFormAgregarParada() { showForm(formAgregarParada); }
    @FXML private void mostrarFormAgregarRuta() { showForm(formAgregarRuta); }
    @FXML private void mostrarFormModParada() { showForm(formModParada); }
    @FXML private void mostrarFormModRuta() { showForm(formModRuta); }
    @FXML private void mostrarFormElimParada() { showForm(formElimParada); }
    @FXML private void mostrarFormElimRuta() { showForm(formElimRuta); }


    @FXML
    private void agregarParada() {
        String stopId = txtIdParada.getText().trim();
        String stopName = txtNombreParada.getText().trim();
        String coordinateX = txtXParada.getText().trim();
        String coordinateY = txtYParada.getText().trim();

        if (areAnyFieldsEmpty(stopId, stopName, coordinateX, coordinateY)) {
            showError(ERROR_ALL_FIELDS_REQUIRED);
            return;
        }

        try {
            double x = Double.parseDouble(coordinateX);
            double y = Double.parseDouble(coordinateY);

            boolean isAdded = AdaptadorVisual.getInstance().addStop(stopId, stopName, x, y);

            if (isAdded) {
                clearFields(txtIdParada, txtNombreParada, txtXParada, txtYParada);
                showSuccess(String.format(SUCCESS_STOP_ADDED, stopName));
            } else {
                showError(ERROR_STOP_EXISTS);
            }
        } catch (NumberFormatException exception) {
            showError(ERROR_NUMERIC_FIELDS);
        }
    }

    @FXML
    private void agregarRuta() {
        String originId = txtOrigenRuta.getText().trim();
        String destinationId = txtDestinoRuta.getText().trim();
        String timeValue = txtTiempoRuta.getText().trim();
        String distanceValue = txtDistanciaRuta.getText().trim();
        String costValue = txtCostoRuta.getText().trim();

        if (areAnyFieldsEmpty(originId, destinationId, timeValue, distanceValue, costValue)) {
            showError(ERROR_ALL_FIELDS_REQUIRED);
            return;
        }

        if (originId.equals(destinationId)) {
            showError(ERROR_ORIGIN_DESTINATION_SAME);
            return;
        }

        try {
            double time = Double.parseDouble(timeValue);
            double distance = Double.parseDouble(distanceValue);
            double cost = Double.parseDouble(costValue);

            boolean isAdded = AdaptadorVisual.getInstance().addRoute(
                    originId, destinationId, time, distance, cost);

            if (isAdded) {
                clearFields(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                        txtDistanciaRuta, txtCostoRuta);
                showSuccess(String.format(SUCCESS_ROUTE_CREATED, originId, destinationId));
            } else {
                showError(ERROR_STOP_NOT_FOUND);
            }
        } catch (NumberFormatException exception) {
            showError(ERROR_NUMERIC_ROUTE_FIELDS);
        }
    }


    @FXML
    private void modificarParada() {
        String stopId = txtModIdParada.getText().trim();
        String newName = txtModNombreParada.getText().trim();

        if (areAnyFieldsEmpty(stopId, newName)) {
            showError(ERROR_ALL_FIELDS_REQUIRED);
            return;
        }

        boolean isModified = AdaptadorVisual.getInstance().modifyStopName(stopId, newName);

        if (isModified) {
            clearFields(txtModIdParada, txtModNombreParada);
            showSuccess(String.format(SUCCESS_STOP_UPDATED, stopId));
        } else {
            showError(ERROR_STOP_NOT_FOUND_OR_NAME_TAKEN);
        }
    }

    @FXML
    private void modificarRuta() {
        String originId = txtModOrigenRuta.getText().trim();
        String destinationId = txtModDestinoRuta.getText().trim();
        String timeValue = txtModTiempoRuta.getText().trim();
        String distanceValue = txtModDistanciaRuta.getText().trim();
        String costValue = txtModCostoRuta.getText().trim();

        if (areAnyFieldsEmpty(originId, destinationId, timeValue, distanceValue, costValue)) {
            showError(ERROR_ALL_FIELDS_REQUIRED);
            return;
        }

        try {
            double time = Double.parseDouble(timeValue);
            double distance = Double.parseDouble(distanceValue);
            double cost = Double.parseDouble(costValue);

            boolean isModified = AdaptadorVisual.getInstance().modifyRoute(
                    originId, destinationId, time, distance, cost);

            if (isModified) {
                clearFields(txtModOrigenRuta, txtModDestinoRuta,
                        txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta);
                showSuccess(String.format(SUCCESS_ROUTE_UPDATED, originId, destinationId));
            } else {
                showError(ERROR_ROUTE_NOT_FOUND);
            }
        } catch (NumberFormatException exception) {
            showError(ERROR_NUMERIC_ROUTE_FIELDS);
        }
    }


    @FXML
    private void eliminarParada() {
        String stopId = txtDelIdParada.getText().trim();

        if (stopId.isEmpty()) {
            showError(ERROR_ENTER_STOP_ID);
            return;
        }

        boolean isDeleted = AdaptadorVisual.getInstance().removeStop(stopId);

        if (isDeleted) {
            txtDelIdParada.clear();
            AdaptadorVisual.getInstance().refreshVisualization();
            showSuccess(String.format(SUCCESS_STOP_DELETED, stopId));
        } else {
            showError(String.format(ERROR_STOP_NOT_FOUND_ID, stopId));
        }
    }

    @FXML
    private void eliminarRuta() {
        String originId = txtDelOrigenRuta.getText().trim();
        String destinationId = txtDelDestinoRuta.getText().trim();

        if (areAnyFieldsEmpty(originId, destinationId)) {
            showError(ERROR_ENTER_ORIGIN_DESTINATION);
            return;
        }

        boolean isDeleted = AdaptadorVisual.getInstance().removeRoute(originId, destinationId);

        if (isDeleted) {
            clearFields(txtDelOrigenRuta, txtDelDestinoRuta);
            AdaptadorVisual.getInstance().refreshVisualization();
            showSuccess(String.format(SUCCESS_ROUTE_DELETED, originId, destinationId));
        } else {
            showError(String.format(ERROR_ROUTE_NOT_FOUND_DIRECTION, originId, destinationId));
        }
    }


    @FXML
    private void calcularRuta() {
        String startId = txtCalcInicio.getText().trim();
        String endId = txtCalcFin.getText().trim();
        String criteria = cmbCriterio.getValue();

        if (areAnyFieldsEmpty(startId, endId)) {
            showError(ERROR_ENTER_START_END);
            return;
        }

        if (startId.equals(endId)) {
            showError(ERROR_START_END_SAME);
            return;
        }

        String result = AdaptadorVisual.getInstance().calculateRoute(startId, endId, criteria);
        txtResultado.setText(result);

        highlightRouteOnMap(startId, endId, criteria);
        showResultPanel();
    }

    private void highlightRouteOnMap(String startId, String endId, String criteria) {
        logica.GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        logica.CalculadorRuta calculador = new logica.CalculadorRuta();

        List<String> path = calculador.calcularDijkstra(grafo, startId, endId, criteria);

        if (path != null && !path.isEmpty()) {
            AdaptadorVisual.getInstance().getVisualizationPanel().resaltarRuta(path);
        }
    }

    private void showResultPanel() {
        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
        hideMessage();
    }

    @FXML
    private void limpiarTodo() {
        AdaptadorVisual.getInstance().clearAll();
        txtResultado.clear();
        hideResultPanel();
        showSuccess(SUCCESS_GRAPH_CLEARED);
    }


    private void toggleSubmenu(VBox submenu) {
        boolean isCurrentlyVisible = submenu.isVisible();
        closeAllSubmenus();

        if (!isCurrentlyVisible) {
            submenu.setVisible(true);
            submenu.setManaged(true);
        }
    }

    private void closeAllSubmenus() {
        VBox[] allSubmenus = {subMenuAgregar, subMenuModificar, subMenuEliminar};
        for (VBox submenu : allSubmenus) {
            submenu.setVisible(false);
            submenu.setManaged(false);
        }
    }

    private void showForm(VBox targetForm) {
        hideAllForms();
        targetForm.setVisible(true);
        targetForm.setManaged(true);
        hideMessage();
    }

    private void hideAllForms() {
        for (VBox form : allForms) {
            form.setVisible(false);
            form.setManaged(false);
        }
    }

    private void hideResultPanel() {
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
    }

    private void activateButton(Button activeButton) {
        for (Button button : navigationButtons) {
            String style = (button == activeButton) ? STYLE_BUTTON_ACTIVE : STYLE_BUTTON_BASE;
            button.setStyle(style);
        }
    }


    private void showSuccess(String message) {
        lblMensaje.setText(SYMBOL_SUCCESS + "  " + message);
        lblMensaje.setStyle(STYLE_MESSAGE_SUCCESS);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void showError(String message) {
        lblMensaje.setText(SYMBOL_ERROR + "  " + message);
        lblMensaje.setStyle(STYLE_MESSAGE_ERROR);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void hideMessage() {
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }


    private boolean areAnyFieldsEmpty(String... fieldValues) {
        for (String value : fieldValues) {
            if (value.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }


    private void loadDataFromDatabase() {
        logica.GestorDB database = AdaptadorVisual.getInstance().getDatabaseManager();

        try {
            loadStopsFromDatabase(database);
            loadRoutesFromDatabase(database);
        } catch (java.sql.SQLException exception) {
            System.out.println("Error cargando datos: " + exception.getMessage());
        }
    }

    private void loadStopsFromDatabase(logica.GestorDB database) throws java.sql.SQLException {
        java.sql.ResultSet stopsResult = database.cargarParadas();

        while (stopsResult.next()) {
            AdaptadorVisual.getInstance().addStop(
                    stopsResult.getString("id"),
                    stopsResult.getString("nombre"),
                    stopsResult.getDouble("x"),
                    stopsResult.getDouble("y")
            );
        }
    }

    private void loadRoutesFromDatabase(logica.GestorDB database) throws java.sql.SQLException {
        java.sql.ResultSet routesResult = database.cargarRutas();

        while (routesResult.next()) {
            AdaptadorVisual.getInstance().addRoute(
                    routesResult.getString("origen"),
                    routesResult.getString("destino"),
                    routesResult.getDouble("tiempo"),
                    routesResult.getDouble("distancia"),
                    routesResult.getDouble("costo")
            );
        }
    }
}