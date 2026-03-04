// AdaptadorVisual.java
package grafica;

import javafx.application.Platform;
import logica.GrafoTransporte;
import logica.Parada;
import logica.Ruta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AdaptadorVisual {
    private static AdaptadorVisual instancia;
    private GrafoTransporte backend;
    private PanelVisualizacion panelVisual;

    private HashMap<String, double[]> coordenadasVisuales = new HashMap<>();
    private HashMap<String, String>   nombresPorId        = new HashMap<>();
    private HashSet<String>           rutasExistentes     = new HashSet<>();

    private AdaptadorVisual() {}

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(GrafoTransporte backend)       { this.backend = backend;     }
    public void setPanelVisual(PanelVisualizacion panel)  { this.panelVisual = panel;   }
    public GrafoTransporte    getBackend()                { return backend;             }
    public PanelVisualizacion getPanelVisual()            { return panelVisual;         }

    public String getNombre(String id) {
        return nombresPorId.getOrDefault(id, id);
    }

    // ── AGREGAR PARADA ────────────────────────────────────────────────────────
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (backend == null) return false;

        // ID duplicado
        if (nombresPorId.containsKey(id)) return false;

        // Nombre duplicado (case-insensitive)
        for (String n : nombresPorId.values())
            if (n.equalsIgnoreCase(nombre)) return false;

        backend.registrarParada(new Parada(id, nombre));
        coordenadasVisuales.put(id, new double[]{x, y});
        nombresPorId.put(id, nombre);
        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarParadaVisual(id, nombre, x, y));
        return true;
    }

    // ── AGREGAR RUTA ──────────────────────────────────────────────────────────
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (backend == null) return false;
        if (!nombresPorId.containsKey(origen) || !nombresPorId.containsKey(destino)) return false;

        backend.agregarRuta(origen, destino, tiempo, costo, distancia);
        rutasExistentes.add(origen + "|" + destino);
        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia, costo));
        return true;
    }

    // ── MODIFICAR PARADA ──────────────────────────────────────────────────────
    public boolean modificarParada(String id, String nuevoNombre) {
        if (!nombresPorId.containsKey(id)) return false;

        // No dejar poner un nombre que ya tiene otra parada
        for (java.util.Map.Entry<String, String> entry : nombresPorId.entrySet()) {
            if (!entry.getKey().equals(id) && entry.getValue().equalsIgnoreCase(nuevoNombre))
                return false;
        }

        // Actualizar el nombre en el mapa local
        // Nota: la clase Parada no tiene setNombre, así que hay que pedirle al compañero
        // que lo agregue — ver NOTA_PARA_COMPAÑERO.md
        nombresPorId.put(id, nuevoNombre);
        return true;
    }

    // ── MODIFICAR RUTA ────────────────────────────────────────────────────────
    public boolean modificarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (!rutasExistentes.contains(origen + "|" + destino)) return false;

        // Eliminar la ruta vieja y crear una nueva con los valores actualizados
        backend.eliminarRuta(origen, destino);
        backend.agregarRuta(origen, destino, tiempo, costo, distancia);
        return true;
    }

    // ── ELIMINAR PARADA ───────────────────────────────────────────────────────
    public boolean eliminarParada(String id) {
        if (backend == null || !nombresPorId.containsKey(id)) return false;

        backend.eliminarParada(id);
        coordenadasVisuales.remove(id);
        nombresPorId.remove(id);
        // Limpiar rutas que salían o llegaban a esa parada
        rutasExistentes.removeIf(r -> r.startsWith(id + "|") || r.endsWith("|" + id));
        return true;
    }

    // ── ELIMINAR RUTA ─────────────────────────────────────────────────────────
    public boolean eliminarRuta(String origen, String destino) {
        if (backend == null) return false;
        if (!rutasExistentes.contains(origen + "|" + destino)) return false;

        backend.eliminarRuta(origen, destino);
        rutasExistentes.remove(origen + "|" + destino);
        return true;
    }

    // ── CALCULAR Y FORMATEAR RESULTADO ────────────────────────────────────────
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        if (backend == null) return "Backend no conectado.";

        List<String> camino = backend.calcularDijkstra(idInicio, idFin, criterio);

        if (camino.isEmpty())
            return "X  No existe ruta entre " + getNombre(idInicio) + " y " + getNombre(idFin) + ".";

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
        if (panelVisual != null) panelVisual.limpiarTodo();
        backend = new GrafoTransporte();
    }
}