package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Parada;

import logica.persistencia.GestorDB;



import java.util.Collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
// nitido
public class AdaptadorVisual {

    private static AdaptadorVisual instancia;

    private GrafoTransporte       logicaGrafo;
    private logica.GestorDB       gestorBaseDatos;
    private Digraph<String, String> grafoVisual;
    private PanelVisualizacion    panelVisualizacion;


    private GrafoTransporte backend;
    private PanelVisualizacion visualizationPanel;
    private GestorDB databaseManager;

    private final Map<String, double[]> visualCoordinates = new HashMap<>();
    private final Map<String, String> stopNamesById = new HashMap<>();
    private final Set<String> existingRoutes = new HashSet<>();
    private final Map<String, double[]> routeData = new HashMap<>();

    private AdaptadorVisual() {
        this.databaseManager = new GestorDB();
        this.backend = new GrafoTransporte(); // Inicializamos para evitar NullPointerException

    private final Map<String, String>   nombresParadas      = new HashMap<>();
    private final Map<String, double[]> coordenadasParadas  = new HashMap<>(); // guarda x,y de cada parada
    private final Set<String>           rutasResaltadas     = new HashSet<>();
    private final Set<String>           rutasConTransbordo  = new HashSet<>(); // "origen-destino"

    private AdaptadorVisual() {
        this.gestorBaseDatos = new logica.GestorDB();
        this.logicaGrafo     = new GrafoTransporte();
        this.grafoVisual     = new DigraphEdgeList<>();

    }

    public static AdaptadorVisual getInstance() {
        if (instancia == null) {
            instancia = new AdaptadorVisual();
        }
        return instancia;
    }

    public void inicializarPanel() {
        this.panelVisualizacion = new PanelVisualizacion(grafoVisual);
    }

    public PanelVisualizacion getVisualizationPanel() { return panelVisualizacion; }
    public GrafoTransporte    getBackend()            { return logicaGrafo; }
    public logica.GestorDB    getDatabaseManager()    { return gestorBaseDatos; }

    // retorna el mapa completo de ids->nombres (solo lectura)
    public Map<String, String> getNombresParadas() {
        return Collections.unmodifiableMap(nombresParadas);
    }

    // retorna [x, y] de una parada
    public double[] getCoordenadas(String id) {
        return coordenadasParadas.getOrDefault(id, new double[]{0.0, 0.0});
    }


    public GestorDB getDatabaseManager() {
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

    // true si esa ruta tiene transbordo
    public boolean tieneTransbordo(String idArista) {
        return rutasConTransbordo.contains(idArista);

    }

    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (nombresParadas.containsKey(id)) return false;

        boolean ok = logicaGrafo.registrarParada(new Parada(id, nombre, x, y));

        if (ok) {
            grafoVisual.insertVertex(id);
            nombresParadas.put(id, nombre);
            coordenadasParadas.put(id, new double[]{x, y}); // guardamos coords
            gestorBaseDatos.guardarParada(id, nombre, x, y);

            if (panelVisualizacion != null) {
                panelVisualizacion.actualizarGrafico();
                panelVisualizacion.fijarCoordenadasNodo(id, x, y);
            }
            return true;
        }
        return false;
    }

    public boolean modificarNombreParada(String id, String nuevoNombre) {
        if (!nombresParadas.containsKey(id)) return false;

        logicaGrafo.modificarParada(id, nuevoNombre, 0, 0);
        nombresParadas.put(id, nuevoNombre);
        return true;
    }

    public boolean eliminarParada(String id) {
        if (!nombresParadas.containsKey(id)) return false;

        if (logicaGrafo.eliminarParada(id)) {
            grafoVisual.removeVertex(
                    grafoVisual.vertices().stream()
                            .filter(v -> v.element().equals(id))
                            .findFirst().get()
            );
            nombresParadas.remove(id);
            coordenadasParadas.remove(id); // limpiamos coords
            gestorBaseDatos.eliminarParada(id);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    public boolean agregarRuta(String origen, String destino,
                               double tiempo, double distancia, double costo) {
        return agregarRutaConTransbordo(origen, destino, tiempo, distancia, costo, false);
    }

    // versión con transbordo - úsala desde el controlador
    public boolean agregarRutaConTransbordo(String origen, String destino,
                                            double tiempo, double distancia, double costo,
                                            boolean transbordo) {
        if (!nombresParadas.containsKey(origen) || !nombresParadas.containsKey(destino)) return false;

        if (logicaGrafo.agregarRuta(origen, destino, tiempo, costo, distancia)) {
            String idArista = origen + "-" + destino;
            grafoVisual.insertEdge(origen, destino, idArista);
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo);

            if (transbordo) rutasConTransbordo.add(idArista);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    public boolean modificarRuta(String origen, String destino,
                                 double tiempo, double distancia, double costo) {
        return modificarRutaConTransbordo(origen, destino, tiempo, distancia, costo, false);
    }

    // versión con transbordo
    public boolean modificarRutaConTransbordo(String origen, String destino,
                                              double tiempo, double distancia, double costo,
                                              boolean transbordo) {
        if (logicaGrafo.modificarRuta(origen, destino, tiempo, costo, distancia)) {
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo);

            String idArista = origen + "-" + destino;
            if (transbordo) {
                rutasConTransbordo.add(idArista);
            } else {
                rutasConTransbordo.remove(idArista);
            }
            return true;
        }
        return false;
    }

    public boolean eliminarRuta(String origen, String destino) {
        if (logicaGrafo.eliminarRuta(origen, destino)) {
            String idArista = origen + "-" + destino;
            grafoVisual.edges().stream()
                    .filter(e -> e.element().equals(idArista))
                    .findFirst()
                    .ifPresent(edge -> grafoVisual.removeEdge(edge));

            rutasConTransbordo.remove(idArista); // limpiamos transbordo si tenía
            gestorBaseDatos.eliminarRuta(origen, destino);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    public String calcularRuta(String idInicio, String idFin, String criterio) {
        CalculadorRuta calculador = new CalculadorRuta();
        List<String> camino = calculador.calcularDijkstra(logicaGrafo, idInicio, idFin, criterio);

        rutasResaltadas.clear();

        if (camino == null || camino.isEmpty()) {
            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return "No existe ruta entre " + getStopName(idInicio) + " y " + getStopName(idFin) + ".";
        }

        double totalTiempo = 0, totalDistancia = 0, totalCosto = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("=== Ruta Óptima (").append(criterio.toUpperCase()).append(") ===\n\n");

        for (int i = 0; i < camino.size(); i++) {
            String actual = camino.get(i);
            sb.append("◉ ").append(getStopName(actual)).append("\n");

            if (i < camino.size() - 1) {
                String siguiente  = camino.get(i + 1);
                String idArista   = actual + "-" + siguiente;
                rutasResaltadas.add(idArista);

                for (logica.Ruta r : logicaGrafo.obtenerVecinos(actual)) {
                    if (r.getIdDestino().equals(siguiente)) {
                        totalTiempo     += r.getTiempo();
                        totalDistancia  += r.getDistancia();
                        totalCosto      += r.getCosto();
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
        sb.append("• Distancia: ").append(totalDistancia).append(" km\n");
        sb.append("• Costo total: $").append(totalCosto).append("\n");
        sb.append("• Tramos: ").append(camino.size() - 1).append("\n");

        if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
        return sb.toString();
    }

    public String getStopName(String id) {
        return nombresParadas.getOrDefault(id, id);
    }

    public String getEdgeDataAsString(String idArista) {
        if (!rutasResaltadas.contains(idArista)) return "";
        return getDetallesRuta(idArista);
    }

    public String getDetallesRuta(String idArista) {
        String[] partes = idArista.split("-");
        if (partes.length == 2) {
            String origen  = partes[0];
            String destino = partes[1];
            for (logica.Ruta r : logicaGrafo.obtenerVecinos(origen)) {
                if (r.getIdDestino().equals(destino)) {
                    return r.getTiempo() + " min | " + r.getDistancia() + " km | $" + r.getCosto();
                }
            }
        }
        return "Datos no disponibles";
    }

    public void limpiarTodo() {
        logicaGrafo     = new GrafoTransporte();
        grafoVisual     = new DigraphEdgeList<>();
        nombresParadas.clear();
        coordenadasParadas.clear();
        rutasResaltadas.clear();
        rutasConTransbordo.clear();
        inicializarPanel();
    }


    // --- MÉTODOS DEPRECATED (Para compatibilidad) ---
    @Deprecated public static AdaptadorVisual getInstancia() { return getInstance(); }
    @Deprecated public void setPanelVisual(PanelVisualizacion p) { setVisualizationPanel(p); }
    @Deprecated public PanelVisualizacion getPanelVisual() { return getVisualizationPanel(); }
    @Deprecated public GestorDB getGestorDB() { return getDatabaseManager(); }

    public void refrescarVisualizacion() {
        if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
    }

}