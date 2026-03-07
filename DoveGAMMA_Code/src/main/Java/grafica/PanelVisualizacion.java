// PanelVisualizacion.java
package grafica;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

public class PanelVisualizacion extends Pane {

    private Map<String, NodoVisual> nodos  = new HashMap<>();
    private List<LineaVisual>       lineas = new ArrayList<>();

    // paleta de colores del proyecto
    private final Color cOscuro   = Color.web("#0a0714");
    private final Color cMorado   = Color.web("#4a1a5e");
    private final Color cTerra    = Color.web("#a65d48");
    private final Color cBeige    = Color.web("#d4a574");
    private final Color cClaro    = Color.web("#e8c9a8");
    private final Color cResalte  = Color.web("#f0c040");
    private final Color cGridLine = Color.web("#1e1030");

    private double dragOffsetX, dragOffsetY;

    public PanelVisualizacion() {
        setPrefSize(900, 630);
        setStyle("-fx-background-color: #0a0714;");
        dibujarGrid();
    }

    private void dibujarGrid() {
        int paso = 40;
        for (int x = 0; x < 1400; x += paso) {
            Line l = new Line(x, 0, x, 900);
            l.setStroke(cGridLine);
            l.setStrokeWidth(0.5);
            getChildren().add(l);
        }
        for (int y = 0; y < 900; y += paso) {
            Line l = new Line(0, y, 1400, y);
            l.setStroke(cGridLine);
            l.setStrokeWidth(0.5);
            getChildren().add(l);
        }
    }

    public void agregarParadaVisual(String id, String nombre, double x, double y) {
        x = Math.max(35, Math.min(x, getPrefWidth()  - 35));
        y = Math.max(35, Math.min(y, getPrefHeight() - 35));

        Circle circulo = new Circle(x, y, 22);
        circulo.setFill(cTerra);
        circulo.setStroke(cBeige);
        circulo.setStrokeWidth(2.5);

        Text lblNombre = new Text(nombre);
        lblNombre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblNombre.setFill(cClaro);
        posicionarTexto(lblNombre, x, y - 30);

        Text lblId = new Text(id.length() > 4 ? id.substring(0, 4) : id);
        lblId.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        lblId.setFill(cClaro);
        posicionarTexto(lblId, x, y + 4);

        // animación de entrada
        circulo.setScaleX(0); circulo.setScaleY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(350), circulo);
        st.setToX(1); st.setToY(1);
        st.play();

        // hover
        circulo.setOnMouseEntered(e -> {
            circulo.setFill(cBeige);
            circulo.setRadius(26);
            circulo.setEffect(new javafx.scene.effect.DropShadow(18, cTerra));
        });
        circulo.setOnMouseExited(e -> {
            circulo.setFill(cTerra);
            circulo.setRadius(22);
            circulo.setEffect(null);
        });

        // drag
        final String idFinal = id;
        circulo.setOnMousePressed(e -> {
            dragOffsetX = e.getX() - circulo.getCenterX();
            dragOffsetY = e.getY() - circulo.getCenterY();
            circulo.toFront(); lblNombre.toFront(); lblId.toFront();
        });

        circulo.setOnMouseDragged(e -> {
            double nx = Math.max(35, Math.min(e.getX() - dragOffsetX, getPrefWidth()  - 35));
            double ny = Math.max(35, Math.min(e.getY() - dragOffsetY, getPrefHeight() - 35));

            circulo.setCenterX(nx); circulo.setCenterY(ny);
            posicionarTexto(lblNombre, nx, ny - 30);
            posicionarTexto(lblId, nx, ny + 4);

            NodoVisual nodo = nodos.get(idFinal);
            if (nodo != null) {
                nodo.x = nx; nodo.y = ny;
                redibPjarLineas(idFinal);
            }
        });

        NodoVisual nodo = new NodoVisual(id, nombre, circulo, lblNombre, lblId, x, y);
        nodos.put(id, nodo);
        getChildren().addAll(circulo, lblNombre, lblId);
    }

    public void agregarRutaVisual(String idOrigen, String idDestino,
                                  double tiempo, double distancia, double costo) {
        NodoVisual origen  = nodos.get(idOrigen);
        NodoVisual destino = nodos.get(idDestino);
        if (origen == null || destino == null) return;

        LineaVisual lv = construirLinea(origen, destino, tiempo, distancia, costo);
        lineas.add(lv);

        FadeTransition ft = new FadeTransition(Duration.millis(500), lv.linea);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();

        subirNodosAlFrente();
    }

    // elimina solo la linea visual entre origen y destino — las paradas quedan
    public void eliminarRutaVisual(String idOrigen, String idDestino) {
        Iterator<LineaVisual> iter = lineas.iterator();
        while (iter.hasNext()) {
            LineaVisual lv = iter.next();
            if (lv.origen.id.equals(idOrigen) && lv.destino.id.equals(idDestino)) {
                // borra del panel todos los elementos gráficos de esa línea
                getChildren().removeAll(lv.linea, lv.flecha, lv.bgTexto, lv.etiqueta);
                iter.remove();
                break;
            }
        }
    }

    // cambia el texto del nombre de una parada en la pantalla
    public void actualizarNombreParada(String id, String nuevoNombre) {
        NodoVisual nodo = nodos.get(id);
        if (nodo == null) return;
        nodo.nombre = nuevoNombre;
        nodo.lblNombre.setText(nuevoNombre);
        posicionarTexto(nodo.lblNombre, nodo.x, nodo.y - 30);
    }

    private LineaVisual construirLinea(NodoVisual origen, NodoVisual destino,
                                       double tiempo, double distancia, double costo) {
        double x1 = origen.x,  y1 = origen.y;
        double x2 = destino.x, y2 = destino.y;
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double r = 24;

        double startX = x1 + r * Math.cos(angulo);
        double startY = y1 + r * Math.sin(angulo);
        double endX   = x2 - r * Math.cos(angulo);
        double endY   = y2 - r * Math.sin(angulo);

        Line linea = new Line(startX, startY, endX, endY);
        linea.setStroke(cBeige);
        linea.setStrokeWidth(2.5);
        linea.setOpacity(0.85);

        Polygon flecha = construirFlecha(endX, endY, angulo);

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        Rectangle bgTexto = new Rectangle(0, 0, 100, 18);
        bgTexto.setFill(Color.web("#0a0714", 0.85));
        bgTexto.setArcWidth(4); bgTexto.setArcHeight(4);

        Text etiqueta = new Text(String.format("⏱%.0fm  📏%.1fkm  $%.0f", tiempo, distancia, costo));
        etiqueta.setFont(Font.font("Consolas", 10));
        etiqueta.setFill(cClaro);

        double anchoEtiq = etiqueta.getBoundsInLocal().getWidth() + 10;
        bgTexto.setWidth(anchoEtiq);
        bgTexto.setX(midX - anchoEtiq / 2);
        bgTexto.setY(midY - 14);
        posicionarTexto(etiqueta, midX, midY - 3);

        getChildren().addAll(linea, flecha, bgTexto, etiqueta);

        return new LineaVisual(linea, flecha, bgTexto, etiqueta, origen, destino, tiempo, distancia, costo);
    }

    private Polygon construirFlecha(double endX, double endY, double angulo) {
        double len = 13;
        Polygon flecha = new Polygon(
                endX, endY,
                endX - len * Math.cos(angulo - Math.PI / 6),
                endY - len * Math.sin(angulo - Math.PI / 6),
                endX - len * Math.cos(angulo + Math.PI / 6),
                endY - len * Math.sin(angulo + Math.PI / 6)
        );
        flecha.setFill(cTerra);
        flecha.setStroke(cBeige);
        flecha.setStrokeWidth(1.5);
        return flecha;
    }

    private void redibPjarLineas(String idNodo) {
        for (LineaVisual lv : lineas) {
            if (!lv.origen.id.equals(idNodo) && !lv.destino.id.equals(idNodo)) continue;

            double x1 = lv.origen.x,  y1 = lv.origen.y;
            double x2 = lv.destino.x, y2 = lv.destino.y;
            double angulo = Math.atan2(y2 - y1, x2 - x1);
            double r = 24;

            double startX = x1 + r * Math.cos(angulo);
            double startY = y1 + r * Math.sin(angulo);
            double endX   = x2 - r * Math.cos(angulo);
            double endY   = y2 - r * Math.sin(angulo);

            lv.linea.setStartX(startX); lv.linea.setStartY(startY);
            lv.linea.setEndX(endX);     lv.linea.setEndY(endY);

            lv.flecha.getPoints().setAll(
                    endX, endY,
                    endX - 13 * Math.cos(angulo - Math.PI / 6),
                    endY - 13 * Math.sin(angulo - Math.PI / 6),
                    endX - 13 * Math.cos(angulo + Math.PI / 6),
                    endY - 13 * Math.sin(angulo + Math.PI / 6)
            );

            double midX = (startX + endX) / 2;
            double midY = (startY + endY) / 2;
            double w = lv.bgTexto.getWidth();
            lv.bgTexto.setX(midX - w / 2);
            lv.bgTexto.setY(midY - 14);
            posicionarTexto(lv.etiqueta, midX, midY - 3);
        }
    }

    // resalta la ruta calculada por Dijkstra
    public void resaltarRuta(List<String> idsParadas) {
        lineas.forEach(lv -> {
            lv.linea.setStroke(cBeige);
            lv.linea.setStrokeWidth(2.5);
            lv.linea.setOpacity(0.4);
        });

        for (int i = 0; i < idsParadas.size() - 1; i++) {
            String id1 = idsParadas.get(i);
            String id2 = idsParadas.get(i + 1);

            lineas.stream()
                    .filter(lv -> lv.origen.id.equals(id1) && lv.destino.id.equals(id2))
                    .forEach(lv -> {
                        lv.linea.setStroke(cResalte);
                        lv.linea.setStrokeWidth(5);
                        lv.linea.setOpacity(1);

                        FadeTransition ft = new FadeTransition(Duration.millis(700), lv.linea);
                        ft.setFromValue(1.0);
                        ft.setToValue(0.4);
                        ft.setAutoReverse(true);
                        ft.setCycleCount(6);
                        ft.play();
                    });
        }
    }

    public void limpiarTodo() {
        getChildren().clear();
        nodos.clear();
        lineas.clear();
        dibujarGrid();
    }

    private void posicionarTexto(Text t, double cx, double cy) {
        t.setX(cx - t.getBoundsInLocal().getWidth() / 2);
        t.setY(cy);
    }

    private void subirNodosAlFrente() {
        nodos.values().forEach(nv -> {
            nv.circulo.toFront();
            nv.lblNombre.toFront();
            nv.lblId.toFront();
        });
    }

    // ── clases internas ──

    static class NodoVisual {
        String id, nombre;
        Circle circulo;
        Text   lblNombre, lblId;
        double x, y;

        NodoVisual(String id, String nombre, Circle c, Text ln, Text li, double x, double y) {
            this.id = id; this.nombre = nombre;
            this.circulo = c; this.lblNombre = ln; this.lblId = li;
            this.x = x; this.y = y;
        }
    }

    static class LineaVisual {
        Line      linea;
        Polygon   flecha;
        Rectangle bgTexto;
        Text      etiqueta;
        NodoVisual origen, destino;
        double    tiempo, distancia, costo;

        LineaVisual(Line l, Polygon f, Rectangle bg, Text e,
                    NodoVisual o, NodoVisual d,
                    double tiempo, double distancia, double costo) {
            this.linea = l; this.flecha = f;
            this.bgTexto = bg; this.etiqueta = e;
            this.origen = o; this.destino = d;
            this.tiempo = tiempo; this.distancia = distancia; this.costo = costo;
        }
    }
}