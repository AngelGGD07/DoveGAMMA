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
import javafx.scene.effect.DropShadow;

import java.util.*;


public class PanelVisualizacion extends Pane {

    private static final Color COLOR_BACKGROUND_DARK = Color.web("#0a0714");
    private static final Color COLOR_PURPLE_ACCENT = Color.web("#4a1a5e");
    private static final Color COLOR_TERRACOTTA = Color.web("#a65d48");
    private static final Color COLOR_BEIGE = Color.web("#d4a574");
    private static final Color COLOR_LIGHT_BEIGE = Color.web("#e8c9a8");
    private static final Color COLOR_HIGHLIGHT_GOLD = Color.web("#f0c040");
    private static final Color COLOR_GRID_LINE = Color.web("#1e1030");
    private static final Color COLOR_LABEL_BACKGROUND = Color.web("#0a0714", 0.85);

    private static final double PANEL_PREFERRED_WIDTH = 900.0;
    private static final double PANEL_PREFERRED_HEIGHT = 630.0;
    private static final double GRID_MAX_X = 1400.0;
    private static final double GRID_MAX_Y = 900.0;

    private static final double NODE_RADIUS_DEFAULT = 22.0;
    private static final double NODE_RADIUS_HOVER = 26.0;
    private static final double NODE_RADIUS_OFFSET = 24.0;
    private static final double NODE_MARGIN = 35.0;
    private static final double NODE_STROKE_WIDTH = 2.5;
    private static final double NODE_ANIMATION_DURATION_MS = 350.0;

    private static final String FONT_FAMILY_PRIMARY = "Segoe UI";
    private static final String FONT_FAMILY_MONO = "Consolas";
    private static final double FONT_SIZE_STOP_NAME = 12.0;
    private static final double FONT_SIZE_STOP_ID = 11.0;
    private static final double FONT_SIZE_ROUTE_METRICS = 10.0;
    private static final double LABEL_OFFSET_Y_NAME = 30.0;
    private static final double LABEL_OFFSET_Y_ID = 4.0;

    private static final double ROUTE_STROKE_WIDTH_DEFAULT = 2.5;
    private static final double ROUTE_STROKE_WIDTH_HIGHLIGHT = 5.0;
    private static final double ROUTE_OPACITY_DEFAULT = 0.85;
    private static final double ROUTE_OPACITY_DIMMED = 0.4;
    private static final double ROUTE_ANIMATION_DURATION_MS = 500.0;

    private static final double ARROW_LENGTH = 13.0;
    private static final double ARROW_STROKE_WIDTH = 1.5;
    private static final double ARROW_ANGLE_OFFSET_RADIANS = Math.PI / 6.0;

    private static final double ROUTE_LABEL_HEIGHT = 18.0;
    private static final double ROUTE_LABEL_PADDING = 10.0;
    private static final double ROUTE_LABEL_OFFSET_Y = 14.0;
    private static final double ROUTE_LABEL_CORNER_RADIUS = 4.0;

    private static final int GRID_STEP_SIZE = 40;
    private static final double GRID_STROKE_WIDTH = 0.5;

    private static final double HIGHLIGHT_FADE_DURATION_MS = 700.0;
    private static final double HIGHLIGHT_OPACITY_MIN = 0.4;
    private static final double HIGHLIGHT_OPACITY_MAX = 1.0;
    private static final int HIGHLIGHT_CYCLE_COUNT = 6;
    private static final double DROP_SHADOW_RADIUS = 18.0;

    private final Map<String, VisualStop> stops = new HashMap<>();
    private final List<VisualRoute> routes = new ArrayList<>();

    private double dragOffsetX;
    private double dragOffsetY;

    public PanelVisualizacion() {
        setPreferredSize();
        setBackgroundColor();
        drawGrid();
    }

    private void setPreferredSize() {
        setPrefSize(PANEL_PREFERRED_WIDTH, PANEL_PREFERRED_HEIGHT);
    }

    private void setBackgroundColor() {
        setStyle("-fx-background-color: " + COLOR_BACKGROUND_DARK.toString().replace("0x", "#") + ";");
    }

    private void drawGrid() {
        drawVerticalGridLines();
        drawHorizontalGridLines();
    }

    private void drawVerticalGridLines() {
        for (int x = 0; x < GRID_MAX_X; x += GRID_STEP_SIZE) {
            Line gridLine = createGridLine(x, 0, x, GRID_MAX_Y);
            getChildren().add(gridLine);
        }
    }

    private void drawHorizontalGridLines() {
        for (int y = 0; y < GRID_MAX_Y; y += GRID_STEP_SIZE) {
            Line gridLine = createGridLine(0, y, GRID_MAX_Y, y);
            getChildren().add(gridLine);
        }
    }

    private Line createGridLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(COLOR_GRID_LINE);
        line.setStrokeWidth(GRID_STROKE_WIDTH);
        return line;
    }

    public void addStopVisual(String stopId, String stopName, double x, double y) {
        double clampedX = clampCoordinateToPanelBounds(x, getPrefWidth());
        double clampedY = clampCoordinateToPanelBounds(y, getPrefHeight());

        Circle nodeCircle = createNodeCircle(clampedX, clampedY);
        Text nameLabel = createStopNameLabel(stopName, clampedX, clampedY);
        Text idLabel = createStopIdLabel(stopId, clampedX, clampedY);

        setupNodeInteractions(nodeCircle, nameLabel, idLabel, stopId);
        animateNodeAppearance(nodeCircle);

        VisualStop stop = new VisualStop(stopId, stopName, nodeCircle, nameLabel, idLabel, clampedX, clampedY);
        stops.put(stopId, stop);

        getChildren().addAll(nodeCircle, nameLabel, idLabel);
    }

    public void updateStopName(String stopId, String newName) {
        VisualStop stop = stops.get(stopId);
        if (stop == null) {
            return;
        }

        stop.name = newName;
        stop.nameLabel.setText(newName);
        centerTextAtPosition(stop.nameLabel, stop.x, stop.y - LABEL_OFFSET_Y_NAME);
    }

    private double clampCoordinateToPanelBounds(double coordinate, double maxDimension) {
        return Math.max(NODE_MARGIN, Math.min(coordinate, maxDimension - NODE_MARGIN));
    }

    private Circle createNodeCircle(double centerX, double centerY) {
        Circle circle = new Circle(centerX, centerY, NODE_RADIUS_DEFAULT);
        circle.setFill(COLOR_TERRACOTTA);
        circle.setStroke(COLOR_BEIGE);
        circle.setStrokeWidth(NODE_STROKE_WIDTH);
        return circle;
    }

    private Text createStopNameLabel(String stopName, double centerX, double centerY) {
        Text label = new Text(stopName);
        label.setFont(Font.font(FONT_FAMILY_PRIMARY, FontWeight.BOLD, FONT_SIZE_STOP_NAME));
        label.setFill(COLOR_LIGHT_BEIGE);
        centerTextAtPosition(label, centerX, centerY - LABEL_OFFSET_Y_NAME);
        return label;
    }

    private Text createStopIdLabel(String stopId, double centerX, double centerY) {
        String displayId = truncateIdForDisplay(stopId);
        Text label = new Text(displayId);
        label.setFont(Font.font(FONT_FAMILY_MONO, FontWeight.BOLD, FONT_SIZE_STOP_ID));
        label.setFill(COLOR_LIGHT_BEIGE);
        centerTextAtPosition(label, centerX, centerY + LABEL_OFFSET_Y_ID);
        return label;
    }

    private String truncateIdForDisplay(String stopId) {
        final int MAX_DISPLAY_LENGTH = 4;
        if (stopId.length() > MAX_DISPLAY_LENGTH) {
            return stopId.substring(0, MAX_DISPLAY_LENGTH);
        }
        return stopId;
    }

    private void centerTextAtPosition(Text text, double centerX, double centerY) {
        double textWidth = text.getBoundsInLocal().getWidth();
        text.setX(centerX - textWidth / 2.0);
        text.setY(centerY);
    }

    private void setupNodeInteractions(Circle nodeCircle, Text nameLabel, Text idLabel, String stopId) {
        setupHoverEffect(nodeCircle);
        setupDragBehavior(nodeCircle, nameLabel, idLabel, stopId);
    }

    private void setupHoverEffect(Circle nodeCircle) {
        nodeCircle.setOnMouseEntered(event -> {
            nodeCircle.setFill(COLOR_BEIGE);
            nodeCircle.setRadius(NODE_RADIUS_HOVER);
            nodeCircle.setEffect(new DropShadow(DROP_SHADOW_RADIUS, COLOR_TERRACOTTA));
        });

        nodeCircle.setOnMouseExited(event -> {
            nodeCircle.setFill(COLOR_TERRACOTTA);
            nodeCircle.setRadius(NODE_RADIUS_DEFAULT);
            nodeCircle.setEffect(null);
        });
    }

    private void setupDragBehavior(Circle nodeCircle, Text nameLabel, Text idLabel, String stopId) {
        nodeCircle.setOnMousePressed(event -> {
            dragOffsetX = event.getX() - nodeCircle.getCenterX();
            dragOffsetY = event.getY() - nodeCircle.getCenterY();
            bringNodeToFront(nodeCircle, nameLabel, idLabel);
        });

        nodeCircle.setOnMouseDragged(event -> {
            handleNodeDrag(nodeCircle, nameLabel, idLabel, stopId, event.getX(), event.getY());
        });
    }

    private void bringNodeToFront(Circle circle, Text nameLabel, Text idLabel) {
        circle.toFront();
        nameLabel.toFront();
        idLabel.toFront();
    }

    private void handleNodeDrag(Circle nodeCircle, Text nameLabel, Text idLabel,
                                String stopId, double mouseX, double mouseY) {
        double newX = calculateClampedCoordinate(mouseX, dragOffsetX, getPrefWidth());
        double newY = calculateClampedCoordinate(mouseY, dragOffsetY, getPrefHeight());

        updateNodePosition(nodeCircle, nameLabel, idLabel, newX, newY);
        updateStopCoordinates(stopId, newX, newY);
        redrawConnectedRoutes(stopId);
    }

    private double calculateClampedCoordinate(double mousePosition, double offset, double maxDimension) {
        return Math.max(NODE_MARGIN, Math.min(mousePosition - offset, maxDimension - NODE_MARGIN));
    }

    private void updateNodePosition(Circle circle, Text nameLabel, Text idLabel, double newX, double newY) {
        circle.setCenterX(newX);
        circle.setCenterY(newY);
        centerTextAtPosition(nameLabel, newX, newY - LABEL_OFFSET_Y_NAME);
        centerTextAtPosition(idLabel, newX, newY + LABEL_OFFSET_Y_ID);
    }

    private void updateStopCoordinates(String stopId, double newX, double newY) {
        VisualStop stop = stops.get(stopId);
        if (stop != null) {
            stop.x = newX;
            stop.y = newY;
        }
    }

    private void animateNodeAppearance(Circle nodeCircle) {
        nodeCircle.setScaleX(0);
        nodeCircle.setScaleY(0);

        ScaleTransition scaleTransition = new ScaleTransition(
                Duration.millis(NODE_ANIMATION_DURATION_MS), nodeCircle);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
    }

    public void addRouteVisual(String originStopId, String destinationStopId,
                               double travelTime, double distance, double cost) {
        VisualStop originStop = stops.get(originStopId);
        VisualStop destinationStop = stops.get(destinationStopId);

        if (originStop == null || destinationStop == null) {
            return;
        }

        VisualRoute route = createVisualRoute(originStop, destinationStop, travelTime, distance, cost);
        routes.add(route);

        animateRouteAppearance(route.routeLine);
        bringAllStopsToFront();
    }

    public void removeRouteVisual(String originStopId, String destinationStopId) {
        Iterator<VisualRoute> routeIterator = routes.iterator();

        while (routeIterator.hasNext()) {
            VisualRoute route = routeIterator.next();

            if (isRouteMatch(route, originStopId, destinationStopId)) {
                removeRouteFromPanel(route);
                routeIterator.remove();
                break;
            }
        }
    }

    private boolean isRouteMatch(VisualRoute route, String originId, String destinationId) {
        return route.originStop.id.equals(originId) && route.destinationStop.id.equals(destinationId);
    }

    private void removeRouteFromPanel(VisualRoute route) {
        getChildren().removeAll(
                route.routeLine,
                route.arrowHead,
                route.labelBackground,
                route.metricsLabel
        );
    }

    private VisualRoute createVisualRoute(VisualStop origin, VisualStop destination,
                                          double travelTime, double distance, double cost) {
        double angle = calculateAngleBetweenPoints(origin.x, origin.y, destination.x, destination.y);

        Line routeLine = createRouteLine(origin, destination, angle);
        Polygon arrowHead = createArrowHead(destination.x, destination.y, angle);
        Text metricsLabel = createRouteMetricsLabel(travelTime, distance, cost);
        Rectangle labelBackground = createLabelBackground(metricsLabel);

        positionRouteLabel(labelBackground, metricsLabel, origin, destination, angle);

        getChildren().addAll(routeLine, arrowHead, labelBackground, metricsLabel);

        return new VisualRoute(routeLine, arrowHead, labelBackground, metricsLabel,
                origin, destination, travelTime, distance, cost);
    }

    private double calculateAngleBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.atan2(y2 - y1, x2 - x1);
    }

    private Line createRouteLine(VisualStop origin, VisualStop destination, double angle) {
        double startX = origin.x + NODE_RADIUS_OFFSET * Math.cos(angle);
        double startY = origin.y + NODE_RADIUS_OFFSET * Math.sin(angle);
        double endX = destination.x - NODE_RADIUS_OFFSET * Math.cos(angle);
        double endY = destination.y - NODE_RADIUS_OFFSET * Math.sin(angle);

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(COLOR_BEIGE);
        line.setStrokeWidth(ROUTE_STROKE_WIDTH_DEFAULT);
        line.setOpacity(ROUTE_OPACITY_DEFAULT);
        return line;
    }

    private Polygon createArrowHead(double endX, double endY, double angle) {
        double leftAngle = angle - ARROW_ANGLE_OFFSET_RADIANS;
        double rightAngle = angle + ARROW_ANGLE_OFFSET_RADIANS;

        Polygon arrow = new Polygon(
                endX, endY,
                endX - ARROW_LENGTH * Math.cos(leftAngle),
                endY - ARROW_LENGTH * Math.sin(leftAngle),
                endX - ARROW_LENGTH * Math.cos(rightAngle),
                endY - ARROW_LENGTH * Math.sin(rightAngle)
        );

        arrow.setFill(COLOR_TERRACOTTA);
        arrow.setStroke(COLOR_BEIGE);
        arrow.setStrokeWidth(ARROW_STROKE_WIDTH);
        return arrow;
    }

    private Text createRouteMetricsLabel(double travelTime, double distance, double cost) {
        String metricsText = String.format("⏱%.0fm  📏%.1fkm  $%.0f", travelTime, distance, cost);
        Text label = new Text(metricsText);
        label.setFont(Font.font(FONT_FAMILY_MONO, FONT_SIZE_ROUTE_METRICS));
        label.setFill(COLOR_LIGHT_BEIGE);
        return label;
    }

    private Rectangle createLabelBackground(Text metricsLabel) {
        double labelWidth = metricsLabel.getBoundsInLocal().getWidth() + ROUTE_LABEL_PADDING;

        Rectangle background = new Rectangle(0, 0, labelWidth, ROUTE_LABEL_HEIGHT);
        background.setFill(COLOR_LABEL_BACKGROUND);
        background.setArcWidth(ROUTE_LABEL_CORNER_RADIUS);
        background.setArcHeight(ROUTE_LABEL_CORNER_RADIUS);
        return background;
    }

    private void positionRouteLabel(Rectangle background, Text label,
                                    VisualStop origin, VisualStop destination, double angle) {
        double startX = origin.x + NODE_RADIUS_OFFSET * Math.cos(angle);
        double startY = origin.y + NODE_RADIUS_OFFSET * Math.sin(angle);
        double endX = destination.x - NODE_RADIUS_OFFSET * Math.cos(angle);
        double endY = destination.y - NODE_RADIUS_OFFSET * Math.sin(angle);

        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;
        double backgroundWidth = background.getWidth();

        background.setX(midX - backgroundWidth / 2.0);
        background.setY(midY - ROUTE_LABEL_OFFSET_Y);
        centerTextAtPosition(label, midX, midY - ROUTE_LABEL_OFFSET_Y + 11);
    }

    private void animateRouteAppearance(Line routeLine) {
        FadeTransition fadeTransition = new FadeTransition(
                Duration.millis(ROUTE_ANIMATION_DURATION_MS), routeLine);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(ROUTE_OPACITY_DEFAULT);
        fadeTransition.play();
    }

    private void redrawConnectedRoutes(String stopId) {
        for (VisualRoute route : routes) {
            if (isRouteConnectedToStop(route, stopId)) {
                redrawRoute(route);
            }
        }
    }

    private boolean isRouteConnectedToStop(VisualRoute route, String stopId) {
        return route.originStop.id.equals(stopId) || route.destinationStop.id.equals(stopId);
    }

    private void redrawRoute(VisualRoute route) {
        double angle = calculateAngleBetweenPoints(
                route.originStop.x, route.originStop.y,
                route.destinationStop.x, route.destinationStop.y
        );

        updateRouteLineEndpoints(route.routeLine, route.originStop, route.destinationStop, angle);
        updateArrowHeadPosition(route.arrowHead, route.destinationStop.x, route.destinationStop.y, angle);
        updateRouteLabelPosition(route.labelBackground, route.metricsLabel,
                route.originStop, route.destinationStop, angle);
    }

    private void updateRouteLineEndpoints(Line line, VisualStop origin, VisualStop destination, double angle) {
        double startX = origin.x + NODE_RADIUS_OFFSET * Math.cos(angle);
        double startY = origin.y + NODE_RADIUS_OFFSET * Math.sin(angle);
        double endX = destination.x - NODE_RADIUS_OFFSET * Math.cos(angle);
        double endY = destination.y - NODE_RADIUS_OFFSET * Math.sin(angle);

        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
    }

    private void updateArrowHeadPosition(Polygon arrowHead, double endX, double endY, double angle) {
        double leftAngle = angle - ARROW_ANGLE_OFFSET_RADIANS;
        double rightAngle = angle + ARROW_ANGLE_OFFSET_RADIANS;

        arrowHead.getPoints().setAll(
                endX, endY,
                endX - ARROW_LENGTH * Math.cos(leftAngle),
                endY - ARROW_LENGTH * Math.sin(leftAngle),
                endX - ARROW_LENGTH * Math.cos(rightAngle),
                endY - ARROW_LENGTH * Math.sin(rightAngle)
        );
    }

    private void updateRouteLabelPosition(Rectangle background, Text label,
                                          VisualStop origin, VisualStop destination, double angle) {
        double startX = origin.x + NODE_RADIUS_OFFSET * Math.cos(angle);
        double startY = origin.y + NODE_RADIUS_OFFSET * Math.sin(angle);
        double endX = destination.x - NODE_RADIUS_OFFSET * Math.cos(angle);
        double endY = destination.y - NODE_RADIUS_OFFSET * Math.sin(angle);

        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;
        double backgroundWidth = background.getWidth();

        background.setX(midX - backgroundWidth / 2.0);
        background.setY(midY - ROUTE_LABEL_OFFSET_Y);
        centerTextAtPosition(label, midX, midY - 3);
    }

    public void highlightRoute(List<String> stopIds) {
        dimAllRoutes();

        for (int i = 0; i < stopIds.size() - 1; i++) {
            String currentStopId = stopIds.get(i);
            String nextStopId = stopIds.get(i + 1);

            highlightRouteSegment(currentStopId, nextStopId);
        }
    }

    private void dimAllRoutes() {
        for (VisualRoute route : routes) {
            route.routeLine.setStroke(COLOR_BEIGE);
            route.routeLine.setStrokeWidth(ROUTE_STROKE_WIDTH_DEFAULT);
            route.routeLine.setOpacity(ROUTE_OPACITY_DIMMED);
        }
    }

    private void highlightRouteSegment(String originId, String destinationId) {
        for (VisualRoute route : routes) {
            if (isRouteMatch(route, originId, destinationId)) {
                applyHighlightStyle(route.routeLine);
                animateHighlight(route.routeLine);
            }
        }
    }

    private void applyHighlightStyle(Line routeLine) {
        routeLine.setStroke(COLOR_HIGHLIGHT_GOLD);
        routeLine.setStrokeWidth(ROUTE_STROKE_WIDTH_HIGHLIGHT);
        routeLine.setOpacity(HIGHLIGHT_OPACITY_MAX);
    }

    private void animateHighlight(Line routeLine) {
        FadeTransition fadeTransition = new FadeTransition(
                Duration.millis(HIGHLIGHT_FADE_DURATION_MS), routeLine);
        fadeTransition.setFromValue(HIGHLIGHT_OPACITY_MAX);
        fadeTransition.setToValue(HIGHLIGHT_OPACITY_MIN);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(HIGHLIGHT_CYCLE_COUNT);
        fadeTransition.play();
    }

    public void clearAll() {
        getChildren().clear();
        stops.clear();
        routes.clear();
        drawGrid();
    }

    private void bringAllStopsToFront() {
        for (VisualStop stop : stops.values()) {
            stop.nodeCircle.toFront();
            stop.nameLabel.toFront();
            stop.idLabel.toFront();
        }
    }

    @Deprecated
    public void agregarParadaVisual(String id, String nombre, double x, double y) {
        addStopVisual(id, nombre, x, y);
    }

    @Deprecated
    public void agregarRutaVisual(String idOrigen, String idDestino,
                                  double tiempo, double distancia, double costo) {
        addRouteVisual(idOrigen, idDestino, tiempo, distancia, costo);
    }

    @Deprecated
    public void eliminarRutaVisual(String idOrigen, String idDestino) {
        removeRouteVisual(idOrigen, idDestino);
    }

    @Deprecated
    public void actualizarNombreParada(String id, String nuevoNombre) {
        updateStopName(id, nuevoNombre);
    }

    @Deprecated
    public void resaltarRuta(List<String> idsParadas) {
        highlightRoute(idsParadas);
    }

    @Deprecated
    public void limpiarTodo() {
        clearAll();
    }

    private static class VisualStop {
        String id;
        String name;
        Circle nodeCircle;
        Text nameLabel;
        Text idLabel;
        double x;
        double y;

        VisualStop(String id, String name, Circle circle, Text nameLabel, Text idLabel, double x, double y) {
            this.id = id;
            this.name = name;
            this.nodeCircle = circle;
            this.nameLabel = nameLabel;
            this.idLabel = idLabel;
            this.x = x;
            this.y = y;
        }
    }

    private static class VisualRoute {
        Line routeLine;
        Polygon arrowHead;
        Rectangle labelBackground;
        Text metricsLabel;
        VisualStop originStop;
        VisualStop destinationStop;
        double travelTime;
        double distance;
        double cost;

        VisualRoute(Line line, Polygon arrow, Rectangle background, Text label,
                    VisualStop origin, VisualStop destination,
                    double travelTime, double distance, double cost) {
            this.routeLine = line;
            this.arrowHead = arrow;
            this.labelBackground = background;
            this.metricsLabel = label;
            this.originStop = origin;
            this.destinationStop = destination;
            this.travelTime = travelTime;
            this.distance = distance;
            this.cost = cost;
        }
    }
}