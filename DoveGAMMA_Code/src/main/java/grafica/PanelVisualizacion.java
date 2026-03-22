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

// CORRECCIÓN: Importaciones añadidas
import logica.GrafoTransporte;
import logica.Ruta;

import java.util.List;

public class PanelVisualizacion extends StackPane {

    private SmartGraphPanel<String, String> graphView;
    private Digraph<String, String> grafoBase;

    public PanelVisualizacion(Digraph<String, String> grafoVisual) {
        this.grafoBase = grafoVisual;

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

        graphView.setVertexLabelProvider(id -> AdaptadorVisual.getInstance().getStopName(id));

        graphView.setEdgeLabelProvider(idArista -> AdaptadorVisual.getInstance().getEdgeDataAsString(idArista));

        graphView.setEdgeDoubleClickAction(edge -> {
            String idLogicoArista = edge.getUnderlyingEdge().element();
            String detalles = AdaptadorVisual.getInstance().getDetallesRuta(idLogicoArista);

            Platform.runLater(() -> {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Detalles del Tramo");
                alerta.setHeaderText("Conexión: " + idLogicoArista);
                alerta.setContentText(detalles);
                alerta.showAndWait();
            });
        });

        graphView.setVertexDoubleClickAction(vertex -> {
            String idParada = vertex.getUnderlyingVertex().element();
            String nombre = AdaptadorVisual.getInstance().getStopName(idParada);
            GrafoTransporte grafo = AdaptadorVisual.getInstance().getBackend();

            StringBuilder sb = new StringBuilder();
            sb.append("Rutas de salida desde ").append(nombre).append(":\n\n");
            boolean tieneRutas = false;

            for (Ruta r : grafo.obtenerVecinos(idParada)) {
                tieneRutas = true;
                String destName = AdaptadorVisual.getInstance().getStopName(r.getIdDestino());
                sb.append("➡ ").append(destName)
                        .append(" (").append(r.getTiempo()).append(" min, ")
                        .append(r.getDistancia()).append(" km, $")
                        .append(r.getCosto()).append(")\n");
            }
            if (!tieneRutas) sb.append("Ninguna ruta de salida conectada directamente.");

            Platform.runLater(() -> {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Detalles de la Parada (En Mapa)");
                alerta.setHeaderText("Parada: " + nombre + " (" + idParada + ")");
                alerta.setContentText(sb.toString());
                alerta.showAndWait();
            });
        });

        graphView.setAutomaticLayout(true);
        this.getChildren().add(graphView);
    }

    public void iniciarVisualizacion() {
        Platform.runLater(() -> {
            graphView.init();
        });
    }

    public void actualizarGrafico() {
        Platform.runLater(() -> {
            graphView.update();
        });
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
                if (nodo != null) {
                    nodo.setStyleClass("vertex");
                }
            });

            grafoBase.edges().forEach(e -> {
                SmartStylableNode arista = graphView.getStylableEdge(e.element());
                if (arista != null) {
                    arista.setStyleClass("edge");
                }
            });

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

    public void clearAll() {
    }
}