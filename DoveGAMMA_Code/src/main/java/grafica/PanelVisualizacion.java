package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartStylableNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Ruta;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;
import logica.transporte.Metro;
import logica.transporte.SimuladorCondiciones;
import logica.transporte.Taxi;
import logica.transporte.Teleferico;
import logica.transporte.Tren;
import logica.transporte.VehiculoTransporte;

import java.util.List;

public class PanelVisualizacion extends StackPane {

    private static final String SVG_SOL =
            "M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55z" +
                    "m7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4z" +
                    "M20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6z" +
                    "m-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z";

    private static final String SVG_NUBLADO =
            "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6" +
                    "h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z";

    private static final String SVG_LLUVIA =
            "M17.82 8.04A6.01 6.01 0 0 0 12 4c-2.16 0-4.07 1.14-5.13 2.85A4.98 4.98 0 0 0 1 12c0 2.76 2.24 5 5 5h11" +
                    "c2.21 0 4-1.79 4-4 0-2.04-1.53-3.72-3.5-3.96z";

    private static final String SVG_TORMENTA =
            "M7 2v11h3v9l7-12h-4l4-8z";

    private static final String SVG_TREN =
            "M12 2c-4 0-8 .5-8 4v9.5C4 17.43 5.57 19 7.5 19L6 20.5V21h12v-.5L16.5 19c1.93 0 3.5-1.57 3.5-3.5V6" +
                    "c0-3.5-3.58-4-8-4zm0 14.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM6 10V7h12v3H6z";

    private static final String SVG_METRO =
            "M19 16.5V7c0-3.14-2.79-4.81-7-5-4.2.19-7 1.86-7 5v9.5C5 17.88 5.9 19 7.1 19.5L5.5 21H7l1-1.5h8L17 21" +
                    "h1.5l-1.6-1.5c1.2-.5 2.1-1.62 2.1-3zM12 17c-.83 0-1.5-.67-1.5-1.5S11.17 14 12 14s1.5.67 1.5 1.5" +
                    "S12.83 17 12 17zm5-5H7V7h10v5z";

    private static final String SVG_TELEFERICO =
            "M2 8h9V4h2v4h9v2h-2.06c-.3 1.9-1.31 3.54-2.75 4.68L19 21h-2.12l-1.74-5.83C14.25 15.36 13.14 15.5 12 " +
                    "15.5s-2.25-.14-3.14-.33L7.12 21H5l1.81-6.32C5.37 13.54 4.36 11.9 4.06 10H2V8z";

    private static final String SVG_TAXI =
            "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1" +
                    "c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5" +
                    "S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5" +
                    "-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";

    private static final double ANCHO_MENU   = 500;
    private static final double ANCHO_BODY   = ANCHO_MENU - 40;
    private static final double ANCHO_CARD   = (ANCHO_BODY - 10) / 2.0;
    private static final double ANCHO_RESULT = 460;

    private SmartGraphPanel<String, String> graphView;
    private Digraph<String, String>         grafoBase;

    private String paradaOrigen = null;
    private Label  lblEstado;
    private Button btnVerUltimoResultado;

    private String               ultimoResultado            = null;
    private String               ultimoResultadoAlternativa = null; // guarda el texto de la alternativa
    private boolean              ultimaFueAlternativa       = false; // cuál popup vio el usuario al último
    private String               ultimoOrigen               = null;
    private String               ultimoDestino              = null;
    private String               ultimoCriterio             = null;
    private VehiculoTransporte   ultimoVehiculo             = null;
    private SimuladorCondiciones ultimasCondiciones         = null;

    public PanelVisualizacion(Digraph<String, String> grafoVisual) {
        this.grafoBase = grafoVisual;

        SmartPlacementStrategy strategy   = new SmartCircularSortedPlacementStrategy();
        SmartGraphProperties   properties = new SmartGraphProperties();

        java.net.URI cssUri = null;
        try {
            java.net.URL recursoCss = getClass().getResource("/smartgraph.css");
            if (recursoCss != null) cssUri = recursoCss.toURI();
            else cssUri = new java.io.File("smartgraph.css").toURI();
        } catch (Exception e) {
            cssUri = new java.io.File("smartgraph.css").toURI();
        }

        graphView = new SmartGraphPanel<>(grafoVisual, properties, strategy, cssUri);
        graphView.setVertexLabelProvider(id -> AdaptadorVisual.getInstance().getStopName(id));
        graphView.setEdgeLabelProvider(idArista -> AdaptadorVisual.getInstance().getEdgeDataAsString(idArista));

        graphView.setEdgeDoubleClickAction(edge -> {
            String idArista = edge.getUnderlyingEdge().element();
            String detalles = AdaptadorVisual.getInstance().getDetallesRuta(idArista);
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Detalles del Tramo");
                a.setHeaderText("Conexión: " + idArista);
                a.setContentText(detalles);
                a.showAndWait();
            });
        });

        graphView.setVertexDoubleClickAction(vertex -> {
            String idParada = vertex.getUnderlyingVertex().element();
            Platform.runLater(() -> manejarClickParada(idParada));
        });

        graphView.setAutomaticLayout(true);
        this.getChildren().add(graphView);

        lblEstado = new Label("Doble click en una parada para iniciar la planificación");
        lblEstado.setStyle(estiloLblEstado("#6a4a7a", false));
        StackPane.setAlignment(lblEstado, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblEstado, new Insets(0, 0, 16, 0));
        this.getChildren().add(lblEstado);

        btnVerUltimoResultado = new Button("Ver último resultado");
        btnVerUltimoResultado.setStyle(
                "-fx-background-color: #1e1030; -fx-text-fill: #d4a574; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 11; -fx-padding: 7 16 7 16; " +
                        "-fx-border-color: #3a1a50; -fx-border-radius: 6; -fx-border-width: 1;"
        );
        btnVerUltimoResultado.setVisible(false);

        // abre el último popup que el usuario estaba viendo — principal o alternativa
        btnVerUltimoResultado.setOnAction(e -> {
            if (ultimaFueAlternativa && ultimoResultadoAlternativa != null) {
                mostrarResultadoAlternativa(ultimoResultadoAlternativa, ultimoOrigen, ultimoDestino,
                        ultimoCriterio, ultimoVehiculo, ultimasCondiciones);
            } else if (ultimoResultado != null) {
                mostrarResultado(ultimoResultado, ultimoOrigen, ultimoDestino,
                        ultimoCriterio, ultimoVehiculo, ultimasCondiciones);
            }
        });

        StackPane.setAlignment(btnVerUltimoResultado, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnVerUltimoResultado, new Insets(0, 20, 16, 0));
        this.getChildren().add(btnVerUltimoResultado);
    }

    /*
       Función: manejarClickParada
       Argumentos: (String) idParada: id del nodo que el usuario clickeó en el grafo
       Objetivo: Controlar el flujo de dos clicks — primer click marca el origen,
                 segundo click marca el destino y abre el menú de planificación
       Retorno: void
    */
    private void manejarClickParada(String idParada) {
        if (paradaOrigen == null) {
            paradaOrigen = idParada;
            SmartStylableNode nodo = graphView.getStylableVertex(idParada);
            if (nodo != null) nodo.setStyleClass("vertex-origen");

            String nombre = AdaptadorVisual.getInstance().getStopName(idParada);
            lblEstado.setText("Origen: " + nombre + "   —   Selecciona la parada de destino");
            lblEstado.setStyle(estiloLblEstado("#90d890", true));
        } else {
            if (idParada.equals(paradaOrigen)) {
                cancelarSeleccion();
                return;
            }
            abrirMenuViaje(paradaOrigen, idParada);
        }
    }

    /*
       Función: abrirMenuViaje
       Argumentos: (String) idOrigen: id de la parada de inicio del viaje,
                   (String) idDestino: id de la parada de llegada del viaje
       Objetivo: Construir y mostrar el menú completo de planificación con
                 condiciones actuales, selección de vehículo y criterio de optimización
       Retorno: void
    */
    private void abrirMenuViaje(String idOrigen, String idDestino) {
        String nombreOrigen  = AdaptadorVisual.getInstance().getStopName(idOrigen);
        String nombreDestino = AdaptadorVisual.getInstance().getStopName(idDestino);
        SimuladorCondiciones condiciones = new SimuladorCondiciones();

        Stage menuStage = new Stage();
        menuStage.initStyle(StageStyle.TRANSPARENT);
        menuStage.initModality(Modality.APPLICATION_MODAL);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            menuStage.initOwner(this.getScene().getWindow());
        }

        VBox root = new VBox(0);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(ANCHO_MENU);
        root.setStyle(
                "-fx-background-color: #0c0918; " +
                        "-fx-border-color: #2a1a40; -fx-border-width: 1; " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);"
        );

        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setStyle(
                "-fx-background-color: #120d22; -fx-background-radius: 11 11 0 0; -fx-padding: 18 20 16 20;"
        );
        Label lblTitulo = new Label("Planificación de viaje");
        lblTitulo.setStyle(
                "-fx-text-fill: #d4a574; -fx-font-family: 'Segoe UI'; -fx-font-size: 15; -fx-font-weight: BOLD;"
        );
        Label lblRuta = new Label(nombreOrigen + "   →   " + nombreDestino);
        lblRuta.setStyle("-fx-text-fill: #5a3a7a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        header.getChildren().addAll(lblTitulo, lblRuta);

        VBox body = new VBox(14);
        body.setStyle("-fx-padding: 18 20 20 20;");

        Label lblSecCondiciones = crearLabelSeccion("CONDICIONES ACTUALES");

        HBox rowCondiciones = new HBox(10);
        rowCondiciones.setAlignment(Pos.CENTER_LEFT);

        SimuladorCondiciones.Clima clima = condiciones.getClima();
        VBox cardClima   = crearCardCondicion(svgPathClima(clima), clima.getColorHex(), clima.getDescripcion(), "Clima");
        VBox cardTrafico = crearCardCondicion(null, condiciones.getColorHorario(), condiciones.getDescripcionHorario(), "Tráfico");
        rowCondiciones.getChildren().addAll(cardClima, cardTrafico);

        Label lblSecVehiculo = crearLabelSeccion("MODO DE TRANSPORTE");

        VehiculoTransporte[] opciones   = {new Tren(), new Metro(), new Teleferico(), new Taxi()};
        String[]             svgsV      = {SVG_TREN, SVG_METRO, SVG_TELEFERICO, SVG_TAXI};
        VBox[]               cardsV     = new VBox[4];
        VehiculoTransporte[] seleccionV = {null};

        HBox rowV1 = new HBox(10); rowV1.setAlignment(Pos.CENTER);
        HBox rowV2 = new HBox(10); rowV2.setAlignment(Pos.CENTER);

        Label lblImpacto = new Label("Selecciona un modo de transporte");
        lblImpacto.setWrapText(true);
        lblImpacto.setMaxWidth(ANCHO_BODY);
        lblImpacto.setStyle("-fx-text-fill: #4a3a6a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");

        for (int i = 0; i < 4; i++) {
            final int              idx = i;
            final VehiculoTransporte v = opciones[i];
            VBox card = crearCardVehiculo(v.getNombre(), svgsV[i], v.getColorHex());
            cardsV[i] = card;

            card.setOnMouseClicked(e -> {
                seleccionV[0] = v;
                for (VBox c : cardsV) c.setStyle(estiloCardVehiculo(false, null));
                cardsV[idx].setStyle(estiloCardVehiculo(true, v.getColorHex()));

                String textoImpacto;
                if (condiciones.esTormentaConTeleferico(v)) {
                    textoImpacto = "Servicio suspendido — tormenta activa.";
                    lblImpacto.setStyle("-fx-text-fill: #e07070; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
                } else {
                    double factor = condiciones.getFactorTotal(v);
                    int    pct    = (int) Math.round((factor - 1.0) * 100);
                    textoImpacto  = v.getDescripcionImpacto() +
                            (pct > 0 ? "   (+~" + pct + "% al tiempo estimado)" : "");
                    lblImpacto.setStyle("-fx-text-fill: #6a5a8a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
                }
                lblImpacto.setText(textoImpacto);
            });

            if (i < 2) rowV1.getChildren().add(card);
            else       rowV2.getChildren().add(card);
        }

        Label lblSecCriterio = crearLabelSeccion("CRITERIO DE OPTIMIZACIÓN");

        String[][] cDatos = {
                {"Tiempo",      "TIEMPO",      "#1e2e1e", "#78c878"},
                {"Distancia",   "DISTANCIA",   "#161628", "#7888e8"},
                {"Costo",       "COSTO",       "#2a1e08", "#d4a060"},
                {"Transbordos", "TRANSBORDOS", "#1c1030", "#b080e0"}
        };
        String[] criterioSel = {null};
        Button[] botonesC    = new Button[4];

        HBox rowC1 = new HBox(10); rowC1.setAlignment(Pos.CENTER);
        HBox rowC2 = new HBox(10); rowC2.setAlignment(Pos.CENTER);

        for (int i = 0; i < 4; i++) {
            final int    idx    = i;
            final String crit   = cDatos[i][1];
            final String bgCol  = cDatos[i][2];
            final String txtCol = cDatos[i][3];

            Button btn = crearBotonCriterio(cDatos[i][0], bgCol, txtCol, false);
            botonesC[i] = btn;

            btn.setOnAction(e -> {
                criterioSel[0] = crit;
                for (int j = 0; j < 4; j++) {
                    botonesC[j].setStyle(crearEstiloBotonCriterio(cDatos[j][2], cDatos[j][3], false));
                }
                botonesC[idx].setStyle(crearEstiloBotonCriterio(bgCol, txtCol, true));
            });

            if (i < 2) rowC1.getChildren().add(btn);
            else       rowC2.getChildren().add(btn);
        }

        Label lblError = new Label("");
        lblError.setWrapText(true);
        lblError.setMaxWidth(ANCHO_BODY);
        lblError.setStyle("-fx-text-fill: #e07070; -fx-font-size: 11; -fx-font-family: 'Segoe UI';");

        Button btnCalcular = new Button("Calcular ruta");
        btnCalcular.setMaxWidth(Double.MAX_VALUE);
        btnCalcular.setStyle(
                "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; " +
                        "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13; -fx-pref-height: 40;"
        );

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #5a3a7a; " +
                        "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 11; -fx-pref-height: 32; -fx-border-color: #2a1040; " +
                        "-fx-border-radius: 7; -fx-border-width: 1;"
        );

        btnCalcular.setOnAction(e -> {
            if (seleccionV[0] == null) {
                lblError.setText("Selecciona un modo de transporte antes de continuar.");
                return;
            }
            if (criterioSel[0] == null) {
                lblError.setText("Selecciona un criterio de optimización.");
                return;
            }
            if (condiciones.esTormentaConTeleferico(seleccionV[0])) {
                lblError.setText("El teleférico está suspendido por tormenta. Elige otro transporte.");
                return;
            }
            menuStage.close();
            calcularYMostrar(idOrigen, idDestino, criterioSel[0], seleccionV[0], condiciones);
        });
        btnCancelar.setOnAction(e -> { menuStage.close(); cancelarSeleccion(); });

        body.getChildren().addAll(
                lblSecCondiciones, rowCondiciones,
                separadorMenu(),
                lblSecVehiculo, rowV1, rowV2, lblImpacto,
                separadorMenu(),
                lblSecCriterio, rowC1, rowC2,
                lblError, btnCalcular, btnCancelar
        );

        root.getChildren().addAll(header, body);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        menuStage.setScene(escena);
        menuStage.centerOnScreen();
        menuStage.showAndWait();
    }

    /*
       Función: calcularYMostrar
       Argumentos: (String) idOrigen: parada de inicio del viaje,
                   (String) idDestino: parada de llegada del viaje,
                   (String) criterio: criterio de optimización elegido (TIEMPO, DISTANCIA, COSTO, TRANSBORDOS),
                   (VehiculoTransporte) vehiculo: transporte elegido por el usuario,
                   (SimuladorCondiciones) condiciones: condiciones del momento (clima + hora)
       Objetivo: Calcular la ruta óptima, aplicar los factores de tiempo según vehículo y
                 condiciones, guardar el resultado para poder reabrirlo y mostrarlo en el popup
       Retorno: void
    */
    private void calcularYMostrar(String idOrigen, String idDestino, String criterio,
                                  VehiculoTransporte vehiculo, SimuladorCondiciones condiciones) {
        String resultadoBase = AdaptadorVisual.getInstance().calcularRuta(idOrigen, idDestino, criterio);

        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio);
        CalculadorRuta calc = new CalculadorRuta();
        List<String> camino = calc.calcular(
                AdaptadorVisual.getInstance().getBackend(), idOrigen, idDestino, enumCriterio
        );
        if (camino != null && !camino.isEmpty()) resaltarRuta(camino);

        double tiempoBase     = calcularTiempoTotal(camino);
        double factorTotal    = condiciones.getFactorTotal(vehiculo);
        double tiempoAjustado = tiempoBase * factorTotal;
        int    pct            = (int) Math.round((factorTotal - 1.0) * 100);

        StringBuilder bloque = new StringBuilder();
        bloque.append("\n=== Condiciones del Viaje ===\n");
        bloque.append("Vehículo:  ").append(vehiculo.getNombre()).append("\n");
        bloque.append("Clima:     ").append(condiciones.getClima().getDescripcion()).append("\n");
        bloque.append("Tráfico:   ").append(condiciones.getDescripcionHorario()).append("\n");
        if (pct > 0) {
            bloque.append(String.format("Tiempo base:     %.1f min%n", tiempoBase));
            bloque.append(String.format("Tiempo estimado: %.1f min  (+%d%%)%n", tiempoAjustado, pct));
        } else {
            bloque.append("Sin impacto adicional al tiempo estimado.\n");
        }

        String resultadoFinal = resultadoBase + bloque;

        // guardamos todo pa' poder reabrirlo después
        ultimoResultado        = resultadoFinal;
        ultimoOrigen           = idOrigen;
        ultimoDestino          = idDestino;
        ultimoCriterio         = criterio;
        ultimoVehiculo         = vehiculo;
        ultimasCondiciones     = condiciones;
        ultimaFueAlternativa   = false; // volvemos al estado principal
        btnVerUltimoResultado.setVisible(true);

        cancelarSeleccion();
        mostrarResultado(resultadoFinal, idOrigen, idDestino, criterio, vehiculo, condiciones);
    }

    /*
       Función: calcularTiempoTotal
       Argumentos: (List<String>) camino: lista ordenada de ids de paradas que forman el camino
       Objetivo: Sumar el tiempo de cada tramo del camino para obtener el tiempo base total
                 antes de aplicar factores de vehículo y condiciones
       Retorno: (double): tiempo total en minutos sin ningún factor aplicado
    */
    private double calcularTiempoTotal(List<String> camino) {
        if (camino == null || camino.size() < 2) return 0;
        double total          = 0;
        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();
        for (int i = 0; i < camino.size() - 1; i++) {
            String actual    = camino.get(i);
            String siguiente = camino.get(i + 1);
            for (Ruta r : grafo.obtenerVecinos(actual)) {
                if (r.getIdDestino().equals(siguiente)) { total += r.getTiempo(); break; }
            }
        }
        return total;
    }

    /*
       Función: mostrarResultado
       Argumentos: (String) resultado: texto completo del resultado del cálculo,
                   (String) idOrigen: id de la parada de inicio,
                   (String) idDestino: id de la parada de llegada,
                   (String) criterio: criterio de optimización que se usó,
                   (VehiculoTransporte) vehiculo: transporte que se usó en el cálculo,
                   (SimuladorCondiciones) condiciones: condiciones del momento del cálculo
       Objetivo: Construir y mostrar el popup de resultado con chips de contexto,
                 área de texto y los dos botones — cerrar y ver ruta alternativa
       Retorno: void
    */
    private void mostrarResultado(String resultado, String idOrigen, String idDestino,
                                  String criterio, VehiculoTransporte vehiculo,
                                  SimuladorCondiciones condiciones) {
        Stage resultStage = new Stage();
        resultStage.initStyle(StageStyle.TRANSPARENT);
        resultStage.initModality(Modality.NONE);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            resultStage.initOwner(this.getScene().getWindow());
        }

        VBox root = new VBox(0);
        root.setPrefWidth(ANCHO_RESULT);
        root.setStyle(
                "-fx-background-color: #0c0918; -fx-border-color: #2a1a40; -fx-border-width: 1; " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);"
        );

        VBox resHeader = new VBox(6);
        resHeader.setStyle(
                "-fx-background-color: #120d22; -fx-background-radius: 11 11 0 0; -fx-padding: 14 18 12 18;"
        );
        Label lblResRuta = new Label(
                AdaptadorVisual.getInstance().getStopName(idOrigen) + "   →   " +
                        AdaptadorVisual.getInstance().getStopName(idDestino)
        );
        lblResRuta.setStyle(
                "-fx-text-fill: #d4a574; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-font-size: 13;"
        );

        HBox chipsRow = new HBox(8);
        chipsRow.setAlignment(Pos.CENTER_LEFT);
        chipsRow.getChildren().addAll(
                crearChip(vehiculo.getNombre(),                                      vehiculo.getColorHex()),
                crearChip(condiciones.getClima().getDescripcion(),                   condiciones.getClima().getColorHex()),
                crearChip(condiciones.getDescripcionHorario(),                       condiciones.getColorHorario()),
                crearChip(criterio.charAt(0) + criterio.substring(1).toLowerCase(),  "#4a3a6a")
        );
        resHeader.getChildren().addAll(lblResRuta, chipsRow);

        TextArea txtRes = new TextArea(resultado);
        txtRes.setEditable(false);
        txtRes.setWrapText(true);
        txtRes.setPrefRowCount(13);
        txtRes.setStyle(
                "-fx-control-inner-background: #0c0918; -fx-background-color: #0c0918; " +
                        "-fx-text-fill: #c8b090; -fx-font-family: 'Consolas'; -fx-font-size: 11; " +
                        "-fx-border-color: transparent; -fx-background-radius: 0; -fx-padding: 14 16 14 16;"
        );

        VBox resFooter = new VBox(8);
        resFooter.setStyle(
                "-fx-background-color: #120d22; -fx-background-radius: 0 0 11 11; -fx-padding: 10 16 14 16;"
        );

        Button btnAlternativa = new Button("Ver Ruta Alternativa");
        btnAlternativa.setMaxWidth(Double.MAX_VALUE);
        btnAlternativa.setStyle(
                "-fx-background-color: #161628; -fx-text-fill: #7888e8; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                        "-fx-font-family: 'Segoe UI'; -fx-pref-height: 36; " +
                        "-fx-border-color: #2a2a50; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setStyle(
                "-fx-background-color: #1e1030; -fx-text-fill: #d4a574; -fx-background-radius: 6; " +
                        "-fx-cursor: hand; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; " +
                        "-fx-pref-height: 36; -fx-border-color: #3a1a50; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        btnCerrar.setOnAction(e -> resultStage.close());

        btnAlternativa.setOnAction(e -> {
            resultStage.close();
            calcularYMostrarAlternativa(idOrigen, idDestino, criterio, vehiculo, condiciones);
        });

        resFooter.getChildren().addAll(btnAlternativa, btnCerrar);
        root.getChildren().addAll(resHeader, txtRes, resFooter);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        resultStage.setScene(escena);
        resultStage.centerOnScreen();
        resultStage.show();
    }

    /*
       Función: calcularYMostrarAlternativa
       Argumentos: (String) idOrigen: id de la parada de inicio,
                   (String) idDestino: id de la parada de llegada,
                   (String) criterio: criterio de optimización a usar en el recálculo,
                   (VehiculoTransporte) vehiculo: transporte para aplicar los factores,
                   (SimuladorCondiciones) condiciones: condiciones del momento
       Objetivo: Calcular la ruta alternativa bloqueando temporalmente el primer tramo
                 de la ruta óptima original, resaltarla en el grafo con color diferente
                 y mostrar el resultado en el popup con una etiqueta que la identifica como alternativa
       Retorno: void
    */
    private void calcularYMostrarAlternativa(String idOrigen, String idDestino, String criterio,
                                             VehiculoTransporte vehiculo, SimuladorCondiciones condiciones) {
        CriterioOptimizacion enumCriterio  = CriterioOptimizacion.valueOf(criterio);
        CalculadorRuta       calc          = new CalculadorRuta();
        GrafoTransporte      grafo         = AdaptadorVisual.getInstance().getBackend();

        List<String> caminoAlternativo = calc.calcularRutaAlternativa(grafo, idOrigen, idDestino, enumCriterio);

        if (caminoAlternativo == null || caminoAlternativo.isEmpty()) {
            mostrarResultado(
                    "No existe una ruta alternativa entre estas dos paradas.\n" +
                            "El trayecto posiblemente solo tiene un camino posible.",
                    idOrigen, idDestino, criterio, vehiculo, condiciones
            );
            return;
        }

        resaltarRutaAlternativa(caminoAlternativo);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Ruta Alternativa (").append(criterio).append(") ===\n\n");

        double totalTiempo = 0, totalDist = 0, totalCosto = 0;
        for (int i = 0; i < caminoAlternativo.size(); i++) {
            String actual = caminoAlternativo.get(i);
            sb.append("◉ ").append(AdaptadorVisual.getInstance().getStopName(actual)).append("\n");
            if (i < caminoAlternativo.size() - 1) {
                String siguiente = caminoAlternativo.get(i + 1);
                for (Ruta r : grafo.obtenerVecinos(actual)) {
                    if (r.getIdDestino().equals(siguiente)) {
                        totalTiempo += r.getTiempo();
                        totalDist   += r.getDistancia();
                        totalCosto  += r.getCosto();
                        sb.append("   |  ").append(r.getTiempo()).append(" min, ")
                                .append(r.getDistancia()).append(" km, $")
                                .append(r.getCosto()).append("\n   v\n");
                        break;
                    }
                }
            }
        }

        sb.append("\n=== Resumen del Viaje ===\n");
        sb.append("• Tiempo total: ").append(totalTiempo).append(" min\n");
        sb.append("• Distancia: ").append(totalDist).append(" km\n");
        sb.append("• Costo total: $").append(totalCosto).append("\n");
        sb.append("• Tramos: ").append(caminoAlternativo.size() - 1).append("\n");

        double factorTotal    = condiciones.getFactorTotal(vehiculo);
        double tiempoAjustado = totalTiempo * factorTotal;
        int    pct            = (int) Math.round((factorTotal - 1.0) * 100);

        sb.append("\n=== Condiciones del Viaje ===\n");
        sb.append("Vehículo:  ").append(vehiculo.getNombre()).append("\n");
        sb.append("Clima:     ").append(condiciones.getClima().getDescripcion()).append("\n");
        sb.append("Tráfico:   ").append(condiciones.getDescripcionHorario()).append("\n");
        if (pct > 0) {
            sb.append(String.format("Tiempo base:     %.1f min%n", totalTiempo));
            sb.append(String.format("Tiempo estimado: %.1f min  (+%d%%)%n", tiempoAjustado, pct));
        } else {
            sb.append("Sin impacto adicional al tiempo estimado.\n");
        }

        // guardamos la alternativa y marcamos que fue la última que el user vio
        ultimoResultadoAlternativa = sb.toString();
        ultimaFueAlternativa       = true;

        mostrarResultadoAlternativa(sb.toString(), idOrigen, idDestino, criterio, vehiculo, condiciones);
    }

    /*
       Función: mostrarResultadoAlternativa
       Argumentos: (String) resultado: texto completo de la ruta alternativa calculada,
                   (String) idOrigen: id de la parada de inicio,
                   (String) idDestino: id de la parada de llegada,
                   (String) criterio: criterio que se usó,
                   (VehiculoTransporte) vehiculo: transporte usado,
                   (SimuladorCondiciones) condiciones: condiciones del momento
       Objetivo: Mostrar el popup del resultado de la ruta alternativa con identificación
                 visual diferente (borde azul) y un botón pa' volver a la ruta principal
       Retorno: void
    */
    private void mostrarResultadoAlternativa(String resultado, String idOrigen, String idDestino,
                                             String criterio, VehiculoTransporte vehiculo,
                                             SimuladorCondiciones condiciones) {
        Stage altStage = new Stage();
        altStage.initStyle(StageStyle.TRANSPARENT);
        altStage.initModality(Modality.NONE);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            altStage.initOwner(this.getScene().getWindow());
        }

        VBox root = new VBox(0);
        root.setPrefWidth(ANCHO_RESULT);
        root.setStyle(
                "-fx-background-color: #0c0918; -fx-border-color: #2a2a50; -fx-border-width: 1; " +
                        "-fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);"
        );

        VBox altHeader = new VBox(6);
        altHeader.setStyle(
                "-fx-background-color: #10101e; -fx-background-radius: 11 11 0 0; -fx-padding: 14 18 12 18;"
        );
        Label lblAltTitulo = new Label(
                AdaptadorVisual.getInstance().getStopName(idOrigen) + "   →   " +
                        AdaptadorVisual.getInstance().getStopName(idDestino)
        );
        lblAltTitulo.setStyle(
                "-fx-text-fill: #7888e8; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-font-size: 13;"
        );

        HBox chipsAlt = new HBox(8);
        chipsAlt.setAlignment(Pos.CENTER_LEFT);
        chipsAlt.getChildren().addAll(
                crearChip("Ruta Alternativa",                                       "#7888e8"),
                crearChip(vehiculo.getNombre(),                                      vehiculo.getColorHex()),
                crearChip(condiciones.getClima().getDescripcion(),                   condiciones.getClima().getColorHex()),
                crearChip(criterio.charAt(0) + criterio.substring(1).toLowerCase(),  "#4a3a6a")
        );
        altHeader.getChildren().addAll(lblAltTitulo, chipsAlt);

        TextArea txtAlt = new TextArea(resultado);
        txtAlt.setEditable(false);
        txtAlt.setWrapText(true);
        txtAlt.setPrefRowCount(13);
        txtAlt.setStyle(
                "-fx-control-inner-background: #0c0918; -fx-background-color: #0c0918; " +
                        "-fx-text-fill: #a0a8d8; -fx-font-family: 'Consolas'; -fx-font-size: 11; " +
                        "-fx-border-color: transparent; -fx-background-radius: 0; -fx-padding: 14 16 14 16;"
        );

        VBox altFooter = new VBox(8);
        altFooter.setStyle(
                "-fx-background-color: #10101e; -fx-background-radius: 0 0 11 11; -fx-padding: 10 16 14 16;"
        );

        Button btnVerPrincipal = new Button("Ver Ruta Principal");
        btnVerPrincipal.setMaxWidth(Double.MAX_VALUE);
        btnVerPrincipal.setStyle(
                "-fx-background-color: #1e1a10; -fx-text-fill: #d4a574; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                        "-fx-font-family: 'Segoe UI'; -fx-pref-height: 36; " +
                        "-fx-border-color: #3a2a10; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        Button btnCerrarAlt = new Button("Cerrar");
        btnCerrarAlt.setMaxWidth(Double.MAX_VALUE);
        btnCerrarAlt.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #5a3a7a; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                        "-fx-pref-height: 32; -fx-border-color: #2a1040; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        btnCerrarAlt.setOnAction(e -> altStage.close());

        btnVerPrincipal.setOnAction(e -> {
            altStage.close();
            ultimaFueAlternativa = false; // el usuario regresa a la principal
            calcularYMostrar(idOrigen, idDestino, criterio, vehiculo, condiciones);
        });

        altFooter.getChildren().addAll(btnVerPrincipal, btnCerrarAlt);
        root.getChildren().addAll(altHeader, txtAlt, altFooter);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        altStage.setScene(escena);
        altStage.centerOnScreen();
        altStage.show();
    }

    // =====================================================
    // Helpers de construcción UI
    // =====================================================

    private void cancelarSeleccion() {
        if (paradaOrigen != null) {
            SmartStylableNode nodo = graphView.getStylableVertex(paradaOrigen);
            if (nodo != null) nodo.setStyleClass("vertex");
        }
        paradaOrigen = null;
        lblEstado.setText("Doble click en una parada para iniciar la planificación");
        lblEstado.setStyle(estiloLblEstado("#6a4a7a", false));
    }

    private String estiloLblEstado(String color, boolean negrita) {
        return "-fx-background-color: rgba(12,9,24,0.92); -fx-text-fill: " + color + "; " +
                "-fx-padding: 6 18 6 18; -fx-background-radius: 6; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 11;" +
                (negrita ? "-fx-font-weight: BOLD;" : "");
    }

    private Label crearLabelSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle(
                "-fx-text-fill: #3a2a5a; -fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 9; -fx-font-weight: BOLD;"
        );
        return lbl;
    }

    private Separator separadorMenu() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1e1030;");
        return sep;
    }

    private VBox crearCardCondicion(String svgPath, String colorHex, String valor, String etiqueta) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(ANCHO_CARD);
        card.setStyle(
                "-fx-background-color: #120d22; -fx-border-color: #1e1030; " +
                        "-fx-border-width: 1; -fx-border-radius: 7; -fx-background-radius: 7; " +
                        "-fx-padding: 10 12 10 12;"
        );

        if (svgPath != null) {
            SVGPath icono = new SVGPath();
            icono.setContent(svgPath);
            icono.setFill(Color.web(colorHex));
            icono.setScaleX(0.85);
            icono.setScaleY(0.85);
            card.getChildren().add(icono);
        } else {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 18;");
            card.getChildren().add(dot);
        }

        Label lblValor    = new Label(valor);
        lblValor.setStyle("-fx-text-fill: #d4c0a0; -fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-font-weight: BOLD;");
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle("-fx-text-fill: #3a2a5a; -fx-font-family: 'Segoe UI'; -fx-font-size: 9;");
        card.getChildren().addAll(lblValor, lblEtiqueta);
        return card;
    }

    private VBox crearCardVehiculo(String nombre, String svgPath, String colorHex) {
        SVGPath icono = new SVGPath();
        icono.setContent(svgPath);
        icono.setFill(Color.web(colorHex));
        icono.setScaleX(0.95);
        icono.setScaleY(0.95);

        Label lbl = new Label(nombre);
        lbl.setStyle("-fx-text-fill: #7a6a8a; -fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-font-weight: BOLD;");

        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(ANCHO_CARD);
        card.setPrefHeight(82);
        card.setStyle(estiloCardVehiculo(false, colorHex));
        card.getChildren().addAll(icono, lbl);
        return card;
    }

    private String estiloCardVehiculo(boolean seleccionado, String colorHex) {
        String borde = seleccionado && colorHex != null ? colorHex : "#1e1030";
        String bg    = seleccionado ? "#160e28" : "#120d22";
        String ancho = seleccionado ? "2" : "1";
        return "-fx-background-color: " + bg + "; -fx-border-color: " + borde + "; " +
                "-fx-border-width: " + ancho + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-padding: 12; -fx-cursor: hand;";
    }

    private Button crearBotonCriterio(String texto, String bgColor, String txtColor, boolean activo) {
        Button btn = new Button(texto);
        btn.setStyle(crearEstiloBotonCriterio(bgColor, txtColor, activo));
        return btn;
    }

    private String crearEstiloBotonCriterio(String bgColor, String txtColor, boolean activo) {
        String borde = activo ? txtColor : "#1e1030";
        String ancho = activo ? "2" : "1";
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: " + txtColor + "; " +
                "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12; -fx-pref-width: " + ANCHO_CARD + "; -fx-pref-height: 38; " +
                (activo ? "-fx-font-weight: BOLD; " : "") +
                "-fx-border-color: " + borde + "; -fx-border-radius: 7; -fx-border-width: " + ancho + ";";
    }

    private Label crearChip(String texto, String colorHex) {
        Label chip = new Label(texto);
        chip.setStyle(
                "-fx-background-color: " + colorHex + "22; -fx-text-fill: " + colorHex + "; " +
                        "-fx-background-radius: 4; -fx-padding: 2 8 2 8; " +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 10;"
        );
        return chip;
    }

    private String svgPathClima(SimuladorCondiciones.Clima clima) {
        switch (clima) {
            case SOLEADO:  return SVG_SOL;
            case NUBLADO:  return SVG_NUBLADO;
            case LLUVIOSO: return SVG_LLUVIA;
            case TORMENTA: return SVG_TORMENTA;
            default:       return SVG_NUBLADO;
        }
    }

    // =====================================================
    // Métodos públicos
    // =====================================================

    public void iniciarVisualizacion() {
        Platform.runLater(() -> graphView.init());
    }

    public void actualizarGrafico() {
        Platform.runLater(() -> graphView.update());
    }

    public void fijarCoordenadasNodo(String idNodo, double x, double y) {
        Platform.runLater(() -> {
            SmartStylableNode nodo = graphView.getStylableVertex(idNodo);
            if (nodo instanceof SmartGraphVertex) {
                ((SmartGraphVertex<?>) nodo).setPosition(x, y);
            }
        });
    }

    public void resaltarRuta(List<String> idsParadas) {
        Platform.runLater(() -> {
            limpiarEstilosGrafo();
            if (idsParadas == null || idsParadas.isEmpty()) return;
            for (int i = 0; i < idsParadas.size(); i++) {
                String idActual = idsParadas.get(i);
                SmartStylableNode n = graphView.getStylableVertex(idActual);
                if (n != null) n.setStyleClass("vertex-resaltado");
                if (i < idsParadas.size() - 1) {
                    SmartStylableNode a = graphView.getStylableEdge(idActual + "-" + idsParadas.get(i + 1));
                    if (a != null) a.setStyleClass("edge-resaltado");
                }
            }
        });
    }

    /*
       Función: resaltarRutaAlternativa
       Argumentos: (List<String>) idsParadas: lista de ids de paradas de la ruta alternativa
       Objetivo: Resaltar la ruta alternativa en el grafo con estilo visual diferente
                 al de la ruta principal (azul en vez de naranja/rojo)
       Retorno: void
    */
    public void resaltarRutaAlternativa(List<String> idsParadas) {
        Platform.runLater(() -> {
            limpiarEstilosGrafo();
            if (idsParadas == null || idsParadas.isEmpty()) return;
            for (int i = 0; i < idsParadas.size(); i++) {
                String idActual = idsParadas.get(i);
                SmartStylableNode n = graphView.getStylableVertex(idActual);
                if (n != null) n.setStyleClass("vertex-alternativa");
                if (i < idsParadas.size() - 1) {
                    SmartStylableNode a = graphView.getStylableEdge(idActual + "-" + idsParadas.get(i + 1));
                    if (a != null) a.setStyleClass("edge-alternativa");
                }
            }
        });
    }

    // resetea todos los estilos del grafo a su estado base
    private void limpiarEstilosGrafo() {
        grafoBase.vertices().forEach(v -> {
            SmartStylableNode n = graphView.getStylableVertex(v.element());
            if (n != null) n.setStyleClass("vertex");
        });
        grafoBase.edges().forEach(e -> {
            SmartStylableNode a = graphView.getStylableEdge(e.element());
            if (a != null) a.setStyleClass("edge");
        });
    }

    public void clearAll() {
    }
}