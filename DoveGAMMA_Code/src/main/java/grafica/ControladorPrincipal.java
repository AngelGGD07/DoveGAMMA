package grafica;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class ControladorPrincipal {

    private static final String ESTILO_BOTON_BASE =
            "-fx-background-color: #2a1a40; " +
                    "-fx-text-fill: #d4a574; " +
                    "-fx-font-size: 13; " +
                    "-fx-font-weight: BOLD; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand;";

    private static final String ESTILO_BOTON_ACTIVO =
            "-fx-background-color: #a65d48; " +
                    "-fx-text-fill: #e8c9a8; " +
                    "-fx-font-size: 13; " +
                    "-fx-font-weight: BOLD; " +
                    "-fx-font-family: 'Segoe UI'; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand;";

    private static final String ESTILO_MENSAJE_EXITO =
            "-fx-text-fill: #7acc7a; " +
                    "-fx-background-color: #0a2a0a; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 8; " +
                    "-fx-font-size: 11; " +
                    "-fx-font-family: 'Segoe UI';";

    private static final String ESTILO_MENSAJE_ERROR =
            "-fx-text-fill: #e07070; " +
                    "-fx-background-color: #2a0a0a; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 8; " +
                    "-fx-font-size: 11; " +
                    "-fx-font-family: 'Segoe UI';";

    private static final String ERROR_TODOS_CAMPOS_REQUERIDOS = "Todos los campos son obligatorios.";
    private static final String ERROR_ORIGEN_DESTINO_IGUALES = "Origen y destino deben ser diferentes.";
    private static final String ERROR_CAMPOS_NUMERICOS = "X e Y deben ser números.";
    private static final String ERROR_CAMPOS_RUTA_NUMERICOS = "Tiempo, distancia y costo deben ser números.";
    private static final String ERROR_PARADAS_NO_ENCONTRADAS = "Paradas no encontradas. Agrega las paradas primero.";
    private static final String ERROR_PARADA_EXISTE = "Ya existe una parada con ese ID o ese nombre.";
    private static final String ERROR_PARADA_NO_ENCONTRADA_O_NOMBRE_USADO = "Parada no encontrada o el nuevo nombre ya existe.";
    private static final String ERROR_RUTA_NO_ENCONTRADA = "Ruta no encontrada. Verifica IDs y la dirección.";
    private static final String ERROR_RUTA_NO_EXISTE_DIRECCION = "No existe ruta de %s a %s en esa dirección.";
    private static final String ERROR_ID_PARADA_NO_ENCONTRADO = "Parada no encontrada: %s";
    private static final String ERROR_INGRESAR_ID_PARADA = "Escribe el ID de la parada.";
    private static final String ERROR_INGRESAR_ORIGEN_DESTINO = "Escribe ID de origen y destino.";
    private static final String ERROR_INGRESAR_INICIO_FIN = "Escribe el ID de inicio y fin.";
    private static final String ERROR_INICIO_FIN_IGUALES = "Inicio y fin deben ser diferentes.";

    private static final String EXITO_PARADA_AGREGADA = "Parada agregada: %s";
    private static final String EXITO_RUTA_CREADA = "Ruta creada: %s -> %s";
    private static final String EXITO_PARADA_ACTUALIZADA = "Parada actualizada: %s";
    private static final String EXITO_RUTA_ACTUALIZADA = "Ruta actualizada: %s -> %s";
    private static final String EXITO_PARADA_ELIMINADA = "Parada eliminada: %s";
    private static final String EXITO_RUTA_ELIMINADA = "Ruta eliminada: %s -> %s";
    private static final String EXITO_GRAFO_LIMPIADO = "Grafo limpiado.";

    private static final String SIMBOLO_EXITO = "✔";
    private static final String SIMBOLO_ERROR = "⚠";

    private static final String[] CRITERIOS_RUTA = {"tiempo", "distancia", "costo", "transbordos"};

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

    private VBox[] todosLosFormularios;
    private Button[] botonesNavegacion;

    /*
       Función: initialize
       Argumentos: Ninguno
       Objetivo: Método ejecutado automáticamente por JavaFX al cargar la vista para preparar los componentes y configuraciones iniciales.
       Retorno: (void): No retorna valores.
    */
    @FXML
    public void initialize() {
        inicializarVisualizacionGrafo();
        inicializarArregloFormularios();
        inicializarBotonesNavegacion();
        inicializarComboBoxCriterios();

        txtResultado.setPrefHeight(350);
        txtResultado.setMinHeight(250);

        cargarDatosDesdeBD();
    }

    /*
       Función: inicializarVisualizacionGrafo
       Argumentos: Ninguno
       Objetivo: Preparar e incrustar el panel gráfico de SmartGraph usando el AdaptadorVisual.
       Retorno: (void): Modifica la vista actual incrustando el panel gráfico.
    */
    private void inicializarVisualizacionGrafo() {
        AdaptadorVisual.getInstance().inicializarPanel();
        PanelVisualizacion panelVisualizacion = AdaptadorVisual.getInstance().getVisualizationPanel();

        contenedorGrafo.getChildren().clear();
        contenedorGrafo.getChildren().add(panelVisualizacion);
        StackPane.setAlignment(panelVisualizacion, javafx.geometry.Pos.CENTER);

        panelVisualizacion.prefWidthProperty().bind(contenedorGrafo.widthProperty());
        panelVisualizacion.prefHeightProperty().bind(contenedorGrafo.heightProperty());

        panelVisualizacion.iniciarVisualizacion();
    }

    /*
       Función: inicializarArregloFormularios
       Argumentos: Ninguno
       Objetivo: Agrupar los contenedores VBox en un arreglo para iterar sobre ellos al ocultarlos o mostrarlos.
       Retorno: (void): Inicializa el atributo de clase correspondiente.
    */
    private void inicializarArregloFormularios() {
        todosLosFormularios = new VBox[]{
                formAgregarParada, formAgregarRuta,
                formModParada, formModRuta,
                formElimParada, formElimRuta,
                formCalcular
        };
    }

    /*
       Función: inicializarBotonesNavegacion
       Argumentos: Ninguno
       Objetivo: Agrupar los botones principales del menú lateral para aplicarles estilos de activación dinámicamente.
       Retorno: (void): Inicializa el atributo de clase correspondiente.
    */
    private void inicializarBotonesNavegacion() {
        botonesNavegacion = new Button[]{
                btnNavAgregar, btnNavModificar,
                btnNavEliminar, btnNavCalcular
        };
    }

    /*
       Función: inicializarComboBoxCriterios
       Argumentos: Ninguno
       Objetivo: Cargar las opciones de cálculo de rutas (tiempo, costo, etc.) dentro de la lista desplegable.
       Retorno: (void): Actualiza el componente de la interfaz.
    */
    private void inicializarComboBoxCriterios() {
        cmbCriterio.setItems(FXCollections.observableArrayList(CRITERIOS_RUTA));
        cmbCriterio.getSelectionModel().selectFirst();
    }

    /*
       Función: mostrarPanelAgregar
       Argumentos: Ninguno
       Objetivo: Desplegar las opciones secundarias correspondientes a la acción de agregar y resaltar su botón primario.
       Retorno: (void): Solo cambia la visibilidad en pantalla.
    */
    @FXML
    private void mostrarPanelAgregar() {
        alternarSubmenu(subMenuAgregar);
        activarBoton(btnNavAgregar);
    }

    /*
       Función: mostrarPanelModificar
       Argumentos: Ninguno
       Objetivo: Desplegar las opciones secundarias correspondientes a la modificación y resaltar su botón primario.
       Retorno: (void): Solo cambia la visibilidad en pantalla.
    */
    @FXML
    private void mostrarPanelModificar() {
        alternarSubmenu(subMenuModificar);
        activarBoton(btnNavModificar);
    }

    /*
       Función: mostrarPanelEliminar
       Argumentos: Ninguno
       Objetivo: Desplegar las opciones secundarias correspondientes a la eliminación y resaltar su botón primario.
       Retorno: (void): Solo cambia la visibilidad en pantalla.
    */
    @FXML
    private void mostrarPanelEliminar() {
        alternarSubmenu(subMenuEliminar);
        activarBoton(btnNavEliminar);
    }

    /*
       Función: mostrarPanelCalcular
       Argumentos: Ninguno
       Objetivo: Ocultar submenús, resaltar el botón de cálculo y mostrar directamente el formulario correspondiente.
       Retorno: (void): Actualiza estados visuales de la barra de navegación.
    */
    @FXML
    private void mostrarPanelCalcular() {
        cerrarTodosSubmenus();
        activarBoton(btnNavCalcular);
        mostrarFormulario(formCalcular);
    }

    /*
       Funciones: mostrarFormAgregarParada, mostrarFormAgregarRuta, etc.
       Argumentos: Ninguno
       Objetivo: Llamar al helper para hacer visible exclusivamente el formulario que el usuario seleccionó.
       Retorno: (void): Acciones de la interfaz de usuario.
    */
    @FXML private void mostrarFormAgregarParada() { mostrarFormulario(formAgregarParada); }
    @FXML private void mostrarFormAgregarRuta() { mostrarFormulario(formAgregarRuta); }
    @FXML private void mostrarFormModParada() { mostrarFormulario(formModParada); }
    @FXML private void mostrarFormModRuta() { mostrarFormulario(formModRuta); }
    @FXML private void mostrarFormElimParada() { mostrarFormulario(formElimParada); }
    @FXML private void mostrarFormElimRuta() { mostrarFormulario(formElimRuta); }

    /*
       Función: agregarParada
       Argumentos: Ninguno
       Objetivo: Capturar y validar datos para enviar la petición de creación de un nuevo vértice (Parada) al adaptador.
       Retorno: (void): Acciona inserciones.
    */
    @FXML
    private void agregarParada() {
        String idParada = txtIdParada.getText().trim();
        String nombreParada = txtNombreParada.getText().trim();
        String coordenadaX = txtXParada.getText().trim();
        String coordenadaY = txtYParada.getText().trim();

        if (hayCamposVacios(idParada, nombreParada, coordenadaX, coordenadaY)) {
            mostrarError(ERROR_TODOS_CAMPOS_REQUERIDOS);
            return;
        }

        try {
            double x = Double.parseDouble(coordenadaX);
            double y = Double.parseDouble(coordenadaY);

            boolean fueAgregada = AdaptadorVisual.getInstance().agregarParada(idParada, nombreParada, x, y);

            if (fueAgregada) {
                limpiarCampos(txtIdParada, txtNombreParada, txtXParada, txtYParada);
                mostrarExito(String.format(EXITO_PARADA_AGREGADA, nombreParada));
            } else {
                mostrarError(ERROR_PARADA_EXISTE);
            }
        } catch (NumberFormatException excepcion) {
            mostrarError(ERROR_CAMPOS_NUMERICOS);
        }
    }

    /*
       Función: agregarRuta
       Argumentos: Ninguno
       Objetivo: Capturar y validar datos para crear una nueva arista (Ruta) mediante el adaptador.
       Retorno: (void): Acciona inserciones.
    */
    @FXML
    private void agregarRuta() {
        String idOrigen = txtOrigenRuta.getText().trim();
        String idDestino = txtDestinoRuta.getText().trim();
        String valorTiempo = txtTiempoRuta.getText().trim();
        String valorDistancia = txtDistanciaRuta.getText().trim();
        String valorCosto = txtCostoRuta.getText().trim();

        if (hayCamposVacios(idOrigen, idDestino, valorTiempo, valorDistancia, valorCosto)) {
            mostrarError(ERROR_TODOS_CAMPOS_REQUERIDOS);
            return;
        }

        if (idOrigen.equals(idDestino)) {
            mostrarError(ERROR_ORIGEN_DESTINO_IGUALES);
            return;
        }

        try {
            double tiempo = Double.parseDouble(valorTiempo);
            double distancia = Double.parseDouble(valorDistancia);
            double costo = Double.parseDouble(valorCosto);

            boolean fueAgregada = AdaptadorVisual.getInstance().agregarRuta(
                    idOrigen, idDestino, tiempo, distancia, costo);

            if (fueAgregada) {
                limpiarCampos(txtOrigenRuta, txtDestinoRuta, txtTiempoRuta,
                        txtDistanciaRuta, txtCostoRuta);
                mostrarExito(String.format(EXITO_RUTA_CREADA, idOrigen, idDestino));
            } else {
                mostrarError(ERROR_PARADAS_NO_ENCONTRADAS);
            }
        } catch (NumberFormatException excepcion) {
            mostrarError(ERROR_CAMPOS_RUTA_NUMERICOS);
        }
    }

    /*
       Función: modificarParada
       Argumentos: Ninguno
       Objetivo: Editar atributos legibles (nombre) de una parada específica llamando al backend.
       Retorno: (void): Ejecuta cambios de información.
    */
    @FXML
    private void modificarParada() {
        String idParada = txtModIdParada.getText().trim();
        String nuevoNombre = txtModNombreParada.getText().trim();

        if (hayCamposVacios(idParada, nuevoNombre)) {
            mostrarError(ERROR_TODOS_CAMPOS_REQUERIDOS);
            return;
        }

        boolean fueModificada = AdaptadorVisual.getInstance().modificarNombreParada(idParada, nuevoNombre);

        if (fueModificada) {
            limpiarCampos(txtModIdParada, txtModNombreParada);
            mostrarExito(String.format(EXITO_PARADA_ACTUALIZADA, idParada));
        } else {
            mostrarError(ERROR_PARADA_NO_ENCONTRADA_O_NOMBRE_USADO);
        }
    }

    /*
       Función: modificarRuta
       Argumentos: Ninguno
       Objetivo: Editar los pesos (costo, distancia, tiempo) de una arista y repintar la vista si es necesario.
       Retorno: (void): Ejecuta actualización en base de datos y memoria.
    */
    @FXML
    private void modificarRuta() {
        String idOrigen = txtModOrigenRuta.getText().trim();
        String idDestino = txtModDestinoRuta.getText().trim();
        String valorTiempo = txtModTiempoRuta.getText().trim();
        String valorDistancia = txtModDistanciaRuta.getText().trim();
        String valorCosto = txtModCostoRuta.getText().trim();

        if (hayCamposVacios(idOrigen, idDestino, valorTiempo, valorDistancia, valorCosto)) {
            mostrarError(ERROR_TODOS_CAMPOS_REQUERIDOS);
            return;
        }

        try {
            double tiempo = Double.parseDouble(valorTiempo);
            double distancia = Double.parseDouble(valorDistancia);
            double costo = Double.parseDouble(valorCosto);

            boolean fueModificada = AdaptadorVisual.getInstance().modificarRuta(
                    idOrigen, idDestino, tiempo, distancia, costo);

            if (fueModificada) {
                limpiarCampos(txtModOrigenRuta, txtModDestinoRuta,
                        txtModTiempoRuta, txtModDistanciaRuta, txtModCostoRuta);
                mostrarExito(String.format(EXITO_RUTA_ACTUALIZADA, idOrigen, idDestino));
            } else {
                mostrarError(ERROR_RUTA_NO_ENCONTRADA);
            }
        } catch (NumberFormatException excepcion) {
            mostrarError(ERROR_CAMPOS_RUTA_NUMERICOS);
        }
    }

    /*
       Función: eliminarParada
       Argumentos: Ninguno
       Objetivo: Borrar un vértice completo y todas las aristas conectadas a él en las 3 capas.
       Retorno: (void): Actualiza todo el entorno por eliminación.
    */
    @FXML
    private void eliminarParada() {
        String idParada = txtDelIdParada.getText().trim();

        if (idParada.isEmpty()) {
            mostrarError(ERROR_INGRESAR_ID_PARADA);
            return;
        }

        boolean fueEliminada = AdaptadorVisual.getInstance().eliminarParada(idParada);

        if (fueEliminada) {
            txtDelIdParada.clear();
            AdaptadorVisual.getInstance().refrescarVisualizacion();
            mostrarExito(String.format(EXITO_PARADA_ELIMINADA, idParada));
        } else {
            mostrarError(String.format(ERROR_ID_PARADA_NO_ENCONTRADO, idParada));
        }
    }

    /*
       Función: eliminarRuta
       Argumentos: Ninguno
       Objetivo: Cortar la conexión (arista) entre dos paradas si existe previamente.
       Retorno: (void): Actualiza todo el entorno por eliminación.
    */
    @FXML
    private void eliminarRuta() {
        String idOrigen = txtDelOrigenRuta.getText().trim();
        String idDestino = txtDelDestinoRuta.getText().trim();

        if (hayCamposVacios(idOrigen, idDestino)) {
            mostrarError(ERROR_INGRESAR_ORIGEN_DESTINO);
            return;
        }

        boolean fueEliminada = AdaptadorVisual.getInstance().eliminarRuta(idOrigen, idDestino);

        if (fueEliminada) {
            limpiarCampos(txtDelOrigenRuta, txtDelDestinoRuta);
            AdaptadorVisual.getInstance().refrescarVisualizacion();
            mostrarExito(String.format(EXITO_RUTA_ELIMINADA, idOrigen, idDestino));
        } else {
            mostrarError(String.format(ERROR_RUTA_NO_EXISTE_DIRECCION, idOrigen, idDestino));
        }
    }

    /*
       Función: calcularRuta
       Argumentos: Ninguno
       Objetivo: Obtener nodos origen/destino y criterio, validar que existan, y pedir la ruta más corta para resaltarla visualmente e imprimir detalles.
       Retorno: (void): Acción disparada por botón para cálculo algorítmico.
    */
    @FXML
    private void calcularRuta() {
        String idInicio = txtCalcInicio.getText().trim();
        String idFin = txtCalcFin.getText().trim();
        String criterio = cmbCriterio.getValue();

        if (hayCamposVacios(idInicio, idFin)) {
            mostrarError(ERROR_INGRESAR_INICIO_FIN);
            return;
        }

        if (idInicio.equals(idFin)) {
            mostrarError(ERROR_INICIO_FIN_IGUALES);
            return;
        }

        logica.GrafoTransporte backendActual = AdaptadorVisual.getInstance().getBackend();
        if (!backendActual.obtenerIdsParadas().contains(idInicio) || !backendActual.obtenerIdsParadas().contains(idFin)) {
            mostrarError("Error: El ID de Inicio o Fin no existe en el sistema.");
            return;
        }

        String resultado = AdaptadorVisual.getInstance().calcularRuta(idInicio, idFin, criterio);
        txtResultado.setText(resultado);

        resaltarRutaEnMapa(idInicio, idFin, criterio);
        mostrarPanelResultado();
    }

    /*
       Función: resaltarRutaEnMapa
       Argumentos:
             (String) idInicio: Identificador del nodo de partida.
             (String) idFin: Identificador del nodo de llegada.
             (String) criterio: Métrica bajo la que se resolvió Dijkstra (tiempo, etc).
       Objetivo: Ejecutar el cálculo y delegar a la vista la tarea de iluminar los caminos correspondientes en CSS.
       Retorno: (void): Delega comportamiento visual.
    */
    private void resaltarRutaEnMapa(String idInicio, String idFin, String criterio) {
        logica.GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();
        logica.CalculadorRuta calculador = new logica.CalculadorRuta();

        List<String> ruta = calculador.calcularDijkstra(grafo, idInicio, idFin, criterio);

        if (ruta != null && !ruta.isEmpty()) {
            AdaptadorVisual.getInstance().getVisualizationPanel().resaltarRuta(ruta);
        }
    }

    /*
       Función: mostrarPanelResultado
       Argumentos: Ninguno
       Objetivo: Desplegar el cuadro de texto inferior donde se muestra el resumen del cálculo de ruta.
       Retorno: (void): Afecta visibilidad del VBox.
    */
    private void mostrarPanelResultado() {
        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
        ocultarMensaje();
    }

    /*
       Función: limpiarTodo
       Argumentos: Ninguno
       Objetivo: Destruir el grafo actual de memoria, recrear la vista y limpiar la base de datos (según adaptador).
       Retorno: (void): Botón de reinicio masivo.
    */
    @FXML
    private void limpiarTodo() {
        AdaptadorVisual.getInstance().limpiarTodo();
        inicializarVisualizacionGrafo();
        txtResultado.clear();
        ocultarPanelResultado();
        mostrarExito(EXITO_GRAFO_LIMPIADO);
    }

    /*
       Función: alternarSubmenu
       Argumentos: (VBox) submenu: El contenedor lateral a mostrar/ocultar.
       Objetivo: Proveer un comportamiento de acordeón para las secciones del menú lateral.
       Retorno: (void): Control de visualización.
    */
    private void alternarSubmenu(VBox submenu) {
        boolean esVisibleActualmente = submenu.isVisible();
        cerrarTodosSubmenus();

        if (!esVisibleActualmente) {
            submenu.setVisible(true);
            submenu.setManaged(true);
        }
    }

    /*
       Función: cerrarTodosSubmenus
       Argumentos: Ninguno
       Objetivo: Asegurarse de que ningún acordeón lateral se sobreponga.
       Retorno: (void): Apoya a alternarSubmenu.
    */
    private void cerrarTodosSubmenus() {
        VBox[] todosSubmenus = {subMenuAgregar, subMenuModificar, subMenuEliminar};
        for (VBox submenu : todosSubmenus) {
            submenu.setVisible(false);
            submenu.setManaged(false);
        }
    }

    /*
       Función: mostrarFormulario
       Argumentos: (VBox) formularioObjetivo: El panel central con inputs que debe mostrarse.
       Objetivo: Asegurar que la pantalla central solo muestre las entradas correspondientes a la opción seleccionada.
       Retorno: (void): Control de capas centrales.
    */
    private void mostrarFormulario(VBox formularioObjetivo) {
        ocultarTodosFormularios();
        formularioObjetivo.setVisible(true);
        formularioObjetivo.setManaged(true);
        ocultarMensaje();
    }

    /*
       Función: ocultarTodosFormularios
       Argumentos: Ninguno
       Objetivo: Limpiar la vista central de componentes antes de mostrar uno nuevo.
       Retorno: (void): Apoya a mostrarFormulario.
    */
    private void ocultarTodosFormularios() {
        for (VBox formulario : todosLosFormularios) {
            formulario.setVisible(false);
            formulario.setManaged(false);
        }
    }

    /*
       Función: ocultarPanelResultado
       Argumentos: Ninguno
       Objetivo: Hacer desaparecer el cuadro inferior donde se imprime Dijkstra cuando se navega a otra pantalla.
       Retorno: (void): Control de visibilidad.
    */
    private void ocultarPanelResultado() {
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
    }

    /*
       Función: activarBoton
       Argumentos: (Button) botonActivo: El componente botón clickeado.
       Objetivo: Iterar sobre todos los botones maestros y cambiarles el color de CSS para indicar cuál sección está viva.
       Retorno: (void): Afecta estilos en tiempo real.
    */
    private void activarBoton(Button botonActivo) {
        for (Button boton : botonesNavegacion) {
            String estilo = (boton == botonActivo) ? ESTILO_BOTON_ACTIVO : ESTILO_BOTON_BASE;
            boton.setStyle(estilo);
        }
    }

    /*
       Función: mostrarExito
       Argumentos: (String) mensaje: Texto a inyectar en la etiqueta de alertas.
       Objetivo: Hacer feedback visual de una operación concluida en color verde.
       Retorno: (void): Alteración de Label.
    */
    private void mostrarExito(String mensaje) {
        lblMensaje.setText(SIMBOLO_EXITO + "  " + mensaje);
        lblMensaje.setStyle(ESTILO_MENSAJE_EXITO);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    /*
       Función: mostrarError
       Argumentos: (String) mensaje: Texto descriptivo del problema.
       Objetivo: Hacer feedback visual de una excepción o validación fallida en color rojo.
       Retorno: (void): Alteración de Label.
    */
    private void mostrarError(String mensaje) {
        lblMensaje.setText(SIMBOLO_ERROR + "  " + mensaje);
        lblMensaje.setStyle(ESTILO_MENSAJE_ERROR);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    /*
       Función: ocultarMensaje
       Argumentos: Ninguno
       Objetivo: Borrar de la vista las alertas rojas o verdes una vez se cambia de contexto.
       Retorno: (void): Manipulación de Label.
    */
    private void ocultarMensaje() {
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    /*
       Función: hayCamposVacios
       Argumentos: (String...) valoresCampos: Lista variable de los valores string sacados de los TextFields.
       Objetivo: Evitar crear objetos nulos o incompletos verificando longitud.
       Retorno: (boolean): True si al menos uno está vacío.
    */
    private boolean hayCamposVacios(String... valoresCampos) {
        for (String valor : valoresCampos) {
            if (valor.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /*
       Función: limpiarCampos
       Argumentos: (TextField...) campos: Elementos gráficos a vaciar de texto.
       Objetivo: Restablecer los inputs a blanco después de un éxito en la ejecución o una limpieza.
       Retorno: (void): Vacía propiedades de texto.
    */
    private void limpiarCampos(TextField... campos) {
        for (TextField campo : campos) {
            campo.clear();
        }
    }

    /*
       Función: cargarDatosDesdeBD
       Argumentos: Ninguno
       Objetivo: Al arrancar la aplicación, pedir al adaptador que extraiga toda la información y llene el grafo visual.
       Retorno: (void): Flujo de inicialización de persistencia.
    */
    private void cargarDatosDesdeBD() {
        logica.GestorDB baseDatos = AdaptadorVisual.getInstance().getDatabaseManager();

        try {
            cargarParadasDesdeBD(baseDatos);
            cargarRutasDesdeBD(baseDatos);
        } catch (java.sql.SQLException excepcion) {
            System.out.println("Error cargando datos: " + excepcion.getMessage());
        }
    }

    /*
       Función: cargarParadasDesdeBD
       Argumentos: (logica.GestorDB) baseDatos: Instancia de la conexión.
       Objetivo: Iterar sobre el ResultSet de paradas y agregarlas una a una en el adaptador.
       Retorno: (void): Acción de carga.
    */
    private void cargarParadasDesdeBD(logica.GestorDB baseDatos) throws java.sql.SQLException {
        java.sql.ResultSet resultadoParadas = baseDatos.cargarParadas();

        while (resultadoParadas.next()) {
            AdaptadorVisual.getInstance().agregarParada(
                    resultadoParadas.getString("id"),
                    resultadoParadas.getString("nombre"),
                    resultadoParadas.getDouble("x"),
                    resultadoParadas.getDouble("y")
            );
        }
    }

    /*
       Función: cargarRutasDesdeBD
       Argumentos: (logica.GestorDB) baseDatos: Instancia de la conexión.
       Objetivo: Iterar sobre el ResultSet de rutas y unirlas en el adaptador visual.
       Retorno: (void): Acción de carga.
    */
    private void cargarRutasDesdeBD(logica.GestorDB baseDatos) throws java.sql.SQLException {
        java.sql.ResultSet resultadoRutas = baseDatos.cargarRutas();

        while (resultadoRutas.next()) {
            AdaptadorVisual.getInstance().agregarRuta(
                    resultadoRutas.getString("origen"),
                    resultadoRutas.getString("destino"),
                    resultadoRutas.getDouble("tiempo"),
                    resultadoRutas.getDouble("distancia"),
                    resultadoRutas.getDouble("costo")
            );
        }
    }
}