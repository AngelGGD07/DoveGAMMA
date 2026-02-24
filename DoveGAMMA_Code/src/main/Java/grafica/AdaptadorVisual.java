// AdaptadorVisual.java - Traduce la UI a la lógica de tu compañero
package grafica;

import javafx.application.Platform;
import logica.GrafoTransporte;
import logica.Parada;
import java.util.HashMap;
import java.util.List;

public class AdaptadorVisual {
    private static AdaptadorVisual instancia;
    // Ahora apuntamos directamente a la clase de tu compañero
    private GrafoTransporte backend;
    private PanelVisualizacion panelVisual;

    // GUARDAMOS LAS COORDENADAS AQUÍ
    // Como su clase Parada no tiene X e Y, la UI las recordará localmente.
    private HashMap<String, double[]> coordenadasVisuales = new HashMap<>();

    private AdaptadorVisual() {}

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(GrafoTransporte backend) {
        this.backend = backend;
    }

    public void setPanelVisual(PanelVisualizacion panel) {
        this.panelVisual = panel;
    }

    public GrafoTransporte getBackend() {
        return backend;
    }

    public void notificarNuevaRuta(String idOrigen, String idDestino, double tiempo, double distancia) {
        if (panelVisual != null) {
            Platform.runLater(() -> panelVisual.agregarRutaVisual(idOrigen, idDestino, tiempo, distancia));
        }
    }

    // --- ADAPTACIÓN A SU LÓGICA DE PARADAS ---
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (backend != null) {
            // 1. Usas su constructor exacto: Parada(codigo, nombre)
            Parada nuevaParada = new Parada(id, nombre);

            // 2. Llamas a su método exacto: registrarParada(Parada)
            backend.registrarParada(nuevaParada);

            // 3. Guardas las X, Y en la interfaz gráfica
            coordenadasVisuales.put(id, new double[]{x, y});

            // 4. Dibujar en pantalla
            if (panelVisual != null) {
                Platform.runLater(() -> panelVisual.agregarParadaVisual(id, nombre, x, y));
            }
            return true;
        }
        return false;
    }

    // --- ADAPTACIÓN A SU LÓGICA DE RUTAS ---
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia) {
        if (backend != null) {
            // Su método pide: idOrigen, idDestino, tiempo, costo, dist
            // Le pasamos 0.0 en costo por defecto para no afectar lo que él hizo
            backend.agregarRuta(origen, destino, tiempo, 0.0, distancia);

            if (panelVisual != null) {
                Platform.runLater(() -> panelVisual.agregarRutaVisual(origen, destino, tiempo, distancia));
            }
            return true;
        }
        return false;
    }

    // --- PUENTE PARA EL DIJKSTRA ---
    public List<String> calcularRutaOptima(String inicio, String fin, String criterio) {
        if (backend != null) {
            // Llamamos al método del backend (ajustado según la lógica del compañero)
            return backend.obtenerRutaDijkstra(inicio, fin, criterio);
        }
        return null;
    }
}
