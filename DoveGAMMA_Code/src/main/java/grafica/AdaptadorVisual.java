package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Parada;
import logica.Ruta;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;
import logica.persistencia.GestorDB;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptadorVisual {

    private static AdaptadorVisual instancia;

    private GrafoTransporte         logicaGrafo;
    private GestorDB                gestorBaseDatos;
    private Digraph<String, String> grafoVisual;
    private PanelVisualizacion      panelVisualizacion;

    private final Map<String, String>   nombresParadas     = new HashMap<>();
    private final Map<String, double[]> coordenadasParadas = new HashMap<>();

    private AdaptadorVisual() {
        this.gestorBaseDatos = new GestorDB();
        this.logicaGrafo     = new GrafoTransporte();
        this.grafoVisual     = new DigraphEdgeList<>();
    }

    public static AdaptadorVisual getInstance() {
        if (instancia == null) instancia = new AdaptadorVisual();
        return instancia;
    }

    public void inicializarPanel() {
        this.panelVisualizacion = new PanelVisualizacion(grafoVisual);
    }

    public PanelVisualizacion getVisualizationPanel() { return panelVisualizacion; }
    public GrafoTransporte    getBackend()            { return logicaGrafo; }
    public GestorDB           getDatabaseManager()    { return gestorBaseDatos; }

    public Map<String, String> getNombresParadas() {
        return Collections.unmodifiableMap(nombresParadas);
    }

    public double[] getCoordenadas(String id) {
        return coordenadasParadas.getOrDefault(id, new double[]{0.0, 0.0});
    }

    /*
       Función: agregarParada
       Argumentos: (String) id, (String) nombre, (double) x, (double) y
       Objetivo: Añadir un nuevo vertice y dibujarlo en smartgraph.
       Retorno: (boolean): true si se insertó en todas las capas, false si el ID ya existía.
    */
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (nombresParadas.containsKey(id)) return false;

        boolean ok = logicaGrafo.registrarParada(new Parada(id, nombre, x, y));
        if (ok) {
            grafoVisual.insertVertex(id);
            nombresParadas.put(id, nombre);
            coordenadasParadas.put(id, new double[]{x, y});
            gestorBaseDatos.guardarParada(id, nombre, x, y);

            if (panelVisualizacion != null) {
                panelVisualizacion.actualizarGrafico();
                panelVisualizacion.fijarCoordenadasNodo(id, x, y);
            }
            return true;
        }
        return false;
    }

    /*
       Función: modificarNombreParada
       Argumentos: (String) id, (String) nuevoNombre
       Objetivo: Actualizar el nombre de una parada.
       Retorno: (boolean): true si se modificó correctamente, false si no existe.
    */
    public boolean modificarNombreParada(String id, String nuevoNombre) {
        if (!nombresParadas.containsKey(id)) return false;
        logicaGrafo.modificarParada(id, nuevoNombre, 0, 0);
        nombresParadas.put(id, nuevoNombre);
        return true;
    }

    /*
       Función: eliminarParada
       Argumentos: (String) id
       Objetivo: Eliminar un nodo (parada) del grafo, la interfaz y de la BD.
       Retorno: (boolean): true si la operación fue exitosa.
    */
    public boolean eliminarParada(String id) {
        if (!nombresParadas.containsKey(id)) return false;

        if (logicaGrafo.eliminarParada(id)) {
            grafoVisual.removeVertex(
                    grafoVisual.vertices().stream()
                            .filter(v -> v.element().equals(id))
                            .findFirst().get());
            nombresParadas.remove(id);
            coordenadasParadas.remove(id);
            gestorBaseDatos.eliminarParada(id);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: agregarRutaConTransbordo
       Argumentos: (String) origen: id parada de inicio,
                   (String) destino: id parada de llegada,
                   (double) tiempo: minutos del tramo,
                   (double) distancia: km del tramo,
                   (double) costo: precio del tramo,
                   (int) transbordo: cantidad de transbordos requeridos
       Objetivo: Registrar la ruta en el grafo lógico, en el grafo visual y en la BD
       Retorno: (boolean): true si se agregó, false si las paradas no existen
    */
    public boolean agregarRutaConTransbordo(String origen, String destino,
                                            double tiempo, double distancia, double costo,
                                            int transbordo) {
        if (!nombresParadas.containsKey(origen) || !nombresParadas.containsKey(destino)) return false;

        if (logicaGrafo.agregarRuta(origen, destino, tiempo, costo, distancia, transbordo)) {
            String idArista = origen + "-" + destino;
            grafoVisual.insertEdge(origen, destino, idArista);
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo, transbordo);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: modificarRutaConTransbordo
       Argumentos: (String) origen, (String) destino,
                   (double) tiempo, (double) distancia, (double) costo,
                   (int) transbordo: nuevo valor de cantidad de transbordos
       Objetivo: Actualizar los datos de una ruta existente en lógica y BD
       Retorno: (boolean): true si se modificó OK
    */
    public boolean modificarRutaConTransbordo(String origen, String destino,
                                              double tiempo, double distancia, double costo,
                                              int transbordo) {
        if (logicaGrafo.modificarRuta(origen, destino, tiempo, costo, distancia, transbordo)) {
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo, transbordo);
            return true;
        }
        return false;
    }

    /*
    Función: eliminarRuta
    Argumentos: (String) origen, (String) destino
    Objetivo: Eliminar una ruta de la parte logica, de SmartGraph y de la BD.
    Retorno: (boolean): true si se eliminó correctamente.
            */
    public boolean eliminarRuta(String origen, String destino) {
        if (logicaGrafo.eliminarRuta(origen, destino)) {
            String idArista = origen + "-" + destino;
            grafoVisual.edges().stream()
                    .filter(e -> e.element().equals(idArista))
                    .findFirst()
                    .ifPresent(edge -> grafoVisual.removeEdge(edge));

            gestorBaseDatos.eliminarRuta(origen, destino);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: calcularRuta
       Argumentos: (String) idInicio, (String) idFin, (String) criterio
       Objetivo: Delegar la búsqueda matemática al CalculadorRuta e itera sobre los tramos
                 resultantes para sumar los totales (tiempo, distancia, costo).
       Retorno: (String): Texto formateado con el desglose de la ruta óptima.
    */
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        CalculadorRuta calculador = new CalculadorRuta();
        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio.toUpperCase());
        List<String> camino = calculador.calcular(logicaGrafo, idInicio, idFin, enumCriterio);

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
                String siguiente = camino.get(i + 1);
                for (Ruta r : logicaGrafo.obtenerVecinos(actual)) {
                    if (r.getIdDestino().equals(siguiente)) {
                        totalTiempo    += r.getTiempo();
                        totalDistancia += r.getDistancia();
                        totalCosto     += r.getCosto();
                        sb.append("   |  ").append(r.getTiempo()).append(" min, ")
                                .append(r.getDistancia()).append(" km, $")
                                .append(r.getCosto()).append("\n   v\n");
                        break;
                    }
                }
            }
        }

        sb.append("\n=== Resumen ===\n");
        sb.append("• Tiempo: ").append(totalTiempo).append(" min\n");
        sb.append("• Distancia: ").append(totalDistancia).append(" km\n");
        sb.append("• Costo: $").append(totalCosto).append("\n");
        sb.append("• Tramos: ").append(camino.size() - 1).append("\n");

        if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
        return sb.toString();
    }

    public String getStopName(String id) {
        return nombresParadas.getOrDefault(id, id);
    }

    public void limpiarTodo() {
        logicaGrafo  = new GrafoTransporte();
        grafoVisual  = new DigraphEdgeList<>();
        nombresParadas.clear();
        coordenadasParadas.clear();
        inicializarPanel();
    }

    public void refrescarVisualizacion() {
        if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
    }
}