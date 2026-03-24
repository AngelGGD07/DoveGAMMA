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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CalculadorRuta;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;

import java.util.List;

public class PanelVisualizacion extends StackPane {

    private SmartGraphPanel<String, String> graphView;
    private Digraph<String, String>         grafoBase;

    private String paradaOrigen = null;

    private Label  lblEstado;
    private Button btnVerUltimoResultado;

    // guardamos el último resultado pa' poder reabrirlo
    private String ultimoResultado = null;
    private String ultimoOrigen    = null;
    private String ultimoDestino   = null;
    private String ultimoCriterio  = null;

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
            String idLogicoArista = edge.getUnderlyingEdge().element();
            String detalles = AdaptadorVisual.getInstance().getDetallesRuta(idLogicoArista);
            Platform.runLater(() -> {
                javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alerta.setTitle("Detalles del Tramo");
                alerta.setHeaderText("Conexión: " + idLogicoArista);
                alerta.setContentText(detalles);
                alerta.showAndWait();
            });
        });

        graphView.setVertexDoubleClickAction(vertex -> {
            String idParada = vertex.getUnderlyingVertex().element();
            Platform.runLater(() -> manejarClickParada(idParada));
        });

        graphView.setAutomaticLayout(true);
        this.getChildren().add(graphView);

        // label de estado abajo al centro
        lblEstado = new Label("Doble click en una parada para calcular ruta");
        lblEstado.setStyle(estiloLabelEstado("#9a7a5a", false));
        StackPane.setAlignment(lblEstado, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblEstado, new Insets(0, 0, 14, 0));
        this.getChildren().add(lblEstado);

        // botón flotante abajo derecha — arranca oculto, aparece luego del primer cálculo
        btnVerUltimoResultado = new Button("📋  Ver último resultado");
        btnVerUltimoResultado.setStyle(
                "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; " +
                        "-fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: BOLD; " +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-padding: 8 16 8 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 3);"
        );
        btnVerUltimoResultado.setVisible(false);
        btnVerUltimoResultado.setOnAction(e -> {
            if (ultimoResultado != null) {
                mostrarResultado(ultimoResultado, ultimoOrigen, ultimoDestino, ultimoCriterio);
            }
        });
        StackPane.setAlignment(btnVerUltimoResultado, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnVerUltimoResultado, new Insets(0, 20, 14, 0));
        this.getChildren().add(btnVerUltimoResultado);
    }

    /*
       Función: manejarClickParada
       Argumentos: (String) idParada: id del nodo clickeado
       Objetivo: Controlar el flujo de dos clicks (origen → destino)
       Retorno: void
    */
    private void manejarClickParada(String idParada) {
        if (paradaOrigen == null) {
            paradaOrigen = idParada;
            String nombre = AdaptadorVisual.getInstance().getStopName(idParada);

            SmartStylableNode nodo = graphView.getStylableVertex(idParada);
            if (nodo != null) nodo.setStyleClass("vertex-origen");

            lblEstado.setText("Origen: " + nombre + " — elige el destino  (click en el mismo nodo para cancelar)");
            lblEstado.setStyle(estiloLabelEstado("#90d890", true));

        } else {
            if (idParada.equals(paradaOrigen)) {
                cancelarSeleccion();
                return;
            }
            abrirMenuCriterios(paradaOrigen, idParada);
        }
    }

    /*
       Función: abrirMenuCriterios
       Argumentos: (String) idOrigen: parada de inicio, (String) idDestino: parada final
       Objetivo: Mostrar el popup con los 4 criterios para que el usuario elija
       Retorno: void
    */
    private void abrirMenuCriterios(String idOrigen, String idDestino) {
        String nombreOrigen  = AdaptadorVisual.getInstance().getStopName(idOrigen);
        String nombreDestino = AdaptadorVisual.getInstance().getStopName(idDestino);

        Stage menuStage = new Stage();
        menuStage.initStyle(StageStyle.TRANSPARENT);
        menuStage.initModality(Modality.APPLICATION_MODAL);
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            menuStage.initOwner(this.getScene().getWindow());
        }

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(310);
        root.setStyle(
                "-fx-background-color: #0e0b1a; -fx-border-color: #4a2a60; " +
                        "-fx-border-width: 2; -fx-border-radius: 14; -fx-background-radius: 14; " +
                        "-fx-padding: 22; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 24, 0, 0, 6);"
        );

        Label titulo = new Label("¿Qué quieres optimizar?");
        titulo.setStyle("-fx-text-fill: #d4a574; -fx-font-family: 'Segoe UI'; -fx-font-size: 16; -fx-font-weight: BOLD;");

        Label rutaLabel = new Label(nombreOrigen + "  →  " + nombreDestino);
        rutaLabel.setStyle("-fx-text-fill: #6a4a7a; -fx-font-family: 'Segoe UI'; -fx-font-size: 11;");

        Button btnTiempo     = crearBotonCriterio("⏱   Tiempo",      "#1a2a1a", "#90d890");
        Button btnDistancia  = crearBotonCriterio("📏   Distancia",   "#141428", "#8090f0");
        Button btnCosto      = crearBotonCriterio("💰   Costo",       "#281a08", "#f0c060");
        Button btnTransbordo = crearBotonCriterio("🔄   Transbordos", "#1a0a28", "#c090f0");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
                "-fx-background-color: #3a1010; -fx-text-fill: #f08080; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 12; " +
                        "-fx-pref-width: 266; -fx-pref-height: 34;"
        );

        btnTiempo.setOnAction(e     -> { menuStage.close(); calcularYMostrar(idOrigen, idDestino, "TIEMPO"); });
        btnDistancia.setOnAction(e  -> { menuStage.close(); calcularYMostrar(idOrigen, idDestino, "DISTANCIA"); });
        btnCosto.setOnAction(e      -> { menuStage.close(); calcularYMostrar(idOrigen, idDestino, "COSTO"); });
        btnTransbordo.setOnAction(e -> { menuStage.close(); calcularYMostrar(idOrigen, idDestino, "TRANSBORDOS"); });
        btnCancelar.setOnAction(e   -> { menuStage.close(); cancelarSeleccion(); });

        root.getChildren().addAll(titulo, rutaLabel, btnTiempo, btnDistancia, btnCosto, btnTransbordo, btnCancelar);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        menuStage.setScene(escena);
        menuStage.centerOnScreen();
        menuStage.showAndWait();
    }

    /*
       Función: calcularYMostrar
       Argumentos: (String) idOrigen: parada inicio, (String) idDestino: parada fin, (String) criterio: criterio elegido
       Objetivo: Calcular la ruta, guardar el resultado pa' reabrir después, resaltar en grafo y abrir popup
       Retorno: void
    */
    private void calcularYMostrar(String idOrigen, String idDestino, String criterio) {
        String resultado = AdaptadorVisual.getInstance().calcularRuta(idOrigen, idDestino, criterio);

        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio);
        List<String> camino = new CalculadorRuta().calcular(
                AdaptadorVisual.getInstance().getBackend(), idOrigen, idDestino, enumCriterio
        );
        if (camino != null && !camino.isEmpty()) {
            resaltarRuta(camino);
        }

        ultimoResultado = resultado;
        ultimoOrigen    = idOrigen;
        ultimoDestino   = idDestino;
        ultimoCriterio  = criterio;
        btnVerUltimoResultado.setVisible(true);

        cancelarSeleccion();
        mostrarResultado(resultado, idOrigen, idDestino, criterio);
    }

    /*
       Función: mostrarResultado
       Argumentos: (String) resultado: texto del camino, (String) idOrigen: parada inicio,
                   (String) idDestino: parada fin, (String) criterio: criterio usado
       Objetivo: Abrir (o reabrir) el popup con el detalle del resultado
       Retorno: void
    */
    private void mostrarResultado(String resultado, String idOrigen, String idDestino, String criterio) {
        Stage resultStage = new Stage();
        resultStage.initStyle(StageStyle.TRANSPARENT);
        resultStage.initModality(Modality.NONE); // no bloquea, el usuario puede seguir en el grafo
        if (this.getScene() != null && this.getScene().getWindow() != null) {
            resultStage.initOwner(this.getScene().getWindow());
        }

        String nombreOrigen  = AdaptadorVisual.getInstance().getStopName(idOrigen);
        String nombreDestino = AdaptadorVisual.getInstance().getStopName(idDestino);

        VBox root = new VBox(10);
        root.setPrefWidth(330);
        root.setStyle(
                "-fx-background-color: #0e0b1a; -fx-border-color: #4a2a60; " +
                        "-fx-border-width: 2; -fx-border-radius: 14; -fx-background-radius: 14; " +
                        "-fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 24, 0, 0, 6);"
        );

        Label tituloRes = new Label(nombreOrigen + "  →  " + nombreDestino);
        tituloRes.setStyle(
                "-fx-text-fill: #d4a574; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; -fx-font-size: 13;"
        );

        Label lblCriterio = new Label("Optimizado por: " + criterio.toLowerCase());
        lblCriterio.setStyle("-fx-text-fill: #6a4a7a; -fx-font-size: 10; -fx-font-family: 'Segoe UI';");

        TextArea txtRes = new TextArea(resultado);
        txtRes.setEditable(false);
        txtRes.setWrapText(true);
        txtRes.setPrefRowCount(11);
        txtRes.setStyle(
                "-fx-control-inner-background: #160e28; -fx-background-color: #160e28; " +
                        "-fx-text-fill: #e8c9a8; -fx-font-family: 'Consolas'; -fx-font-size: 11; " +
                        "-fx-border-color: #2a1040; -fx-border-radius: 6; -fx-background-radius: 6;"
        );

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle(
                "-fx-background-color: #a65d48; -fx-text-fill: #e8c9a8; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-font-weight: BOLD; -fx-font-family: 'Segoe UI'; " +
                        "-fx-pref-width: 294; -fx-pref-height: 34;"
        );
        btnCerrar.setOnAction(e -> resultStage.close());

        root.getChildren().addAll(tituloRes, lblCriterio, txtRes, btnCerrar);

        Scene escena = new Scene(root);
        escena.setFill(Color.TRANSPARENT);
        resultStage.setScene(escena);
        resultStage.centerOnScreen();
        resultStage.show();
    }

    private void cancelarSeleccion() {
        if (paradaOrigen != null) {
            SmartStylableNode nodo = graphView.getStylableVertex(paradaOrigen);
            if (nodo != null) nodo.setStyleClass("vertex");
        }
        paradaOrigen = null;
        lblEstado.setText("Doble click en una parada para calcular ruta");
        lblEstado.setStyle(estiloLabelEstado("#9a7a5a", false));
    }

    private String estiloLabelEstado(String color, boolean negrita) {
        return "-fx-background-color: rgba(14,11,26,0.88); " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 6 16 6 16; " +
                "-fx-background-radius: 8; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-font-size: 12;" +
                (negrita ? "-fx-font-weight: BOLD;" : "");
    }

    private Button crearBotonCriterio(String texto, String bgColor, String textColor) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-family: 'Segoe UI'; " +
                        "-fx-font-size: 13; -fx-font-weight: BOLD; -fx-pref-width: 266; -fx-pref-height: 42;"
        );
        return btn;
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
            grafoBase.vertices().forEach(v -> {
                SmartStylableNode nodo = graphView.getStylableVertex(v.element());
                if (nodo != null) nodo.setStyleClass("vertex");
            });
            grafoBase.edges().forEach(e -> {
                SmartStylableNode arista = graphView.getStylableEdge(e.element());
                if (arista != null) arista.setStyleClass("edge");
            });

            if (idsParadas == null || idsParadas.isEmpty()) return;

            for (int i = 0; i < idsParadas.size(); i++) {
                String idActual = idsParadas.get(i);
                SmartStylableNode nodoActual = graphView.getStylableVertex(idActual);
                if (nodoActual != null) nodoActual.setStyleClass("vertex-resaltado");

                if (i < idsParadas.size() - 1) {
                    String idSiguiente = idsParadas.get(i + 1);
                    SmartStylableNode arista = graphView.getStylableEdge(idActual + "-" + idSiguiente);
                    if (arista != null) arista.setStyleClass("edge-resaltado");
                }
            }
        });
    }
}