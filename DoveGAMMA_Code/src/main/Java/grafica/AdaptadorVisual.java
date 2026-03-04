// AdaptadorVisual.java
package grafica;

import javafx.application.Platform;
import logica.GrafoTransporte;
import logica.Parada;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AdaptadorVisual {
    private static AdaptadorVisual instancia;
    private GrafoTransporte    backend;
    private PanelVisualizacion panelVisual;
    private logica.GestorDB gestorDB;

    private HashMap<String, double[]> coordenadasVisuales = new HashMap<>();
    private HashMap<String, String>   nombresPorId        = new HashMap<>();
    private HashSet<String>           rutasExistentes     = new HashSet<>();

    // Guarda datos de cada ruta para poder redibujar: clave "origen|destino", valor [tiempo, dist, costo]
    private HashMap<String, double[]> datosRutas = new HashMap<>();

    private AdaptadorVisual() {
        gestorDB = new logica.GestorDB();
    }

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(GrafoTransporte b)        { this.backend    = b; }
    public void setPanelVisual(PanelVisualizacion p) { this.panelVisual = p; }
    public GrafoTransporte    getBackend()            { return backend;      }
    public PanelVisualizacion getPanelVisual()        { return panelVisual;  }


    // ── AGREGAR PARADA ────────────────────────────────────────────────────────
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (backend == null) return false;

        if (nombresPorId.containsKey(id)) return false;

        for (String n : nombresPorId.values())
            if (n.equalsIgnoreCase(nombre)) return false;

        backend.registrarParada(new Parada(id, nombre));
        coordenadasVisuales.put(id, new double[]{x, y});
        nombresPorId.put(id, nombre);

        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarParadaVisual(id, nombre, x, y));
        gestorDB.guardarParada(id, nombre, x, y);
        return true;
    }

    // ── AGREGAR RUTA ──────────────────────────────────────────────────────────
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (backend == null) return false;
        if (!nombresPorId.containsKey(origen) || !nombresPorId.containsKey(destino)) return false;

        backend.agregarRuta(origen, destino, tiempo, costo, distancia);
        String clave = origen + "|" + destino;
        rutasExistentes.add(clave);
        datosRutas.put(clave, new double[]{tiempo, distancia, costo});

        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia, costo));
        gestorDB.guardarRuta(origen, destino, tiempo, distancia, costo);
        return true;
    }

    // ── MODIFICAR PARADA ──────────────────────────────────────────────────────
    public boolean modificarParada(String id, String nuevoNombre) {
        if (!nombresPorId.containsKey(id)) return false;

        for (java.util.Map.Entry<String, String> e : nombresPorId.entrySet())
            if (!e.getKey().equals(id) && e.getValue().equalsIgnoreCase(nuevoNombre)) return false;

        nombresPorId.put(id, nuevoNombre);
        Platform.runLater(this::redibujarAhora);
        return true;
    }

    // ── MODIFICAR RUTA ────────────────────────────────────────────────────────
    public boolean modificarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        String clave = origen + "|" + destino;
        if (!rutasExistentes.contains(clave)) return false;

        backend.eliminarRuta(origen, destino);
        backend.agregarRuta(origen, destino, tiempo, costo, distancia);
        datosRutas.put(clave, new double[]{tiempo, distancia, costo});
        Platform.runLater(this::redibujarAhora);
        return true;
    }

    // ── ELIMINAR PARADA ───────────────────────────────────────────────────────
    public boolean eliminarParada(String id) {
        if (backend == null || !nombresPorId.containsKey(id)) return false;

        backend.eliminarParada(id);
        coordenadasVisuales.remove(id);
        nombresPorId.remove(id);
        rutasExistentes.removeIf(r -> r.startsWith(id + "|") || r.endsWith("|" + id));
        datosRutas.keySet().removeIf(r -> r.startsWith(id + "|") || r.endsWith("|" + id));
        gestorDB.eliminarParada(id);
        return true;
        // El ControladorPrincipal llama redibujarAhora() justo después de este return
    }

    // ── ELIMINAR RUTA ─────────────────────────────────────────────────────────
    public boolean eliminarRuta(String origen, String destino) {
        if (backend == null) return false;
        String clave = origen + "|" + destino;
        if (!rutasExistentes.contains(clave)) return false;

        backend.eliminarRuta(origen, destino);
        rutasExistentes.remove(clave);
        datosRutas.remove(clave);
        gestorDB.eliminarRuta(origen, destino);
        return true;
        // El ControladorPrincipal llama redibujarAhora() justo después de este return
    }

    // ── REDIBUJAR GRAFO — llamar desde el hilo FX directamente ────────────────
    public void redibujarAhora() {
        if (panelVisual == null) return;

        panelVisual.limpiarTodo(); // borra todo y redibuja el grid

        // Primero los nodos
        for (String id : nombresPorId.keySet()) {
            double[] pos = coordenadasVisuales.get(id);
            if (pos != null)
                panelVisual.agregarParadaVisual(id, nombresPorId.get(id), pos[0], pos[1]);
        }

        // Luego las rutas encima
        for (String clave : rutasExistentes) {
            String[] partes = clave.split("\\|");
            double[] datos  = datosRutas.get(clave);
            if (partes.length == 2 && datos != null)
                panelVisual.agregarRutaVisual(partes[0], partes[1], datos[0], datos[1], datos[2]);
        }
    }

    // ── CALCULAR Y FORMATEAR RESULTADO ────────────────────────────────────────
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        if (backend == null) return "Backend no conectado.";

        List<String> camino = backend.calcularDijkstra(idInicio, idFin, criterio);

        if (camino.isEmpty())
            return "No existe ruta entre " + getNombre(idInicio) + " y " + getNombre(idFin) + ".";

        String titulo;
        switch (criterio.toLowerCase()) {
            case "tiempo":      titulo = "Menor Tiempo";      break;
            case "distancia":   titulo = "Menor Distancia";   break;
            case "costo":       titulo = "Menor Costo";       break;
            case "transbordos": titulo = "Menos Transbordos"; break;
            default:            titulo = criterio;
        }

        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            linea.append(getNombre(camino.get(i)));
            if (i < camino.size() - 1) linea.append(" -> ");
        }

        return "=== " + titulo + " ===\n" +
                linea + "\n" +
                "Paradas: " + camino.size() + "  |  Saltos: " + (camino.size() - 1);
    }

    // ── LIMPIAR TODO ──────────────────────────────────────────────────────────
    public void limpiarTodo() {
        coordenadasVisuales.clear();
        nombresPorId.clear();
        rutasExistentes.clear();
        datosRutas.clear();
        if (panelVisual != null) panelVisual.limpiarTodo();
        backend = new GrafoTransporte();
    }

    public String getNombre(String id) {
        return nombresPorId.getOrDefault(id, id);
    }
    public logica.GestorDB getGestorDB() { return gestorDB; }
}