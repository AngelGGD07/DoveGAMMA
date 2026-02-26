// AdaptadorVisual.java
package grafica;

import javafx.application.Platform;
import logica.GrafoTransporte;
import logica.Parada;

import java.util.HashMap;
import java.util.List;

public class AdaptadorVisual {
    private static AdaptadorVisual instancia;
    private GrafoTransporte backend;
    private PanelVisualizacion panelVisual;

    // Coordenadas visuales (la clase Parada no las tiene)
    private HashMap<String, double[]> coordenadasVisuales = new HashMap<>();

    // Nombres por ID — para mostrar resultados legibles
    private HashMap<String, String> nombresPorId = new HashMap<>();

    private AdaptadorVisual() {}

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(GrafoTransporte backend) { this.backend = backend; }
    public void setPanelVisual(PanelVisualizacion panel) { this.panelVisual = panel; }
    public GrafoTransporte getBackend() { return backend; }

    public String getNombre(String id) {
        return nombresPorId.getOrDefault(id, id);
    }

    // AGREGAR PARADA
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (backend == null) return false;
        backend.registrarParada(new Parada(id, nombre));
        coordenadasVisuales.put(id, new double[]{x, y});
        nombresPorId.put(id, nombre);
        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarParadaVisual(id, nombre, x, y));
        return true;
    }

    // AGREGAR RUTA
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (backend == null) return false;
        backend.agregarRuta(origen, destino, tiempo, costo, distancia);
        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia, costo));
        return true;
    }

    // ELIMINAR PARADA
    public boolean eliminarParada(String id) {
        if (backend == null || !nombresPorId.containsKey(id)) return false;
        backend.eliminarParada(id);
        coordenadasVisuales.remove(id);
        nombresPorId.remove(id);
        return true;
    }

    // ELIMINAR RUTA
    public boolean eliminarRuta(String origen, String destino) {
        if (backend == null) return false;
        backend.eliminarRuta(origen, destino);
        return true;
    }

    // CALCULAR Y FORMATEAR RESULTADO
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        if (backend == null) return "Backend no conectado.";

        List<String> camino = backend.calcularDijkstra(idInicio, idFin, criterio);

        if (camino.isEmpty()) {
            return "X  No existe ruta entre " + getNombre(idInicio) + " y " + getNombre(idFin) + ".";
        }

        String tituloCriterio;
        switch (criterio.toLowerCase()) {
            case "tiempo":      tituloCriterio = "Menor Tiempo";       break;
            case "distancia":   tituloCriterio = "Menor Distancia";    break;
            case "costo":       tituloCriterio = "Menor Costo";        break;
            case "transbordos": tituloCriterio = "Menos Transbordos";  break;
            default:            tituloCriterio = criterio;
        }

        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            linea.append(getNombre(camino.get(i)));
            if (i < camino.size() - 1) linea.append(" -> ");
        }

        return "=== " + tituloCriterio + " ===\n" +
                linea + "\n" +
                "Paradas: " + camino.size() + "  |  Saltos: " + (camino.size() - 1);
    }

    // LIMPIAR TODO (boton header)
    public void limpiarTodo() {
        coordenadasVisuales.clear();
        nombresPorId.clear();
        if (panelVisual != null) panelVisual.limpiarTodo();
        backend = new GrafoTransporte();
    }
}