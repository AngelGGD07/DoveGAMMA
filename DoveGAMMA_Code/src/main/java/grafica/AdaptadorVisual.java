package grafica;

import javafx.application.Platform;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Parada;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdaptadorVisual {

    private static final String ROUTE_KEY_SEPARATOR = "|";
    private static final String ERROR_BACKEND_NOT_CONNECTED = "Backend no conectado.";
    private static final String ERROR_NO_ROUTE_EXISTS = "No existe ruta entre ";
    private static final String AND = " y ";
    private static final String DOT = ".";

    private static final String TITLE_SHORTEST_TIME = "Menor Tiempo";
    private static final String TITLE_SHORTEST_DISTANCE = "Menor Distancia";
    private static final String TITLE_LOWEST_COST = "Menor Costo";
    private static final String TITLE_FEWEST_TRANSFERS = "Menos Transbordos";

    private static final String FORMAT_RESULT_HEADER = "=== %s ===\n";
    private static final String FORMAT_ROUTE_SUMMARY = "Paradas: %d  |  Saltos: %d";
    private static final String ARROW_SEPARATOR = " -> ";

    private static AdaptadorVisual instance;

    private GrafoTransporte backend;
    private PanelVisualizacion visualizationPanel;
    private logica.GestorDB databaseManager;

    private final Map<String, double[]> visualCoordinates = new HashMap<>();
    private final Map<String, String> stopNamesById = new HashMap<>();
    private final Set<String> existingRoutes = new HashSet<>();
    private final Map<String, double[]> routeData = new HashMap<>();

    private AdaptadorVisual() {
        this.databaseManager = new logica.GestorDB();
        this.backend = new GrafoTransporte(); // Inicializamos para evitar NullPointerException
    }

    public static AdaptadorVisual getInstance() {
        if (instance == null) {
            instance = new AdaptadorVisual();
        }
        return instance;
    }

    // --- GETTERS Y SETTERS ---

    public void setBackend(GrafoTransporte backend) {
        this.backend = backend;
    }

    public GrafoTransporte getBackend() {
        return backend;
    }

    public void setVisualizationPanel(PanelVisualizacion panel) {
        this.visualizationPanel = panel;
    }

    public PanelVisualizacion getVisualizationPanel() {
        return visualizationPanel;
    }

    public logica.GestorDB getDatabaseManager() {
        return databaseManager;
    }

    // --- GESTIÓN DE PARADAS ---

    public boolean addStop(String stopId, String stopName, double coordinateX, double coordinateY) {
        if (backend == null) return false;

        if (isStopIdAlreadyExists(stopId) || isStopNameAlreadyExists(stopName)) {
            return false;
        }

        // 1. Registro en Backend
        backend.registrarParada(new Parada(stopId, stopName, coordinateX, coordinateY));

        // 2. Registro local para visualización
        storeVisualCoordinates(stopId, coordinateX, coordinateY);
        storeStopName(stopId, stopName);

        // 3. Persistencia
        persistStopToDatabase(stopId, stopName, coordinateX, coordinateY);

        // 4. GUI
        addStopToVisualization(stopId, stopName, coordinateX, coordinateY);

        return true;
    }

    public boolean modifyStopName(String stopId, String newName) {
        if (!stopNamesById.containsKey(stopId)) return false;
        if (isStopNameAlreadyExistsExcludingId(newName, stopId)) return false;

        updateStopName(stopId, newName);
        refreshVisualization();
        return true;
    }

    public boolean removeStop(String stopId) {
        if (backend == null || !stopNamesById.containsKey(stopId)) return false;

        removeStopFromBackend(stopId);
        removeStopVisualData(stopId);
        removeRoutesAssociatedWithStop(stopId);
        removeStopFromDatabase(stopId);
        refreshVisualization();

        return true;
    }

    // --- GESTIÓN DE RUTAS ---

    public boolean addRoute(String originStopId, String destinationStopId,
                            double travelTime, double distance, double cost) {
        if (backend == null) return false;
        if (!areBothStopsExist(originStopId, destinationStopId)) return false;

        registerRouteInBackend(originStopId, destinationStopId, travelTime, cost, distance);
        storeRouteData(originStopId, destinationStopId, travelTime, distance, cost);
        persistRouteToDatabase(originStopId, destinationStopId, travelTime, distance, cost);
        addRouteToVisualization(originStopId, destinationStopId, travelTime, distance, cost);

        return true;
    }

    public boolean modifyRoute(String originStopId, String destinationStopId,
                               double newTravelTime, double newDistance, double newCost) {
        String routeKey = buildRouteKey(originStopId, destinationStopId);
        if (!existingRoutes.contains(routeKey)) return false;

        updateRouteInBackend(originStopId, destinationStopId, newTravelTime, newCost, newDistance);
        updateRouteData(routeKey, newTravelTime, newDistance, newCost);
        refreshVisualization();

        return true;
    }

    public boolean removeRoute(String originStopId, String destinationStopId) {
        if (backend == null) return false;
        String routeKey = buildRouteKey(originStopId, destinationStopId);
        if (!existingRoutes.contains(routeKey)) return false;

        removeRouteFromBackend(originStopId, destinationStopId);
        removeRouteData(routeKey);
        removeRouteFromDatabase(originStopId, destinationStopId);
        refreshVisualization();

        return true;
    }

    // --- CÁLCULO DE RUTA (Lógica corregida) ---

    public String calculateRoute(String startStopId, String endStopId, String criteria) {
        if (backend == null) return ERROR_BACKEND_NOT_CONNECTED;

        // Instanciamos el calculador y le pasamos el backend
        CalculadorRuta calculador = new CalculadorRuta();
        List<String> path = calculador.calcularDijkstra(backend, startStopId, endStopId, criteria);

        if (path.isEmpty()) {
            return buildNoRouteMessage(startStopId, endStopId);
        }

        return formatRouteResult(path, criteria);
    }

    // --- UTILIDADES Y GUI ---

    public void clearAll() {
        visualCoordinates.clear();
        stopNamesById.clear();
        existingRoutes.clear();
        routeData.clear();
        if (visualizationPanel != null) visualizationPanel.clearAll();
        backend = new GrafoTransporte();
    }

    public void refreshVisualization() {
        if (visualizationPanel == null) return;
        Platform.runLater(() -> {
            visualizationPanel.clearAll();
            redrawAllStops();
            redrawAllRoutes();
        });
    }

    public String getStopName(String stopId) {
        return stopNamesById.getOrDefault(stopId, stopId);
    }

    // --- MÉTODOS PRIVADOS DE APOYO ---

    private boolean isStopIdAlreadyExists(String stopId) {
        return stopNamesById.containsKey(stopId);
    }

    private boolean isStopNameAlreadyExists(String stopName) {
        return stopNamesById.values().stream().anyMatch(name -> name.equalsIgnoreCase(stopName));
    }

    private boolean isStopNameAlreadyExistsExcludingId(String stopName, String excludeId) {
        return stopNamesById.entrySet().stream()
                .anyMatch(e -> !e.getKey().equals(excludeId) && e.getValue().equalsIgnoreCase(stopName));
    }

    private boolean areBothStopsExist(String originId, String destinationId) {
        return stopNamesById.containsKey(originId) && stopNamesById.containsKey(destinationId);
    }

    private void storeVisualCoordinates(String stopId, double x, double y) {
        visualCoordinates.put(stopId, new double[]{x, y});
    }

    private void storeStopName(String stopId, String stopName) {
        stopNamesById.put(stopId, stopName);
    }

    private void updateStopName(String stopId, String newName) {
        stopNamesById.put(stopId, newName);
    }

    private void removeStopFromBackend(String stopId) {
        backend.eliminarParada(stopId);
    }

    private void removeStopVisualData(String stopId) {
        visualCoordinates.remove(stopId);
        stopNamesById.remove(stopId);
    }

    private void removeRoutesAssociatedWithStop(String stopId) {
        existingRoutes.removeIf(r -> r.contains(stopId + ROUTE_KEY_SEPARATOR) || r.contains(ROUTE_KEY_SEPARATOR + stopId));
        routeData.keySet().removeIf(r -> r.contains(stopId + ROUTE_KEY_SEPARATOR) || r.contains(ROUTE_KEY_SEPARATOR + stopId));
    }

    private String buildRouteKey(String originId, String destinationId) {
        return originId + ROUTE_KEY_SEPARATOR + destinationId;
    }

    private void registerRouteInBackend(String originId, String destinationId, double t, double c, double d) {
        backend.agregarRuta(originId, destinationId, t, c, d);
    }

    private void storeRouteData(String originId, String destinationId, double t, double d, double c) {
        String key = buildRouteKey(originId, destinationId);
        existingRoutes.add(key);
        routeData.put(key, new double[]{t, d, c});
    }

    private void updateRouteInBackend(String o, String d, double t, double c, double dist) {
        backend.eliminarRuta(o, d);
        backend.agregarRuta(o, d, t, c, dist);
    }

    private void updateRouteData(String key, double t, double d, double c) {
        routeData.put(key, new double[]{t, d, c});
    }

    private void removeRouteFromBackend(String o, String d) {
        backend.eliminarRuta(o, d);
    }

    private void removeRouteData(String key) {
        existingRoutes.remove(key);
        routeData.remove(key);
    }

    private void addStopToVisualization(String id, String name, double x, double y) {
        if (visualizationPanel != null) {
            Platform.runLater(() -> visualizationPanel.addStopVisual(id, name, x, y));
        }
    }

    private void addRouteToVisualization(String o, String d, double t, double dist, double c) {
        if (visualizationPanel != null) {
            Platform.runLater(() -> visualizationPanel.addRouteVisual(o, d, t, dist, c));
        }
    }

    private void redrawAllStops() {
        visualCoordinates.forEach((id, pos) ->
                visualizationPanel.addStopVisual(id, stopNamesById.get(id), pos[0], pos[1]));
    }

    private void redrawAllRoutes() {
        existingRoutes.forEach(key -> {
            String[] parts = key.split("\\" + ROUTE_KEY_SEPARATOR);
            double[] data = routeData.get(key);
            if (parts.length == 2 && data != null) {
                visualizationPanel.addRouteVisual(parts[0], parts[1], data[0], data[1], data[2]);
            }
        });
    }

    private void persistStopToDatabase(String id, String n, double x, double y) {
        databaseManager.guardarParada(id, n, x, y);
    }

    private void persistRouteToDatabase(String o, String d, double t, double dist, double c) {
        databaseManager.guardarRuta(o, d, t, dist, c);
    }

    private void removeStopFromDatabase(String id) {
        databaseManager.eliminarParada(id);
    }

    private void removeRouteFromDatabase(String o, String d) {
        databaseManager.eliminarRuta(o, d);
    }

    private String buildNoRouteMessage(String startId, String endId) {
        return ERROR_NO_ROUTE_EXISTS + getStopName(startId) + AND + getStopName(endId) + DOT;
    }

    private String formatRouteResult(List<String> path, String criteria) {
        String title = getTitleForCriteria(criteria);
        String routePath = buildPathString(path);
        return String.format(FORMAT_RESULT_HEADER, title) + routePath + "\n" +
                String.format(FORMAT_ROUTE_SUMMARY, path.size(), path.size() - 1);
    }

    private String getTitleForCriteria(String criteria) {
        switch (criteria.toLowerCase()) {
            case "tiempo": return TITLE_SHORTEST_TIME;
            case "distancia": return TITLE_SHORTEST_DISTANCE;
            case "costo": return TITLE_LOWEST_COST;
            case "transbordos": return TITLE_FEWEST_TRANSFERS;
            default: return criteria;
        }
    }

    private String buildPathString(List<String> stopIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stopIds.size(); i++) {
            sb.append(getStopName(stopIds.get(i)));
            if (i < stopIds.size() - 1) sb.append(ARROW_SEPARATOR);
        }
        return sb.toString();
    }

    // --- MÉTODOS DEPRECATED (Para compatibilidad) ---
    @Deprecated public static AdaptadorVisual getInstancia() { return getInstance(); }
    @Deprecated public void setPanelVisual(PanelVisualizacion p) { setVisualizationPanel(p); }
    @Deprecated public PanelVisualizacion getPanelVisual() { return getVisualizationPanel(); }
    @Deprecated public logica.GestorDB getGestorDB() { return getDatabaseManager(); }
}