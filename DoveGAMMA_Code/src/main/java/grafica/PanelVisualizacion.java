package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartStylableNode;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.util.List;

public class PanelVisualizacion extends StackPane {

    private SmartGraphPanel<String, String> graphView;

    /*
       Función: PanelVisualizacion
       Argumentos: (Digraph<String, String>) grafoVisual: el grafo lógico base.
       Objetivo: Inicializar el panel gráfico inyectando Nombres de Paradas, filtrando etiquetas de rutas y configurando el doble clic.
       Retorno: (void): Constructor.
    */
    public PanelVisualizacion(Digraph<String, String> grafoVisual) {
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        com.brunomnsilva.smartgraph.graphview.SmartGraphProperties properties = new com.brunomnsilva.smartgraph.graphview.SmartGraphProperties();

        java.net.URI cssUri = null;
        try {
            java.net.URL recursoCss = getClass().getResource("/smartgraph.css");
            if (recursoCss != null) cssUri = recursoCss.toURI();
            else cssUri = new java.io.File("smartgraph.css").toURI();
        } catch (Exception e) {
            cssUri = new java.io.File("smartgraph.css").toURI();
        }

        graphView = new SmartGraphPanel<>(grafoVisual, properties, strategy, cssUri);

        graphView.setVertexLabelProvider(id -> grafica.AdaptadorVisual.getInstance().getStopName(id));

        graphView.setEdgeLabelProvider(idArista -> grafica.AdaptadorVisual.getInstance().getEdgeDataAsString(idArista));

        graphView.setEdgeDoubleClickAction(edge -> {
            String idLogicoArista = edge.getUnderlyingEdge().element();
            String detalles = grafica.AdaptadorVisual.getInstance().getDetallesRuta(idLogicoArista);

            Platform.runLater(() -> {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Detalles del Tramo");
                alerta.setHeaderText("Conexión: " + idLogicoArista);
                alerta.setContentText(detalles);
                alerta.showAndWait();
            });
        });

        graphView.setAutomaticLayout(true);
        this.getChildren().add(graphView);
    }

    /*
       Función: iniciarVisualizacion
       Argumentos: Ninguno
       Objetivo: Ejecutar el renderizado inicial de la librería gráfica una vez que el panel está agregado en la escena.
       Retorno: (void): Solo ejecuta una instrucción en el hilo gráfico de JavaFX.
    */
    public void iniciarVisualizacion() {
        Platform.runLater(() -> {
            graphView.init();
        });
    }

    /*
       Función: actualizarGrafico
       Argumentos: Ninguno
       Objetivo: Refrescar la vista para repintar etiquetas y aplicar cambios de nodos.
       Retorno: (void): Modifica el estado del panel sin devolver datos.
    */
    public void actualizarGrafico() {
        Platform.runLater(() -> {
            graphView.update();
        });
    }

    /*
       Función: fijarCoordenadasNodo
       Argumentos:
             (String) idNodo: el identificador único del nodo que se desea mover.
             (double) x: la coordenada horizontal objetivo en pantalla.
             (double) y: la coordenada vertical objetivo en pantalla.
       Objetivo: Posicionar un nodo específico en unas coordenadas exactas en la pantalla de la interfaz.
       Retorno: (void): Modifica atributos visuales.
    */
    public void fijarCoordenadasNodo(String idNodo, double x, double y) {
        Platform.runLater(() -> {
            SmartStylableNode nodo = graphView.getStylableVertex(idNodo);

            if (nodo instanceof SmartGraphVertex) {
                ((SmartGraphVertex<?>) nodo).setPosition(x, y);
            }
        });
    }

    /*
       Función: resaltarRuta
       Argumentos: (List<String>) idsParadas: la lista ordenada con los identificadores de los nodos que conforman la ruta calculada.
       Objetivo: Alterar las clases CSS de los nodos y aristas específicos para destacar visualmente el camino más corto en el mapa.
       Retorno: (void): Altera visualmente la aplicación sin devolver datos al controlador.
    */
    public void resaltarRuta(List<String> idsParadas) {
        Platform.runLater(() -> {

            graphView.getSmartVertices().forEach(v -> v.setStyleClass("vertex"));
            graphView.getSmartEdges().forEach(e -> e.setStyleClass("edge"));

            if (idsParadas == null || idsParadas.isEmpty()) return;

            for (int i = 0; i < idsParadas.size(); i++) {
                String idActual = idsParadas.get(i);

                SmartStylableNode nodoActual = graphView.getStylableVertex(idActual);
                if (nodoActual != null) {
                    nodoActual.setStyleClass("vertex-resaltado");
                }

                if (i < idsParadas.size() - 1) {
                    String idSiguiente = idsParadas.get(i + 1);
                    String idArista = idActual + "-" + idSiguiente;

                    SmartStylableNode arista = graphView.getStylableEdge(idArista);
                    if (arista != null) {
                        arista.setStyleClass("edge-resaltado");
                    }
                }
            }
        });
    }

    /*
       Función: clearAll
       Argumentos: Ninguno
       Objetivo: Cumplir con la interfaz o métodos anteriores de limpieza.
       Retorno: (void): No retorna valores.
    */
    public void clearAll() {
    }
}