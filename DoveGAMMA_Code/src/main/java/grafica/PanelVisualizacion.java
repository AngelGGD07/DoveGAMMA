package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Ruta;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;
import logica.transporte.*;

import java.util.List;

public class PanelVisualizacion extends StackPane {

    private static final String SVG_SOL       = "M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z";
    private static final String SVG_NUBLADO   = "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z";
    private static final String SVG_LLUVIA    = "M17.82 8.04A6.01 6.01 0 0 0 12 4c-2.16 0-4.07 1.14-5.13 2.85A4.98 4.98 0 0 0 1 12c0 2.76 2.24 5 5 5h11c2.21 0 4-1.79 4-4 0-2.04-1.53-3.72-3.5-3.96z";
    private static final String SVG_TORMENTA  = "M7 2v11h3v9l7-12h-4l4-8z";
    private static final String SVG_TREN      = "M12 2c-4 0-8 .5-8 4v9.5C4 17.43 5.57 19 7.5 19L6 20.5V21h12v-.5L16.5 19c1.93 0 3.5-1.57 3.5-3.5V6c0-3.5-3.58-4-8-4zm0 14.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM6 10V7h12v3H6z";
    private static final String SVG_METRO     = "M19 16.5V7c0-3.14-2.79-4.81-7-5-4.2.19-7 1.86-7 5v9.5C5 17.88 5.9 19 7.1 19.5L5.5 21H7l1-1.5h8L17 21h1.5l-1.6-1.5c1.2-.5 2.1-1.62 2.1-3zM12 17c-.83 0-1.5-.67-1.5-1.5S11.17 14 12 14s1.5.67 1.5 1.5S12.83 17 12 17zm5-5H7V7h10v5z";
    private static final String SVG_TELEFERICO= "M2 8h9V4h2v4h9v2h-2.06c-.3 1.9-1.31 3.54-2.75 4.68L19 21h-2.12l-1.74-5.83C14.25 15.36 13.14 15.5 12 15.5s-2.25-.14-3.14-.33L7.12 21H5l1.81-6.32C5.37 13.54 4.36 11.9 4.06 10H2V8z";
    private static final String SVG_TAXI      = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";

    private static final double ANCHO_MENU   = 500;
    private static final double ANCHO_BODY   = ANCHO_MENU - 40;
    private static final double ANCHO_CARD   = (ANCHO_BODY - 10) / 2.0;
    private static final double ANCHO_RESULT = 490;
    private static final double ANCHO_INFO   = 340;

    private SmartGraphPanel<String, String> graphView;
    private Digraph<String, String>         grafoBase;

    private String paradaOrigen = null;
    private Label  lblEstado;
    private Button btnVerUltimoResultado;

    private String               ultimoOrigen            = null;
    private String               ultimoDestino           = null;
    private String               ultimoCriterio          = null;
    private VehiculoTransporte   ultimoVehiculo          = null;
    private SimuladorCondiciones ultimasCondiciones      = null;
    private List<String>         ultimoCaminoPrincipal   = null;
    private List<String>         ultimoCaminoAlternativo = null;
    private boolean              ultimoViendoAlternativa = false;

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

        // solo el nombre — nada más visible en el grafo por defecto
        graphView.setVertexLabelProvider(id -> AdaptadorVisual.getInstance().getStopName(id));
        graphView.setEdgeLabelProvider(idArista -> "");

        // doble click en arista → popup bonito con los datos de la ruta
        graphView.setEdgeDoubleClickAction(edge -> {
            String idArista = edge.getUnderlyingEdge().element();
            Platform.runLater(() -> mostrarInfoArista(idArista));
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
                        "-fx-border-color: #3a1a50; -fx-border-radius: 6; -fx-border-width: 1;");
        btnVerUltimoResultado.setVisible(false);

        // reabre exactamente lo que el usuario estaba viendo cuando cerró
        btnVerUltimoResultado.setOnAction(e -> {
            if (ultimoViendoAlternativa && ultimoCaminoAlternativo != null) {
                mostrarResultadoCreativo(ultimoCaminoAlternativo, ultimoOrigen, ultimoDestino,
                        ultimoCriterio, ultimoVehiculo, ultimasCondiciones, true);
            } else if (ultimoCaminoPrincipal != null) {
                mostrarResultadoCreativo(ultimoCaminoPrincipal, ultimoOrigen, ultimoDestino,
                        ultimoCriterio, ultimoVehiculo, ultimasCondiciones, false);
            }
        });

        StackPane.setAlignment(btnVerUltimoResultado, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnVerUltimoResultado, new Insets(0, 20, 16, 0));
        this.getChildren().add(btnVerUltimoResultado);
    }

    /*
       Función: mostrarInfoArista
       Argumentos: (String) idArista: "origen-destino" de la ruta clickeada
       Objetivo: Popup estilizado con los 4 atributos de la ruta manteniendo la paleta, CENTRADO
       Retorno: void
    */
    private void mostrarInfoArista(String idArista) {
        String[] partes = idArista.split("-");
        if (partes.length < 2) return;

        Ruta rutaInfo = null;
        for (Ruta r : AdaptadorVisual.getInstance().getBackend().obtenerVecinos(partes[0])) {
            if (r.getIdDestino().equals(partes[1])) { rutaInfo = r; break; }
        }
        if (rutaInfo == null) return;
        final Ruta ruta = rutaInfo;

        Stage s = new Stage();
        s.initStyle(StageStyle.TRANSPARENT);
        s.initModality(Modality.NONE);
        if (this.getScene() != null && this.getScene().getWindow() != null)
            s.initOwner(this.getScene().getWindow());

        VBox root = new VBox(0);
        root.setPrefWidth(ANCHO_INFO);
        root.setStyle("-fx-background-color: #0c0918; -fx-border-color: #ffa50055; " +
                "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);");

        VBox header = new VBox(4);
        header.setStyle("-fx-background-color: #120d22; -fx-background-radius: 11 11 0 0; -fx-padding: 14 18 12 18;");
        Label lblTitulo = new Label("Información de Ruta");
        lblTitulo.setStyle("-fx-text-fill: #d4a574; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-font-size: 13;");
        Label lblConexion = new Label(
                AdaptadorVisual.getInstance().getStopName(partes[0]) +
                        "   →   " +
                        AdaptadorVisual.getInstance().getStopName(partes[1]));
        lblConexion.setStyle("-fx-text-fill: #5a3a7a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        header.getChildren().addAll(lblTitulo, lblConexion);

        VBox body = new VBox(8);
        body.setStyle("-fx-padding: 14 18 14 18;");
        body.getChildren().addAll(
                crearFilaDato("⏱  Tiempo ",     String.format("%.1f min", ruta.getTiempo()),    "#78c878"),
                crearFilaDato("╰┈➤  Distancia ",  String.format("%.1f km",  ruta.getDistancia()), "#7888e8"),
                crearFilaDato("💰  Costo ",       String.format("$%.2f",    ruta.getCosto()),     "#d4a060"),
                crearFilaDato("↔   Transbordos ", String.valueOf(ruta.getTransbordo()),           "#b080e0"));

        VBox footer = new VBox();
        footer.setStyle("-fx-background-color: #120d22; -fx-background-radius: 0 0 11 11; -fx-padding: 10 18 14 18;");
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setStyle("-fx-background-color: #1e1030; -fx-text-fill: #d4a574; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                "-fx-font-family: 'Segoe UI'; -fx-pref-height: 34; " +
                "-fx-border-color: #3a1a50; -fx-border-radius: 6; -fx-border-width: 1;");
        btnCerrar.setOnAction(e -> s.close());
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, body, footer);
        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        s.setScene(escena);
        s.setOnShown(e -> centrarStage(s));
        s.show();
    }

    // fila de dato con etiqueta a la izquierda y valor con el color de la paleta a la derecha
    private HBox crearFilaDato(String etiqueta, String valor, String colorHex) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setStyle("-fx-background-color: " + colorHex + "12; " +
                "-fx-border-color: " + colorHex + "30; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12 8 12;");

        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle("-fx-text-fill: #9a8a7a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        HBox.setHgrow(lblEtiqueta, Priority.ALWAYS);

        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: BOLD; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 12;");

        fila.getChildren().addAll(lblEtiqueta, lblValor);
        return fila;
    }

    /*
       Función: centrarStage
       Argumentos: (Stage) stage: el popup a posicionar
       Objetivo: Centrar en la ventana dueña usando sizeToScene() antes de calcular la posición;
                 si no hay dueña, cae en centerOnScreen()
       Retorno: void
    */
    private void centrarStage(Stage stage) {
        stage.sizeToScene();
        Window owner = stage.getOwner();
        if (owner != null) {
            double cx = owner.getX() + owner.getWidth()  / 2;
            double cy = owner.getY() + owner.getHeight() / 2;
            stage.setX(cx - stage.getWidth()  / 2);
            stage.setY(cy - stage.getHeight() / 2);
        } else {
            stage.centerOnScreen();
        }
    }

    /*
       Función: manejarClickParada
       Argumentos: (String) idParada: id del nodo clickeado
       Objetivo: Primer click = marcar origen, segundo click = abrir menú de viaje
       Retorno: void
    */
    private void manejarClickParada(String idParada) {
        if (paradaOrigen == null) {
            paradaOrigen = idParada;
            SmartStylableNode nodo = graphView.getStylableVertex(idParada);
            if (nodo != null) nodo.setStyleClass("vertex-origen");
            lblEstado.setText("Origen: " + AdaptadorVisual.getInstance().getStopName(idParada)
                    + "   —   Selecciona la parada de destino");
            lblEstado.setStyle(estiloLblEstado("#90d890", true));
        } else {
            if (idParada.equals(paradaOrigen)) { cancelarSeleccion(); return; }
            abrirMenuViaje(paradaOrigen, idParada);
        }
    }

    /*
       Función: abrirMenuViaje
       Argumentos: (String) idOrigen: parada de inicio,
                   (String) idDestino: parada de llegada
       Objetivo: Menú de planificación con condiciones, vehículo y criterio — CENTRADO en la ventana
       Retorno: void
    */
    private void abrirMenuViaje(String idOrigen, String idDestino) {
        String nombreOrigen  = AdaptadorVisual.getInstance().getStopName(idOrigen);
        String nombreDestino = AdaptadorVisual.getInstance().getStopName(idDestino);
        SimuladorCondiciones condiciones = new SimuladorCondiciones();

        Stage menuStage = new Stage();
        menuStage.initStyle(StageStyle.TRANSPARENT);
        menuStage.initModality(Modality.APPLICATION_MODAL);
        if (this.getScene() != null && this.getScene().getWindow() != null)
            menuStage.initOwner(this.getScene().getWindow());

        VBox root = new VBox(0);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(ANCHO_MENU);
        root.setStyle("-fx-background-color: #0c0918; -fx-border-color: #2a1a40; -fx-border-width: 1; " +
                "-fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);");

        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #120d22; -fx-background-radius: 11 11 0 0; -fx-padding: 18 20 16 20;");
        Label lblTitulo = new Label("Planificación de viaje");
        lblTitulo.setStyle("-fx-text-fill: #d4a574; -fx-font-family: 'Segoe UI'; -fx-font-size: 15; -fx-font-weight: BOLD;");
        Label lblRuta = new Label(nombreOrigen + "   →   " + nombreDestino);
        lblRuta.setStyle("-fx-text-fill: #5a3a7a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
        header.getChildren().addAll(lblTitulo, lblRuta);

        VBox body = new VBox(14);
        body.setStyle("-fx-padding: 18 20 20 20;");

        Label lblSecCondiciones = crearLabelSeccion("CONDICIONES ACTUALES");
        HBox rowCondiciones = new HBox(10);
        rowCondiciones.setAlignment(Pos.CENTER_LEFT);
        SimuladorCondiciones.Clima clima = condiciones.getClima();
        rowCondiciones.getChildren().addAll(
                crearCardCondicion(svgPathClima(clima), clima.getColorHex(), clima.getDescripcion(), "Clima"),
                crearCardCondicion(null, condiciones.getColorHorario(), condiciones.getDescripcionHorario(), "Tráfico"));

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
            final int idx = i;
            final VehiculoTransporte v = opciones[i];
            VBox card = crearCardVehiculo(v.getNombre(), svgsV[i], v.getColorHex());
            cardsV[i] = card;
            card.setOnMouseClicked(e -> {
                seleccionV[0] = v;
                for (VBox c : cardsV) c.setStyle(estiloCardVehiculo(false, null));
                cardsV[idx].setStyle(estiloCardVehiculo(true, v.getColorHex()));
                if (condiciones.esTormentaConTeleferico(v)) {
                    lblImpacto.setText("Servicio suspendido — tormenta activa.");
                    lblImpacto.setStyle("-fx-text-fill: #e07070; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
                } else {
                    double factor = condiciones.getFactorTotal(v);
                    int pct = (int) Math.round((factor - 1.0) * 100);
                    lblImpacto.setText(v.getDescripcionImpacto() + (pct > 0 ? "   (+~" + pct + "% al tiempo)" : ""));
                    lblImpacto.setStyle("-fx-text-fill: #6a5a8a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");
                }
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
            final int idx    = i;
            final String crit   = cDatos[i][1];
            final String bgCol  = cDatos[i][2];
            final String txtCol = cDatos[i][3];
            Button btn = crearBotonCriterio(cDatos[i][0], bgCol, txtCol, false);
            botonesC[i] = btn;
            btn.setOnAction(e -> {
                criterioSel[0] = crit;
                for (int j = 0; j < 4; j++)
                    botonesC[j].setStyle(crearEstiloBotonCriterio(cDatos[j][2], cDatos[j][3], false));
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
        btnCalcular.setStyle("-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; " +
                "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 13; -fx-pref-height: 40;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setStyle("-fx-background-color: transparent; -fx-text-fill: #5a3a7a; " +
                "-fx-background-radius: 7; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 11; -fx-pref-height: 32; -fx-border-color: #2a1040; " +
                "-fx-border-radius: 7; -fx-border-width: 1;");

        btnCalcular.setOnAction(e -> {
            if (seleccionV[0] == null)                              { lblError.setText("Selecciona un modo de transporte."); return; }
            if (criterioSel[0] == null)                             { lblError.setText("Selecciona un criterio de optimización."); return; }
            if (condiciones.esTormentaConTeleferico(seleccionV[0])) { lblError.setText("El teleférico está suspendido por tormenta."); return; }
            menuStage.close();
            calcularYMostrar(idOrigen, idDestino, criterioSel[0], seleccionV[0], condiciones);
        });
        btnCancelar.setOnAction(e -> { menuStage.close(); cancelarSeleccion(); });

        body.getChildren().addAll(
                lblSecCondiciones, rowCondiciones, separadorMenu(),
                lblSecVehiculo, rowV1, rowV2, lblImpacto, separadorMenu(),
                lblSecCriterio, rowC1, rowC2, lblError, btnCalcular, btnCancelar);

        root.getChildren().addAll(header, body);
        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        menuStage.setScene(escena);
        menuStage.setOnShown(e -> centrarStage(menuStage));
        menuStage.showAndWait();
    }

    /*
       Función: calcularYMostrar
       Argumentos: (String) idOrigen, (String) idDestino, (String) criterio,
                   (VehiculoTransporte) vehiculo, (SimuladorCondiciones) condiciones
       Objetivo: Calcular ruta óptima, guardar estado y abrir el popup visual CENTRADO
       Retorno: void
    */
    private void calcularYMostrar(String idOrigen, String idDestino, String criterio,
                                  VehiculoTransporte vehiculo, SimuladorCondiciones condiciones) {
        AdaptadorVisual.getInstance().calcularRuta(idOrigen, idDestino, criterio);

        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio);
        CalculadorRuta calc = new CalculadorRuta();
        List<String> camino = calc.calcular(
                AdaptadorVisual.getInstance().getBackend(), idOrigen, idDestino, enumCriterio);

        if (camino != null && !camino.isEmpty()) resaltarRuta(camino);

        ultimoOrigen            = idOrigen;
        ultimoDestino           = idDestino;
        ultimoCriterio          = criterio;
        ultimoVehiculo          = vehiculo;
        ultimasCondiciones      = condiciones;
        ultimoCaminoPrincipal   = camino;
        ultimoCaminoAlternativo = null;
        ultimoViendoAlternativa = false;
        btnVerUltimoResultado.setVisible(true);

        cancelarSeleccion();
        mostrarResultadoCreativo(camino, idOrigen, idDestino, criterio, vehiculo, condiciones, false);
    }

    /*
       Función: calcularYMostrarAlternativa
       Argumentos: (String) idOrigen, (String) idDestino, (String) criterio,
                   (VehiculoTransporte) vehiculo, (SimuladorCondiciones) condiciones
       Objetivo: Calcular y mostrar ruta alternativa en azul CENTRADO
       Retorno: void
    */
    private void calcularYMostrarAlternativa(String idOrigen, String idDestino, String criterio,
                                             VehiculoTransporte vehiculo, SimuladorCondiciones condiciones) {
        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio);
        CalculadorRuta calc   = new CalculadorRuta();
        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        List<String> caminoAlt = calc.calcularRutaAlternativa(grafo, idOrigen, idDestino, enumCriterio);
        if (caminoAlt != null && !caminoAlt.isEmpty()) resaltarRutaAlternativa(caminoAlt);

        ultimoCaminoAlternativo = caminoAlt;
        ultimoViendoAlternativa = true;

        mostrarResultadoCreativo(caminoAlt, idOrigen, idDestino, criterio, vehiculo, condiciones, true);
    }

    /*
       Función: mostrarResultadoCreativo
       Argumentos: (List<String>) camino, (String) idOrigen, (String) idDestino,
                   (String) criterio, (VehiculoTransporte) vehiculo,
                   (SimuladorCondiciones) condiciones, (boolean) esAlternativa
       Objetivo: Popup visual con animación + tarjetas de resumen CENTRADO en la ventana principal
       Retorno: void
    */
    private void mostrarResultadoCreativo(List<String> camino, String idOrigen, String idDestino,
                                          String criterio, VehiculoTransporte vehiculo,
                                          SimuladorCondiciones condiciones, boolean esAlternativa) {
        Stage s = new Stage();
        s.initStyle(StageStyle.TRANSPARENT);
        s.initModality(Modality.NONE);
        if (this.getScene() != null && this.getScene().getWindow() != null)
            s.initOwner(this.getScene().getWindow());

        String colorBorde  = esAlternativa ? "#2a2a50" : "#2a1a40";
        String colorHeader = esAlternativa ? "#10101e" : "#120d22";
        String colorTitulo = esAlternativa ? "#7888e8" : "#d4a574";

        VBox root = new VBox(0);
        root.setPrefWidth(ANCHO_RESULT);
        root.setStyle("-fx-background-color: #0c0918; -fx-border-color: " + colorBorde + "; " +
                "-fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.95), 32, 0, 0, 8);");

        VBox header = new VBox(6);
        header.setStyle("-fx-background-color: " + colorHeader + "; " +
                "-fx-background-radius: 11 11 0 0; -fx-padding: 14 18 12 18;");
        Label lblRutaTitulo = new Label(
                AdaptadorVisual.getInstance().getStopName(idOrigen) +
                        "   →   " +
                        AdaptadorVisual.getInstance().getStopName(idDestino));
        lblRutaTitulo.setStyle("-fx-text-fill: " + colorTitulo + "; -fx-font-weight: BOLD; " +
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 13;");
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        if (esAlternativa) chips.getChildren().add(crearChip("Ruta Alternativa", "#7888e8"));
        chips.getChildren().addAll(
                crearChip(vehiculo.getNombre(), vehiculo.getColorHex()),
                crearChip(condiciones.getClima().getDescripcion(), condiciones.getClima().getColorHex()),
                crearChip(condiciones.getDescripcionHorario(), condiciones.getColorHorario()),
                crearChip(criterio.charAt(0) + criterio.substring(1).toLowerCase(), "#4a3a6a"));
        header.getChildren().addAll(lblRutaTitulo, chips);

        Pane panelAnimado = construirAnimacionRuta(camino, esAlternativa, vehiculo, s);
        HBox statsRow     = construirResumenStats(camino, condiciones, vehiculo);

        VBox footer = new VBox(8);
        footer.setStyle("-fx-background-color: " + colorHeader + "; " +
                "-fx-background-radius: 0 0 11 11; -fx-padding: 10 16 14 16;");

        Button btnAccion = new Button(esAlternativa ? "Ver Ruta Principal" : "Ver Ruta Alternativa");
        btnAccion.setMaxWidth(Double.MAX_VALUE);
        btnAccion.setStyle(esAlternativa
                ? "-fx-background-color: #1e1a10; -fx-text-fill: #d4a574; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-pref-height: 36; -fx-border-color: #3a2a10; -fx-border-radius: 6; -fx-border-width: 1;"
                : "-fx-background-color: #161628; -fx-text-fill: #7888e8; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-pref-height: 36; -fx-border-color: #2a2a50; -fx-border-radius: 6; -fx-border-width: 1;");

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setStyle("-fx-background-color: transparent; -fx-text-fill: #5a3a7a; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                "-fx-pref-height: 32; -fx-border-color: #2a1040; -fx-border-radius: 6; -fx-border-width: 1;");

        btnCerrar.setOnAction(e -> s.close());
        if (esAlternativa) {
            btnAccion.setOnAction(e -> {
                s.close();
                ultimoViendoAlternativa = false;
                if (ultimoCaminoPrincipal != null) {
                    resaltarRuta(ultimoCaminoPrincipal);
                    mostrarResultadoCreativo(ultimoCaminoPrincipal, idOrigen, idDestino,
                            criterio, vehiculo, condiciones, false);
                }
            });
        } else {
            btnAccion.setOnAction(e -> {
                s.close();
                calcularYMostrarAlternativa(idOrigen, idDestino, criterio, vehiculo, condiciones);
            });
        }

        footer.getChildren().addAll(btnAccion, btnCerrar);
        root.getChildren().addAll(header, panelAnimado, statsRow, footer);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        s.setScene(escena);
        s.setOnShown(e -> centrarStage(s));
        s.show();
    }

    /*
       Función: construirAnimacionRuta
       Argumentos: (List<String>) camino, (boolean) esAlternativa,
                   (VehiculoTransporte) vehiculo, (Stage) dueño
       Objetivo: Track con paradas y vehículo SVG animado deslizándose en loop
       Retorno: (Pane): panel listo
    */
    private Pane construirAnimacionRuta(List<String> camino, boolean esAlternativa,
                                        VehiculoTransporte vehiculo, Stage dueño) {
        Pane canvas = new Pane();
        canvas.setStyle("-fx-background-color: #0d0822;");
        canvas.setPrefHeight(130);
        canvas.setPrefWidth(ANCHO_RESULT);

        if (camino == null || camino.isEmpty()) {
            Label lbl = new Label("No existe ruta entre estas paradas.");
            lbl.setStyle("-fx-text-fill: #6a4a7a; -fx-font-size: 12; -fx-font-family: 'Segoe UI';");
            lbl.setLayoutX(20); lbl.setLayoutY(54);
            canvas.getChildren().add(lbl);
            return canvas;
        }

        String colorPrincipal = esAlternativa ? "#5588ff" : "#ff4500";
        String colorOrigen    = "#33dd77";
        String colorDestino   = "#ff6633";
        String svgV           = obtenerSvgVehiculo(vehiculo);
        String colorV         = vehiculo != null ? vehiculo.getColorHex() : "#d4a574";

        double marginX = 36;
        double trackY  = 60;
        double trackW  = ANCHO_RESULT - 2 * marginX;
        int    n       = camino.size();

        double[] stopX = new double[n];
        for (int i = 0; i < n; i++)
            stopX[i] = marginX + (n == 1 ? trackW / 2 : i * trackW / (n - 1));

        if (n > 1) {
            Line trackFondo  = new Line(stopX[0], trackY, stopX[n-1], trackY);
            trackFondo.setStroke(Color.web(colorPrincipal + "33")); trackFondo.setStrokeWidth(4);
            Line trackBrillo = new Line(stopX[0], trackY, stopX[n-1], trackY);
            trackBrillo.setStroke(Color.web(colorPrincipal + "99")); trackBrillo.setStrokeWidth(2);
            canvas.getChildren().addAll(trackFondo, trackBrillo);
        }

        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        for (int i = 0; i < n; i++) {
            if (i < n - 1) {
                for (Ruta r : grafo.obtenerVecinos(camino.get(i))) {
                    if (r.getIdDestino().equals(camino.get(i + 1))) {
                        double midX = (stopX[i] + stopX[i + 1]) / 2;
                        Label lData = new Label(String.format("%.0fmin · $%.0f", r.getTiempo(), r.getCosto()));
                        lData.setStyle("-fx-text-fill: #4a3a6a; -fx-font-size: 8; -fx-font-family: 'Consolas';");
                        lData.setLayoutX(midX - 28); lData.setLayoutY(trackY - 18);
                        canvas.getChildren().add(lData);
                        break;
                    }
                }
            }

            String col   = i == 0 ? colorOrigen : (i == n-1 ? colorDestino : colorPrincipal);
            double radio = (i == 0 || i == n-1) ? 10 : 7;
            Circle c = new Circle(stopX[i], trackY, radio);
            c.setFill(Color.web(col));
            c.setStroke(Color.WHITE);
            c.setStrokeWidth(i == 0 || i == n-1 ? 2 : 1.2);
            c.setEffect(new DropShadow(16, Color.web(col)));
            canvas.getChildren().add(c);

            if (i == 0 || i == n-1) {
                Label tag = new Label(i == 0 ? "INICIO" : "FIN");
                tag.setStyle("-fx-text-fill: " + col + "; -fx-font-size: 7; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI';");
                tag.setLayoutX(stopX[i] - 12); tag.setLayoutY(trackY - 28);
                canvas.getChildren().add(tag);
            }

            Label lNom = new Label(AdaptadorVisual.getInstance().getStopName(camino.get(i)));
            lNom.setStyle("-fx-text-fill: #c8b090; -fx-font-size: 9; -fx-font-family: 'Segoe UI';");
            lNom.setMaxWidth(70); lNom.setWrapText(true);
            lNom.setLayoutX(stopX[i] - 35); lNom.setLayoutY(trackY + 14);
            canvas.getChildren().add(lNom);
        }

        SVGPath iconoV = new SVGPath();
        iconoV.setContent(svgV);
        iconoV.setFill(Color.web(colorV));
        iconoV.setScaleX(0.85); iconoV.setScaleY(0.85);
        iconoV.setLayoutX(stopX[0] - 12); iconoV.setLayoutY(trackY - 32);
        iconoV.setEffect(new DropShadow(12, Color.web(colorV)));
        canvas.getChildren().add(iconoV);

        if (n > 1) {
            double distancia = stopX[n-1] - stopX[0];
            double duracion  = 2.2 + n * 0.5;

            TranslateTransition ir = new TranslateTransition(Duration.seconds(duracion), iconoV);
            ir.setFromX(0); ir.setToX(distancia); ir.setInterpolator(Interpolator.EASE_BOTH);

            PauseTransition pausaFin    = new PauseTransition(Duration.seconds(0.5));
            TranslateTransition reset   = new TranslateTransition(Duration.millis(1), iconoV);
            reset.setToX(0);
            PauseTransition pausaInicio = new PauseTransition(Duration.seconds(0.3));

            SequentialTransition loop = new SequentialTransition(ir, pausaFin, reset, pausaInicio);
            loop.setCycleCount(Animation.INDEFINITE);
            loop.play();

            dueño.setOnHidden(e -> loop.stop());
        }
        return canvas;
    }

    /*
       Función: construirResumenStats
       Argumentos: (List<String>) camino, (SimuladorCondiciones) condiciones,
                   (VehiculoTransporte) vehiculo
       Objetivo: 4 tarjetas de resumen — tiempo ajustado, distancia, costo, tramos
       Retorno: (HBox): fila de tarjetas
    */
    private HBox construirResumenStats(List<String> camino,
                                       SimuladorCondiciones condiciones,
                                       VehiculoTransporte vehiculo) {
        double totalTiempo = 0, totalDist = 0, totalCosto = 0;
        GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

        if (camino != null && camino.size() > 1) {
            for (int i = 0; i < camino.size() - 1; i++) {
                for (Ruta r : grafo.obtenerVecinos(camino.get(i))) {
                    if (r.getIdDestino().equals(camino.get(i + 1))) {
                        totalTiempo += r.getTiempo();
                        totalDist   += r.getDistancia();
                        totalCosto  += r.getCosto();
                        break;
                    }
                }
            }
        }

        int    tramos      = camino != null ? Math.max(0, camino.size() - 1) : 0;
        double factor      = (condiciones != null && vehiculo != null) ? condiciones.getFactorTotal(vehiculo) : 1.0;
        double tiempoFinal = totalTiempo * factor;
        int    pct         = (int) Math.round((factor - 1.0) * 100);
        String labelTiempo = pct > 0
                ? String.format("%.0f min\n(+%d%%)", tiempoFinal, pct)
                : String.format("%.0f min", totalTiempo);

        HBox row = new HBox(8);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-background-color: #0d0820;");

        double cardW = (ANCHO_RESULT - 32 - 24.0) / 4.0;
        row.getChildren().addAll(
                crearCardStat("⏱ ", labelTiempo,                         "Tiempo ",    "#78c878", cardW),
                crearCardStat("╰┈➤ ", String.format("%.1f km", totalDist), "Distancia ", "#7888e8", cardW),
                crearCardStat("💰 ", String.format("$%.0f", totalCosto),  "Costo ",     "#d4a060", cardW),
                crearCardStat("↔ ",  String.valueOf(tramos),              "Tramos ",    "#b080e0", cardW));
        return row;
    }

    private VBox crearCardStat(String icon, String valor, String etiqueta, String colorHex, double ancho) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(ancho);
        card.setPadding(new Insets(8, 4, 8, 4));
        card.setStyle("-fx-background-color: " + colorHex + "18; " +
                "-fx-border-color: " + colorHex + "44; -fx-border-radius: 7; -fx-background-radius: 7;");
        Label lblIco = new Label(icon);
        lblIco.setStyle("-fx-font-size: 15;");
        Label lblVal = new Label(valor);
        lblVal.setWrapText(true); lblVal.setAlignment(Pos.CENTER); lblVal.setMaxWidth(ancho - 10);
        lblVal.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: BOLD; " +
                "-fx-font-size: 12; -fx-font-family: 'Segoe UI'; -fx-alignment: CENTER;");
        Label lblEtq = new Label(etiqueta);
        lblEtq.setStyle("-fx-text-fill: #4a3a6a; -fx-font-size: 9; -fx-font-family: 'Segoe UI';");
        card.getChildren().addAll(lblIco, lblVal, lblEtq);
        return card;
    }

    private String obtenerSvgVehiculo(VehiculoTransporte vehiculo) {
        if (vehiculo == null) return SVG_TREN;
        String n = vehiculo.getNombre().toLowerCase();
        if (n.contains("metro")) return SVG_METRO;
        if (n.contains("tren"))  return SVG_TREN;
        if (n.contains("tel"))   return SVG_TELEFERICO;
        if (n.contains("taxi"))  return SVG_TAXI;
        return SVG_TREN;
    }

    // =========================================================
    // Helpers UI del menú
    // =========================================================

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
        lbl.setStyle("-fx-text-fill: #3a2a5a; -fx-font-family: 'Segoe UI'; -fx-font-size: 9; -fx-font-weight: BOLD;");
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
        card.setStyle("-fx-background-color: #120d22; -fx-border-color: #1e1030; " +
                "-fx-border-width: 1; -fx-border-radius: 7; -fx-background-radius: 7; -fx-padding: 10 12 10 12;");
        if (svgPath != null) {
            SVGPath icono = new SVGPath();
            icono.setContent(svgPath);
            icono.setFill(Color.web(colorHex));
            icono.setScaleX(0.85); icono.setScaleY(0.85);
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
        icono.setContent(svgPath); icono.setFill(Color.web(colorHex));
        icono.setScaleX(0.95); icono.setScaleY(0.95);
        Label lbl = new Label(nombre);
        lbl.setStyle("-fx-text-fill: #7a6a8a; -fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-font-weight: BOLD;");
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(ANCHO_CARD); card.setPrefHeight(82);
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
        chip.setStyle("-fx-background-color: " + colorHex + "22; -fx-text-fill: " + colorHex + "; " +
                "-fx-background-radius: 4; -fx-padding: 2 8 2 8; -fx-font-family: 'Segoe UI'; -fx-font-size: 10;");
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

    // =========================================================
    // Métodos públicos
    // =========================================================

    /*
       Función: iniciarVisualizacion
       Argumentos: ninguno
       Objetivo: Enviar la orden a SmartGraph para que comience a calcular el
                 layout automático.
       Retorno: void
    */
    public void iniciarVisualizacion() { Platform.runLater(() -> graphView.init()); }

    /*
       Función: actualizarGrafico
       Argumentos: ninguno
       Objetivo: Refrescar el panel visual.
       Retorno: void
    */
    public void actualizarGrafico()    { Platform.runLater(() -> graphView.update()); }

    /*
       Función: fijarCoordenadasNodo
       Argumentos: (String) idNodo, (double) x, (double) y
       Objetivo: Forzar la posición de una parada en coordenadas específicas de
                 la pantalla.
       Retorno: void
    */
    public void fijarCoordenadasNodo(String idNodo, double x, double y) {
        Platform.runLater(() -> {
            SmartStylableNode nodo = graphView.getStylableVertex(idNodo);
            if (nodo instanceof SmartGraphVertex)
                ((SmartGraphVertex<?>) nodo).setPosition(x, y);
        });
    }

    /*
       Función: resaltarRuta
       Argumentos: (List<String>) idsParadas: Lista en orden del camino a iluminar.
       Objetivo: Limpiar colores previos y resaltar la ruta.
       Retorno: void
    */
    public void resaltarRuta(List<String> idsParadas) {
        Platform.runLater(() -> {
            limpiarEstilosGrafo();
            if (idsParadas == null || idsParadas.isEmpty()) return;
            for (int i = 0; i < idsParadas.size(); i++) {
                String id = idsParadas.get(i);
                SmartStylableNode n = graphView.getStylableVertex(id);
                if (n != null) n.setStyleClass("vertex-resaltado");
                if (i < idsParadas.size() - 1) {
                    SmartStylableNode a = graphView.getStylableEdge(id + "-" + idsParadas.get(i + 1));
                    if (a != null) a.setStyleClass("edge-resaltado");
                }
            }
        });
    }

    /*
       Función: resaltarRutaAlternativa
       Argumentos: (List<String>) idsParadas: paradas de la ruta alternativa
       Objetivo: Pintar la alternativa en azul distinguiéndola de la naranja principal
       Retorno: void
    */
    public void resaltarRutaAlternativa(List<String> idsParadas) {
        Platform.runLater(() -> {
            limpiarEstilosGrafo();
            if (idsParadas == null || idsParadas.isEmpty()) return;
            for (int i = 0; i < idsParadas.size(); i++) {
                String id = idsParadas.get(i);
                SmartStylableNode n = graphView.getStylableVertex(id);
                if (n != null) n.setStyleClass("vertex-alternativa");
                if (i < idsParadas.size() - 1) {
                    SmartStylableNode a = graphView.getStylableEdge(id + "-" + idsParadas.get(i + 1));
                    if (a != null) a.setStyleClass("edge-alternativa");
                }
            }
        });
    }

    /*
       Función: limpiarEstilosGrafo
       Argumentos: ninguno
       Objetivo: Restaurar todos los nodos y aristas a su apariencia original.
       Retorno: void
    */
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

}