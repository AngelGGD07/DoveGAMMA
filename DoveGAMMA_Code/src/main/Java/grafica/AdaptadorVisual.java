package grafica;

import javafx.application.Platform;
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
    }

    @Deprecated
    public static AdaptadorVisual getInstancia() {
        return getInstance();
    }

    public static AdaptadorVisual getInstance() {
        if (instance == null) {
            instance = new AdaptadorVisual();
        }
        return instance;
    }

    public void setBackend(GrafoTransporte backend) {
        this.backend = backend;
    }

    @Deprecated
    public void setPanelVisual(PanelVisualizacion panel) {
        setVisualizationPanel(panel);
    }

    public void setVisualizationPanel(PanelVisualizacion panel) {
        this.visualizationPanel = panel;
    }

    public GrafoTransporte getBackend() {
        return backend;
    }

    @Deprecated
    public PanelVisualizacion getPanelVisual() {
        return getVisualizationPanel();
    }

    public PanelVisualizacion getVisualizationPanel() {
        return visualizationPanel;
    }

    @Deprecated
    public logica.GestorDB getGestorDB() {
        return getDatabaseManager();
    }

    public logica.GestorDB getDatabaseManager() {
        return databaseManager;
    }

    public boolean addStop(String stopId, String stopName, double coordinateX, double coordinateY) {
        if (backend == null) {
            return false;
        }

        if (isStopIdAlreadyExists(stopId)) {
            return false;
        }

        if (isStopNameAlreadyExists(stopName)) {
            return false;
        }

        registerStopInBackend(stopId, stopName);
        storeVisualCoordinates(stopId, coordinateX, coordinateY);
        storeStopName(stopId, stopName);
        persistStopToDatabase(stopId, stopName, coordinateX, coordinateY);
        addStopToVisualization(stopId, stopName, coordinateX, coordinateY);

        return true;
    }

    public boolean modifyStopName(String stopId, String newName) {
        if (!stopNamesById.containsKey(stopId)) {
            return false;
        }

        if (isStopNameAlreadyExistsExcludingId(newName, stopId)) {
            return false;
        }

        updateStopName(stopId, newName);
        refreshVisualization();

        return true;
    }

    public boolean removeStop(String stopId) {
        if (backend == null || !stopNamesById.containsKey(stopId)) {
            return false;
        }

        removeStopFromBackend(stopId);
        removeStopVisualData(stopId);
        removeRoutesAssociatedWithStop(stopId);
        removeStopFromDatabase(stopId);

        return true;
    }

    public boolean addRoute(String originStopId, String destinationStopId,
                            double travelTime, double distance, double cost) {
        if (backend == null) {
            return false;
        }

        if (!areBothStopsExist(originStopId, destinationStopId)) {
            return false;
        }

        registerRouteInBackend(originStopId, destinationStopId, travelTime, cost, distance);
        storeRouteData(originStopId, destinationStopId, travelTime, distance, cost);
        persistRouteToDatabase(originStopId, destinationStopId, travelTime, distance, cost);
        addRouteToVisualization(originStopId, destinationStopId, travelTime, distance, cost);

        return true;
    }

    public boolean modifyRoute(String originStopId, String destinationStopId,
                               double newTravelTime, double newDistance, double newCost) {
        String routeKey = buildRouteKey(originStopId, destinationStopId);

        if (!existingRoutes.contains(routeKey)) {
            return false;
        }

        updateRouteInBackend(originStopId, destinationStopId, newTravelTime, newCost, newDistance);
        updateRouteData(routeKey, newTravelTime, newDistance, newCost);
        refreshVisualization();

        return true;
    }

    public boolean removeRoute(String originStopId, String destinationStopId) {
        if (backend == null) {
            return false;
        }

        String routeKey = buildRouteKey(originStopId, destinationStopId);

        if (!existingRoutes.contains(routeKey)) {
            return false;
        }

        removeRouteFromBackend(originStopId, destinationStopId);
        removeRouteData(routeKey);
        removeRouteFromDatabase(originStopId, destinationStopId);

        return true;
    }

    public String calculateRoute(String startStopId, String endStopId, String criteria) {
        if (backend == null) {
            return ERROR_BACKEND_NOT_CONNECTED;
        }

        List<String> path = backend.calcularDijkstra(startStopId, endStopId, criteria);

        if (path.isEmpty()) {
            return buildNoRouteMessage(startStopId, endStopId);
        }

        return formatRouteResult(path, criteria);
    }

    public void clearAll() {
        visualCoordinates.clear();
        stopNamesById.clear();
        existingRoutes.clear();
        routeData.clear();

        if (visualizationPanel != null) {
            visualizationPanel.clearAll();
        }

        backend = new GrafoTransporte();
    }

    @Deprecated
    public void redibujarAhora() {
        refreshVisualization();
    }

    public void refreshVisualization() {
        if (visualizationPanel == null) {
            return;
        }

        Platform.runLater(() -> {
            visualizationPanel.clearAll();
            redrawAllStops();
            redrawAllRoutes();
        });
    }

    public String getStopName(String stopId) {
        return stopNamesById.getOrDefault(stopId, stopId);
    }

    @Deprecated
    public boolean agregarParada(String id, String nombre, double x, double y) {
        return addStop(id, nombre, x, y);
    }

    @Deprecated
    public boolean modificarParada(String id, String nuevoNombre) {
        return modifyStopName(id, nuevoNombre);
    }

    @Deprecated
    public boolean eliminarParada(String id) {
        return removeStop(id);
    }

    @Deprecated
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        return addRoute(origen, destino, tiempo, distancia, costo);
    }

    @Deprecated
    public boolean modificarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        return modifyRoute(origen, destino, tiempo, distancia, costo);
    }

    @Deprecated
    public boolean eliminarRuta(String origen, String destino) {
        return removeRoute(origen, destino);
    }

    @Deprecated
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        return calculateRoute(idInicio, idFin, criterio);
    }

    @Deprecated
    public void limpiarTodo() {
        clearAll();
    }

    @Deprecated
    public String getNombre(String id) {
        return getStopName(id);
    }

    private boolean isStopIdAlreadyExists(String stopId) {
        return stopNamesById.containsKey(stopId);
    }

    private boolean isStopNameAlreadyExists(String stopName) {
        for (String existingName : stopNamesById.values()) {
            if (existingName.equalsIgnoreCase(stopName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStopNameAlreadyExistsExcludingId(String stopName, String excludeId) {
        for (Map.Entry<String, String> entry : stopNamesById.entrySet()) {
            if (!entry.getKey().equals(excludeId) && entry.getValue().equalsIgnoreCase(stopName)) {
                return true;
            }
        }
        return false;
    }

    private boolean areBothStopsExist(String originId, String destinationId) {
        return stopNamesById.containsKey(originId) && stopNamesById.containsKey(destinationId);
    }
    
    private void registerStopInBackend(String stopId, String stopName) {
        backend.registrarParada(new Parada(stopId, stopName));
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
        existingRoutes.removeIf(route -> route.startsWith(stopId + ROUTE_KEY_SEPARATOR)
                || route.endsWith(ROUTE_KEY_SEPARATOR + stopId));
        routeData.keySet().removeIf(route -> route.startsWith(stopId + ROUTE_KEY_SEPARATOR)
                || route.endsWith(ROUTE_KEY_SEPARATOR + stopId));
    }

    private String buildRouteKey(String originId, String destinationId) {
        return originId + ROUTE_KEY_SEPARATOR + destinationId;
    }

    private void registerRouteInBackend(String originId, String destinationId,
                                        double time, double cost, double distance) {
        backend.agregarRuta(originId, destinationId, time, cost, distance);
    }

    private void storeRouteData(String originId, String destinationId,
                                double time, double distance, double cost) {
        String routeKey = buildRouteKey(originId, destinationId);
        existingRoutes.add(routeKey);
        routeData.put(routeKey, new double[]{time, distance, cost});
    }

    private void updateRouteInBackend(String originId, String destinationId,
                                      double time, double cost, double distance) {
        backend.eliminarRuta(originId, destinationId);
        backend.agregarRuta(originId, destinationId, time, cost, distance);
    }

    private void updateRouteData(String routeKey, double time, double distance, double cost) {
        routeData.put(routeKey, new double[]{time, distance, cost});
    }

    private void removeRouteFromBackend(String originId, String destinationId) {
        backend.eliminarRuta(originId, destinationId);
    }

    private void removeRouteData(String routeKey) {
        existingRoutes.remove(routeKey);
        routeData.remove(routeKey);
    }

    private void addStopToVisualization(String stopId, String stopName, double x, double y) {
        if (visualizationPanel != null) {
            Platform.runLater(() -> visualizationPanel.addStopVisual(stopId, stopName, x, y));
        }
    }

    private void addRouteToVisualization(String originId, String destinationId,
                                         double time, double distance, double cost) {
        if (visualizationPanel != null) {
            Platform.runLater(() -> visualizationPanel.addRouteVisual(
                    originId, destinationId, time, distance, cost));
        }
    }

    private void redrawAllStops() {
        for (String stopId : stopNamesById.keySet()) {
            double[] position = visualCoordinates.get(stopId);
            if (position != null) {
                visualizationPanel.addStopVisual(
                        stopId, stopNamesById.get(stopId), position[0], position[1]);
            }
        }
    }

    private void redrawAllRoutes() {
        for (String routeKey : existingRoutes) {
            String[] parts = routeKey.split("\\" + ROUTE_KEY_SEPARATOR);
            double[] data = routeData.get(routeKey);

            if (parts.length == 2 && data != null) {
                visualizationPanel.addRouteVisual(parts[0], parts[1], data[0], data[1], data[2]);
            }
        }
    }

    private void persistStopToDatabase(String stopId, String stopName, double x, double y) {
        databaseManager.guardarParada(stopId, stopName, x, y);
    }

    private void persistRouteToDatabase(String originId, String destinationId,
                                        double time, double distance, double cost) {
        databaseManager.guardarRuta(originId, destinationId, time, distance, cost);
    }

    private void removeStopFromDatabase(String stopId) {
        databaseManager.eliminarParada(stopId);
    }

    private void removeRouteFromDatabase(String originId, String destinationId) {
        databaseManager.eliminarRuta(originId, destinationId);
    }

    private String buildNoRouteMessage(String startId, String endId) {
        return ERROR_NO_ROUTE_EXISTS + getStopName(startId) + AND + getStopName(endId) + DOT;
    }

    private String formatRouteResult(List<String> path, String criteria) {
        String title = getTitleForCriteria(criteria);
        String routePath = buildPathString(path);
        int stopCount = path.size();
        int hopCount = path.size() - 1;

        return String.format(FORMAT_RESULT_HEADER, title) +
                routePath + "\n" +
                String.format(FORMAT_ROUTE_SUMMARY, stopCount, hopCount);
    }

    private String getTitleForCriteria(String criteria) {
        switch (criteria.toLowerCase()) {
            case "tiempo":
                return TITLE_SHORTEST_TIME;
            case "distancia":
                return TITLE_SHORTEST_DISTANCE;
            case "costo":
                return TITLE_LOWEST_COST;
            case "transbordos":
                return TITLE_FEWEST_TRANSFERS;
            default:
                return criteria;
        }
    }

    private String buildPathString(List<String> stopIds) {
        StringBuilder pathBuilder = new StringBuilder();

        for (int i = 0; i < stopIds.size(); i++) {
            pathBuilder.append(getStopName(stopIds.get(i)));
            if (i < stopIds.size() - 1) {
                pathBuilder.append(ARROW_SEPARATOR);
            }
        }

        return pathBuilder.toString();
    }
}