// PanelVisualizacion.java - Visualizador de grafo con flechas y efectos
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

public class PanelVisualizacion extends Pane {

    private Map<String, NodoVisual> nodos;
    private List<LineaVisual> lineas;
    private String darkPurple = "#1a0a2e";
    private String terracota = "#a65d48";
    private String beige = "#d4a574";
    private String lightBeige = "#e8c9a8";

    public PanelVisualizacion() {
        this.nodos = new HashMap<>();
        this.lineas = new ArrayList<>();
        setStyle("-fx-background-color: " + darkPurple + ";");
        setPrefSize(900, 650);
    }

    public void agregarParadaVisual(String id, String nombre, double x, double y) {
        // Validar límites
        x = Math.max(30, Math.min(x, getPrefWidth() - 30));
        y = Math.max(30, Math.min(y, getPrefHeight() - 30));

        // Círculo principal
        Circle circulo = new Circle(x, y, 20);
        circulo.setFill(Color.web(terracota));
        circulo.setStroke(Color.web(beige));
        circulo.setStrokeWidth(3);

        // Animación de entrada
        ScaleTransition st = new ScaleTransition(Duration.millis(500), circulo);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        st.play();

        // Label con nombre
        Text label = new Text(x - 25, y - 30, nombre);
        label.setFill(Color.web(lightBeige));
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        // ID pequeño debajo
        Text idLabel = new Text(x - 10, y + 35, id);
        idLabel.setFill(Color.web(beige));
        idLabel.setFont(Font.font("Consolas", 11));

        // Efectos hover
        circulo.setOnMouseEntered(e -> {
            circulo.setFill(Color.web(beige));
            circulo.setStroke(Color.web(terracota));
            circulo.setRadius(25);
        });

        circulo.setOnMouseExited(e -> {
            circulo.setFill(Color.web(terracota));
            circulo.setStroke(Color.web(beige));
            circulo.setRadius(20);
        });

        NodoVisual nodo = new NodoVisual(id, circulo, label, idLabel, x, y);
        nodos.put(id, nodo);

        getChildren().addAll(circulo, label, idLabel);

        // Traer al frente
        circulo.toFront();
        label.toFront();
        idLabel.toFront();
    }

    public void agregarRutaVisual(String idOrigen, String idDestino, double tiempo, double distancia) {
        NodoVisual origen = nodos.get(idOrigen);
        NodoVisual destino = nodos.get(idDestino);

        if (origen == null || destino == null) return;

        double x1 = origen.x;
        double y1 = origen.y;
        double x2 = destino.x;
        double y2 = destino.y;

        // Calcular ángulo para la flecha
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double radio = 25; // Radio del círculo + margen

        // Puntos de inicio y fin (en el borde de los círculos)
        double startX = x1 + radio * Math.cos(angulo);
        double startY = y1 + radio * Math.sin(angulo);
        double endX = x2 - radio * Math.cos(angulo);
        double endY = y2 - radio * Math.sin(angulo);

        // Línea principal
        Line linea = new Line(startX, startY, endX, endY);
        linea.setStroke(Color.web(beige));
        linea.setStrokeWidth(3);
        linea.setOpacity(0.8);

        // Flecha (triángulo)
        double arrowLength = 15;
        double arrowWidth = 10;

        Polygon flecha = new Polygon();
        flecha.getPoints().addAll(
                endX, endY,
                endX - arrowLength * Math.cos(angulo - Math.PI / 6),
                endY - arrowLength * Math.sin(angulo - Math.PI / 6),
                endX - arrowLength * Math.cos(angulo + Math.PI / 6),
                endY - arrowLength * Math.sin(angulo + Math.PI / 6)
        );
        flecha.setFill(Color.web(terracota));
        flecha.setStroke(Color.web(beige));
        flecha.setStrokeWidth(2);

        // Label con pesos
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        Text pesoLabel = new Text(midX - 30, midY - 10,
                String.format("⏱%.0f | 📏%.1f", tiempo, distancia));
        pesoLabel.setFill(Color.web(lightBeige));
        pesoLabel.setFont(Font.font("Consolas", 11));
        pesoLabel.setStyle("-fx-background-color: " + darkPurple + "; -fx-padding: 2px;");

        // Fondo para el texto (rectángulo semitransparente)
        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(
                midX - 35, midY - 25, 70, 20
        );
        bg.setFill(Color.web(darkPurple, 0.8));
        bg.setArcWidth(5);
        bg.setArcHeight(5);

        LineaVisual lv = new LineaVisual(linea, flecha, origen, destino);
        lineas.add(lv);

        getChildren().addAll(linea, flecha, bg, pesoLabel);

        // Animación de entrada
        FadeTransition ft = new FadeTransition(Duration.millis(600), linea);
        ft.setFromValue(0);
        ft.setToValue(0.8);
        ft.play();

        // Traer nodos al frente
        origen.circulo.toFront();
        origen.label.toFront();
        origen.idLabel.toFront();
        destino.circulo.toFront();
        destino.label.toFront();
        destino.idLabel.toFront();
    }

    public void resaltarRuta(List<String> idsParadas) {
        // Resetear estilos
        lineas.forEach(l -> {
            l.linea.setStroke(Color.web(beige));
            l.linea.setStrokeWidth(3);
            l.linea.setOpacity(0.8);
        });

        // Resaltar líneas de la ruta
        for (int i = 0; i < idsParadas.size() - 1; i++) {
            String id1 = idsParadas.get(i);
            String id2 = idsParadas.get(i + 1);

            lineas.stream()
                    .filter(l -> l.origen.id.equals(id1) && l.destino.id.equals(id2))
                    .forEach(l -> {
                        l.linea.setStroke(Color.web("#00ff88")); // Verde neón
                        l.linea.setStrokeWidth(5);
                        l.linea.setOpacity(1);

                        // Animación de pulso
                        ScaleTransition st = new ScaleTransition(Duration.millis(1000), l.linea);
                        st.setToX(1.2);
                        st.setToY(1.2);
                        st.setAutoReverse(true);
                        st.setCycleCount(4);
                        st.play();
                    });
        }
    }

    // Métodos para integración con backend mediante reflection
    public void agregarParadaVisualDesdeObjeto(Object parada) {
        try {
            String id = (String) parada.getClass().getMethod("getId").invoke(parada);
            String nombre = (String) parada.getClass().getMethod("getNombre").invoke(parada);
            double x = (Double) parada.getClass().getMethod("getX").invoke(parada);
            double y = (Double) parada.getClass().getMethod("getY").invoke(parada);
            agregarParadaVisual(id, nombre, x, y);
        } catch (Exception e) {
            System.err.println("Error al procesar parada: " + e.getMessage());
        }
    }

    public void agregarRutaVisualDesdeObjeto(Object ruta) {
        try {
            Object origen = ruta.getClass().getMethod("getOrigen").invoke(ruta);
            Object destino = ruta.getClass().getMethod("getDestino").invoke(ruta);
            String idOrigen = (String) origen.getClass().getMethod("getId").invoke(origen);
            String idDestino = (String) destino.getClass().getMethod("getId").invoke(destino);
            double tiempo = (Double) ruta.getClass().getMethod("getTiempo").invoke(ruta);
            double distancia = (Double) ruta.getClass().getMethod("getDistancia").invoke(ruta);
            agregarRutaVisual(idOrigen, idDestino, tiempo, distancia);
        } catch (Exception e) {
            System.err.println("Error al procesar ruta: " + e.getMessage());
        }
    }

    private static class NodoVisual {
        String id;
        Circle circulo;
        Text label;
        Text idLabel;
        double x, y;

        NodoVisual(String id, Circle c, Text l, Text il, double x, double y) {
            this.id = id;
            this.circulo = c;
            this.label = l;
            this.idLabel = il;
            this.x = x;
            this.y = y;
        }
    }

    private static class LineaVisual {
        Line linea;
        Polygon flecha;
        NodoVisual origen;
        NodoVisual destino;

        LineaVisual(Line l, Polygon f, NodoVisual o, NodoVisual d) {
            this.linea = l;
            this.flecha = f;
            this.origen = o;
            this.destino = d;
        }
    }
}