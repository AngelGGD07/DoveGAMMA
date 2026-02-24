// AdaptadorVisual.java - Puente entre tu UI y cualquier backend
import javafx.application.Platform;

public class AdaptadorVisual {
    private static AdaptadorVisual instancia;
    private InterfazGrafo backend;
    private PanelVisualizacion panelVisual;

    private AdaptadorVisual() {}

    public static AdaptadorVisual getInstancia() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void setBackend(InterfazGrafo backend) {
        this.backend = backend;
    }

    public void setPanelVisual(PanelVisualizacion panel) {
        this.panelVisual = panel;
        sincronizarConBackend();
    }

    // Llamado cuando el backend agrega una parada
    public void notificarNuevaParada(String id, String nombre, double x, double y) {
        if (panelVisual != null) {
            Platform.runLater(() -> panelVisual.agregarParadaVisual(id, nombre, x, y));
        }
    }

    // Llamado cuando el backend agrega una ruta
    public void notificarNuevaRuta(String idOrigen, String idDestino, double tiempo, double distancia) {
        if (panelVisual != null) {
            Platform.runLater(() -> panelVisual.agregarRutaVisual(idOrigen, idDestino, tiempo, distancia));
        }
    }

    private void sincronizarConBackend() {
        if (backend == null || panelVisual == null) return;

        // Sincronizar paradas existentes
        for (Object p : backend.obtenerParadas()) {
            // Usar reflection o casting según la clase del backend
            panelVisual.agregarParadaVisualDesdeObjeto(p);
        }

        // Sincronizar rutas existentes
        for (Object r : backend.obtenerRutas()) {
            panelVisual.agregarRutaVisualDesdeObjeto(r);
        }
    }

    public InterfazGrafo getBackend() { return backend; }
}