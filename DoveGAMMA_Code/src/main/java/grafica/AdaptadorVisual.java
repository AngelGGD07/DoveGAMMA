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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdaptadorVisual {

    private static AdaptadorVisual instancia;

    private GrafoTransporte         logicaGrafo;
    private GestorDB                gestorBaseDatos;
    private Digraph<String, String> grafoVisual;
    private PanelVisualizacion      panelVisualizacion;

    private final Map<String, String>   nombresParadas     = new HashMap<>();
    private final Map<String, double[]> coordenadasParadas = new HashMap<>();
    private final Set<String>           rutasResaltadas    = new HashSet<>();
    private final Map<String, Integer>  cantidadTransbordo = new HashMap<>();

    private AdaptadorVisual() {
        this.gestorBaseDatos = new GestorDB();
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
    public GestorDB           getDatabaseManager()    { return gestorBaseDatos; }

    public Map<String, String> getNombresParadas() {
        return Collections.unmodifiableMap(nombresParadas);
    }

    public double[] getCoordenadas(String id) {
        return coordenadasParadas.getOrDefault(id, new double[]{0.0, 0.0});
    }

    /*
       Función: getCantidadTransbordo
       Argumentos: (String) idArista: el id de la arista origen-destino
       Objetivo: Devolver cuántos transbordos tiene esa ruta
       Retorno: (int): la cantidad de transbordos, 0 si no tiene
    */
    public int getCantidadTransbordo(String idArista) {
        return cantidadTransbordo.getOrDefault(idArista, 0);
    }

    /*
       Función: agregarParada
       Argumentos: (String) id: código único de la parada,
                   (String) nombre: nombre visible,
                   (double) x: posición horizontal en el grafo,
                   (double) y: posición vertical en el grafo
       Objetivo: Registrar una nueva parada en lógica, grafo visual y BD
       Retorno: (boolean): true si se agregó, false si ya existía ese id
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
       Argumentos: (String) id: código de la parada a modificar,
                   (String) nuevoNombre: el nombre que va a reemplazar al viejo
       Objetivo: Actualizar el nombre de una parada en lógica y en el mapa local
       Retorno: (boolean): true si existía y se actualizó, false si no encontró el id
    */
    public boolean modificarNombreParada(String id, String nuevoNombre) {
        if (!nombresParadas.containsKey(id)) return false;

        logicaGrafo.modificarParada(id, nuevoNombre, 0, 0);
        nombresParadas.put(id, nuevoNombre);
        return true;
    }

    /*
       Función: eliminarParada
       Argumentos: (String) id: código de la parada a borrar
       Objetivo: Eliminar la parada de lógica, grafo visual, mapas y BD
       Retorno: (boolean): true si se eliminó, false si no existía
    */
    public boolean eliminarParada(String id) {
        if (!nombresParadas.containsKey(id)) return false;

        if (logicaGrafo.eliminarParada(id)) {
            grafoVisual.removeVertex(
                    grafoVisual.vertices().stream()
                            .filter(v -> v.element().equals(id))
                            .findFirst().get()
            );
            nombresParadas.remove(id);
            coordenadasParadas.remove(id);
            gestorBaseDatos.eliminarParada(id);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: agregarRuta
       Argumentos: (String) origen: id de la parada de inicio,
                   (String) destino: id de la parada de llegada,
                   (double) tiempo: minutos del trayecto,
                   (double) distancia: kilómetros del trayecto,
                   (double) costo: precio del trayecto,
                   (int) transbordo: cantidad de transbordos necesarios
       Objetivo: Delegar al método principal que registra la ruta completa
       Retorno: (boolean): true si se creó la ruta
    */
    public boolean agregarRuta(String origen, String destino,
                               double tiempo, double distancia, double costo, int transbordo) {
        return agregarRutaConTransbordo(origen, destino, tiempo, distancia, costo, transbordo);
    }

    /*
       Función: agregarRutaConTransbordo
       Argumentos: (String) origen: id parada inicio,
                   (String) destino: id parada llegada,
                   (double) tiempo: minutos,
                   (double) distancia: kilómetros,
                   (double) costo: precio,
                   (int) transbordo: cantidad de transbordos
       Objetivo: Registrar la ruta en lógica, grafo visual, mapa de transbordos y BD
       Retorno: (boolean): true si se agregó correctamente
    */
    public boolean agregarRutaConTransbordo(String origen, String destino,
                                            double tiempo, double distancia, double costo,
                                            int transbordo) {
        if (!nombresParadas.containsKey(origen) || !nombresParadas.containsKey(destino)) return false;

        if (logicaGrafo.agregarRuta(origen, destino, tiempo, costo, distancia, transbordo)) {
            String idArista = origen + "-" + destino;
            grafoVisual.insertEdge(origen, destino, idArista);
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo, transbordo);

            if (transbordo > 0) cantidadTransbordo.put(idArista, transbordo);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: modificarRutaConTransbordo
       Argumentos: (String) origen: id parada inicio,
                   (String) destino: id parada llegada,
                   (double) tiempo: nuevo tiempo,
                   (double) distancia: nueva distancia,
                   (double) costo: nuevo costo,
                   (int) transbordo: nueva cantidad de transbordos
       Objetivo: Actualizar los datos de una ruta existente en lógica, BD y mapa de transbordos
       Retorno: (boolean): true si se modificó, false si no existía
    */
    public boolean modificarRutaConTransbordo(String origen, String destino,
                                              double tiempo, double distancia, double costo,
                                              int transbordo) {
        if (logicaGrafo.modificarRuta(origen, destino, tiempo, costo, distancia, transbordo)) {
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo, transbordo);

            String idArista = origen + "-" + destino;
            if (transbordo > 0) {
                cantidadTransbordo.put(idArista, transbordo);
            } else {
                cantidadTransbordo.remove(idArista);
            }
            return true;
        }
        return false;
    }

    /*
       Función: eliminarRuta
       Argumentos: (String) origen: id parada inicio,
                   (String) destino: id parada llegada
       Objetivo: Borrar la ruta de lógica, grafo visual, mapa de transbordos y BD
       Retorno: (boolean): true si se eliminó
    */
    public boolean eliminarRuta(String origen, String destino) {
        if (logicaGrafo.eliminarRuta(origen, destino)) {
            String idArista = origen + "-" + destino;
            grafoVisual.edges().stream()
                    .filter(e -> e.element().equals(idArista))
                    .findFirst()
                    .ifPresent(edge -> grafoVisual.removeEdge(edge));

            cantidadTransbordo.remove(idArista);
            gestorBaseDatos.eliminarRuta(origen, destino);

            if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
            return true;
        }
        return false;
    }

    /*
       Función: calcularRuta
       Argumentos: (String) idInicio: id de la parada de partida,
                   (String) idFin: id de la parada destino,
                   (String) criterio: criterio de optimización en texto
       Objetivo: Calcular la ruta óptima y construir el texto del resultado
       Retorno: (String): texto con el recorrido y el resumen del viaje
    */
    public String calcularRuta(String idInicio, String idFin, String criterio) {
        CalculadorRuta calculador = new CalculadorRuta();

        CriterioOptimizacion enumCriterio = CriterioOptimizacion.valueOf(criterio.toUpperCase());
        List<String> camino = calculador.calcular(logicaGrafo, idInicio, idFin, enumCriterio);

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
                String siguiente = camino.get(i + 1);
                String idArista  = actual + "-" + siguiente;
                rutasResaltadas.add(idArista);

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
            for (Ruta r : logicaGrafo.obtenerVecinos(origen)) {
                if (r.getIdDestino().equals(destino)) {
                    return r.getTiempo() + " min | " + r.getDistancia() + " km | $" + r.getCosto();
                }
            }
        }
        return "Datos no disponibles";
    }

    public void limpiarTodo() {
        logicaGrafo    = new GrafoTransporte();
        grafoVisual    = new DigraphEdgeList<>();
        nombresParadas.clear();
        coordenadasParadas.clear();
        rutasResaltadas.clear();
        cantidadTransbordo.clear();
        inicializarPanel();
    }

    public void refrescarVisualizacion() {
        if (panelVisualizacion != null) panelVisualizacion.actualizarGrafico();
    }
}