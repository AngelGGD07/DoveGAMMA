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

    // coordenadas visuales de cada parada
    private HashMap<String, double[]> coordenadasVisuales = new HashMap<>();

    // id -> nombre (para mostrar resultados legibles)
    private HashMap<String, String> nombresPorId = new HashMap<>();

    private AdaptadorVisual() {}

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(GrafoTransporte backend) { this.backend = backend; }
    public void setPanelVisual(PanelVisualizacion panel) { this.panelVisual = panel; }
    public GrafoTransporte getBackend() { return backend; }
    public PanelVisualizacion getPanelVisual() { return panelVisual; }

    public String getNombre(String id) {
        return nombresPorId.getOrDefault(id, id);
    }

    // AGREGAR PARADA - valida que no exista el ID ni el nombre
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (backend == null) return false;

        // no se puede repetir el ID
        if (nombresPorId.containsKey(id)) return false;

        // no se puede repetir el nombre tampoco
        if (nombresPorId.containsValue(nombre)) return false;

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

        // las dos paradas tienen que existir
        if (!nombresPorId.containsKey(origen) || !nombresPorId.containsKey(destino)) return false;

        backend.agregarRuta(origen, destino, tiempo, costo, distancia);

        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia, costo));
        return true;
    }

    // MODIFICAR PARADA - cambia el nombre en el visual y en los maps
    // OJO al socio: necesitas agregar modificarParada(id, nombre) en GrafoTransporte
    public boolean modificarParada(String id, String nuevoNombre) {
        if (backend == null || !nombresPorId.containsKey(id)) return false;

        // no puedes poner un nombre que ya tiene otra parada
        if (nombresPorId.containsValue(nuevoNombre) && !nombresPorId.get(id).equals(nuevoNombre))
            return false;

        backend.modificarParada(id, nuevoNombre); // el socio implementa esto
        nombresPorId.put(id, nuevoNombre);

        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.actualizarNombreParada(id, nuevoNombre));
        return true;
    }

    // MODIFICAR RUTA - elimina la vieja y agrega una nueva con los nuevos valores
    // OJO al socio: necesitas agregar modificarRuta() en GrafoTransporte
    public boolean modificarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (backend == null) return false;
        if (!nombresPorId.containsKey(origen) || !nombresPorId.containsKey(destino)) return false;

        backend.modificarRuta(origen, destino, tiempo, costo, distancia); // el socio implementa esto

        // visual: borra la linea vieja y pone la nueva
        if (panelVisual != null) {
            Platform.runLater(() -> {
                panelVisual.eliminarRutaVisual(origen, destino);
                panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia, costo);
            });
        }
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

    // ELIMINAR RUTA - solo borra la ruta, las paradas se quedan
    public boolean eliminarRuta(String origen, String destino) {
        if (backend == null) return false;
        if (!nombresPorId.containsKey(origen) || !nombresPorId.containsKey(destino)) return false;

        backend.eliminarRuta(origen, destino);

        // solo borra la linea visual, las paradas quedan intactas
        if (panelVisual != null)
            Platform.runLater(() -> panelVisual.eliminarRutaVisual(origen, destino));
        return true;
    }

    // redibuja todo el grafo desde cero (se usa después de eliminar paradas)
    public void redibujarGrafoCompleto() {
        if (panelVisual == null) return;

        // guardamos una copia de los datos antes de limpiar la pantalla
        HashMap<String, double[]> copiaCoords = new HashMap<>(coordenadasVisuales);
        HashMap<String, String> copiaNames = new HashMap<>(nombresPorId);

        // le pedimos al grafo las rutas activas
        List<logica.Ruta> rutasActivas = backend.obtenerTodasLasRutas(); // socio agrega esto

        Platform.runLater(() -> {
            panelVisual.limpiarTodo();
            // redibujar paradas
            copiaCoords.forEach((id, coords) -> {
                String nombre = copiaNames.getOrDefault(id, id);
                panelVisual.agregarParadaVisual(id, nombre, coords[0], coords[1]);
            });
            // redibujar rutas
            for (logica.Ruta r : rutasActivas) {
                panelVisual.agregarRutaVisual(
                        r.getIdOrigen(), r.getIdDestino(),
                        r.getTiempo(), r.getDistancia(), r.getCosto()
                );
            }
        });
    }

    // CALCULAR Y FORMATEAR RESULTADO
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        if (backend == null) return "Backend no conectado.";

        List<String> camino = backend.calcularDijkstra(idInicio, idFin, criterio);

        if (camino.isEmpty()) {
            return "✕  No existe ruta entre " + getNombre(idInicio) + " y " + getNombre(idFin) + ".";
        }

        String tituloCriterio;
        switch (criterio.toLowerCase()) {
            case "tiempo":      tituloCriterio = "Menor Tiempo";      break;
            case "distancia":   tituloCriterio = "Menor Distancia";   break;
            case "costo":       tituloCriterio = "Menor Costo";       break;
            case "transbordos": tituloCriterio = "Menos Transbordos"; break;
            default:            tituloCriterio = criterio;
        }

        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < camino.size(); i++) {
            linea.append(getNombre(camino.get(i)));
            if (i < camino.size() - 1) linea.append(" → ");
        }

        return "=== " + tituloCriterio + " ===\n" +
                linea + "\n" +
                "Paradas: " + camino.size() + "  |  Saltos: " + (camino.size() - 1);
    }

    // LIMPIAR TODO
    public void limpiarTodo() {
        coordenadasVisuales.clear();
        nombresPorId.clear();
        if (panelVisual != null) panelVisual.limpiarTodo();
        backend = new GrafoTransporte();
    }
}