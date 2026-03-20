package grafica;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import logica.CalculadorRuta;
import logica.GrafoTransporte;
import logica.Parada;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdaptadorVisual {

    private static AdaptadorVisual instancia;

    private GrafoTransporte logicaGrafo;
    private logica.GestorDB gestorBaseDatos;

    private Digraph<String, String> grafoVisual;
    private PanelVisualizacion panelVisualizacion;

    private final Map<String, String> nombresParadas = new HashMap<>();
    private final Set<String> rutasResaltadas = new HashSet<>();

    /*
       Función: AdaptadorVisual
       Argumentos: Ninguno
       Objetivo: Inicializar las instancias de la lógica, base de datos y el grafo visual vacío.
       Retorno: (void): Constructor privado para patrón Singleton.
    */
    private AdaptadorVisual() {
        this.gestorBaseDatos = new logica.GestorDB();
        this.logicaGrafo = new GrafoTransporte();
        this.grafoVisual = new DigraphEdgeList<>();
    }

    /*
       Función: getInstance
       Argumentos: Ninguno
       Objetivo: Obtener la única instancia global del adaptador (Patrón Singleton).
       Retorno: (AdaptadorVisual): Retorna la instancia actual para mantener el estado sincronizado en todo el programa.
    */
    public static AdaptadorVisual getInstance() {
        if (instancia == null) {
            instancia = new AdaptadorVisual();
        }
        return instancia;
    }

    /*
       Función: inicializarPanel
       Argumentos: Ninguno
       Objetivo: Crear el objeto PanelVisualizacion enlazándolo al grafo visual actual.
       Retorno: (void): Solo inicializa un componente.
    */
    public void inicializarPanel() {
        this.panelVisualizacion = new PanelVisualizacion(grafoVisual);
    }

    /*
       Función: getVisualizationPanel
       Argumentos: Ninguno
       Objetivo: Proveer acceso al panel gráfico configurado.
       Retorno: (PanelVisualizacion): El panel que se incrustará en la interfaz gráfica.
    */
    public PanelVisualizacion getVisualizationPanel() {
        return panelVisualizacion;
    }

    /*
       Función: getBackend
       Argumentos: Ninguno
       Objetivo: Proveer acceso al grafo de la lógica.
       Retorno: (GrafoTransporte): La instancia del gestor de rutas lógico.
    */
    public GrafoTransporte getBackend() {
        return logicaGrafo;
    }

    /*
       Función: getDatabaseManager
       Argumentos: Ninguno
       Objetivo: Proveer acceso al gestor de base de datos.
       Retorno: (GestorDB): La instancia que maneja las consultas SQL.
    */
    public logica.GestorDB getDatabaseManager() {
        return gestorBaseDatos;
    }

    /*
       Función: agregarParada
       Argumentos:
             (String) id: Identificador único de la parada.
             (String) nombre: Nombre descriptivo de la parada.
             (double) x: Coordenada en el eje horizontal.
             (double) y: Coordenada en el eje vertical.
       Objetivo: Registrar una nueva parada en la lógica, la vista y la base de datos simultáneamente.
       Retorno: (boolean): Retorna true si se agregó correctamente, false si el ID ya existía.
    */
    public boolean agregarParada(String id, String nombre, double x, double y) {
        if (nombresParadas.containsKey(id)) return false;

        boolean agregadoLogica = logicaGrafo.registrarParada(new Parada(id, nombre, x, y));

        if (agregadoLogica) {
            grafoVisual.insertVertex(id);
            nombresParadas.put(id, nombre);
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
       Argumentos:
             (String) id: Identificador de la parada a modificar.
             (String) nuevoNombre: El nuevo nombre que recibirá.
       Objetivo: Cambiar el nombre de una parada existente en la caché local y la lógica.
       Retorno: (boolean): Retorna true si se modificó, false si la parada no existe.
    */
    public boolean modificarNombreParada(String id, String nuevoNombre) {
        if (!nombresParadas.containsKey(id)) return false;

        logicaGrafo.modificarParada(id, nuevoNombre, 0, 0);
        nombresParadas.put(id, nuevoNombre);

        return true;
    }

    /*
       Función: eliminarParada
       Argumentos: (String) id: El identificador de la parada a eliminar.
       Objetivo: Borrar completamente una parada de todas las capas (Lógica, BD y Vista).
       Retorno: (boolean): Retorna true si se eliminó con éxito, false si no se encontró.
    */
    public boolean eliminarParada(String id) {
        if (!nombresParadas.containsKey(id)) return false;

        if (logicaGrafo.eliminarParada(id)) {
            grafoVisual.removeVertex(grafoVisual.vertices().stream().filter(v -> v.element().equals(id)).findFirst().get());
            nombresParadas.remove(id);
            gestorBaseDatos.eliminarParada(id);

            if (panelVisualizacion != null) {
                panelVisualizacion.actualizarGrafico();
            }
            return true;
        }
        return false;
    }

    /*
       Función: agregarRuta
       Argumentos:
             (String) origen: ID de la parada de inicio.
             (String) destino: ID de la parada de destino.
             (double) tiempo: Tiempo estimado de viaje.
             (double) distancia: Distancia física entre los puntos.
             (double) costo: Valor monetario del trayecto.
       Objetivo: Crear una conexión dirigida entre dos nodos en todas las capas.
       Retorno: (boolean): Retorna true si se enlazaron con éxito, false si alguna parada no existe.
    */
    public boolean agregarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (!nombresParadas.containsKey(origen) || !nombresParadas.containsKey(destino)) return false;

        if (logicaGrafo.agregarRuta(origen, destino, tiempo, costo, distancia)) {
            String idArista = origen + "-" + destino;

            grafoVisual.insertEdge(origen, destino, idArista);
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo);

            if (panelVisualizacion != null) {
                panelVisualizacion.actualizarGrafico();
            }
            return true;
        }
        return false;
    }

    /*
       Función: modificarRuta
       Argumentos:
             (String) origen: ID de la parada de inicio.
             (String) destino: ID de la parada de destino.
             (double) tiempo: Nuevo tiempo.
             (double) distancia: Nueva distancia.
             (double) costo: Nuevo costo.
       Objetivo: Actualizar los pesos o valores de una arista ya existente.
       Retorno: (boolean): Retorna true si la ruta existía y fue modificada, false en caso contrario.
    */
    public boolean modificarRuta(String origen, String destino, double tiempo, double distancia, double costo) {
        if (logicaGrafo.modificarRuta(origen, destino, tiempo, costo, distancia)) {
            gestorBaseDatos.guardarRuta(origen, destino, tiempo, distancia, costo);
            return true;
        }
        return false;
    }

    /*
       Función: eliminarRuta
       Argumentos:
             (String) origen: ID de la parada de inicio.
             (String) destino: ID de la parada de destino.
       Objetivo: Destruir la conexión entre dos paradas en la base de datos, lógica y vista.
       Retorno: (boolean): Retorna true si se eliminó correctamente, false si no existía.
    */
    public boolean eliminarRuta(String origen, String destino) {
        if (logicaGrafo.eliminarRuta(origen, destino)) {
            String idArista = origen + "-" + destino;
            grafoVisual.edges().stream()
                    .filter(e -> e.element().equals(idArista))
                    .findFirst()
                    .ifPresent(edge -> grafoVisual.removeEdge(edge));

            gestorBaseDatos.eliminarRuta(origen, destino);

            if (panelVisualizacion != null) {
                panelVisualizacion.actualizarGrafico();
            }
            return true;
        }
        return false;
    }

    /*
       Función: calcularRuta
       Argumentos:
             (String) idInicio: ID del nodo de inicio.
             (String) idFin: ID del nodo destino.
             (String) criterio: Criterio a evaluar (tiempo, costo, distancia).
       Objetivo: Calcular la ruta con Dijkstra, activar las aristas visuales, sumar los costos y armar el reporte.
       Retorno: (String): Un texto enriquecido con el resumen del viaje para el panel de resultados.
    */
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
                String siguiente = camino.get(i + 1);
                String idArista = actual + "-" + siguiente;

                rutasResaltadas.add(idArista);

                for (logica.Ruta r : logicaGrafo.obtenerVecinos(actual)) {
                    if (r.getIdDestino().equals(siguiente)) {
                        totalTiempo += r.getTiempo();
                        totalDistancia += r.getDistancia();
                        totalCosto += r.getCosto();
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

        if (panelVisualizacion != null) {
            panelVisualizacion.actualizarGrafico();
        }

        return sb.toString();
    }

    /*
       Función: getStopName
       Argumentos: (String) id: Identificador de la parada a consultar.
       Objetivo: Devolver el nombre legible de una parada buscando en la caché.
       Retorno: (String): El nombre de la parada o el mismo ID si no tiene nombre registrado.
    */
    public String getStopName(String id) {
        return nombresParadas.getOrDefault(id, id);
    }

    /*
       Función: getEdgeDataAsString
       Argumentos: (String) idArista: el identificador de la arista en formato "Origen-Destino".
       Objetivo: Proveer texto numérico a la vista gráfica ÚNICAMENTE si la ruta forma parte de un cálculo activo.
       Retorno: (String): Texto formateado o cadena vacía para ocultarlo y evitar saturación visual.
    */
    public String getEdgeDataAsString(String idArista) {
        if (!rutasResaltadas.contains(idArista)) {
            return "";
        }
        return getDetallesRuta(idArista);
    }

    /*
       Función: getDetallesRuta
       Argumentos: (String) idArista: el identificador de la arista.
       Objetivo: Buscar de forma obligatoria los datos físicos de una ruta para mostrarlos en pop-ups cuando el usuario haga clic.
       Retorno: (String): Texto completo con tiempo, distancia y costo.
    */
    public String getDetallesRuta(String idArista) {
        String[] partes = idArista.split("-");
        if (partes.length == 2) {
            String origen = partes[0];
            String destino = partes[1];
            for (logica.Ruta r : logicaGrafo.obtenerVecinos(origen)) {
                if (r.getIdDestino().equals(destino)) {
                    return r.getTiempo() + " min | " + r.getDistancia() + " km | $" + r.getCosto();
                }
            }
        }
        return "Datos no disponibles";
    }

    /*
       Función: limpiarTodo
       Argumentos: Ninguno
       Objetivo: Reiniciar todas las estructuras de datos, limpiar la pantalla gráfica y listas de caché.
       Retorno: (void): No retorna datos.
    */
    public void limpiarTodo() {
        logicaGrafo = new GrafoTransporte();
        grafoVisual = new DigraphEdgeList<>();
        nombresParadas.clear();
        rutasResaltadas.clear();
        inicializarPanel();
    }

    /*
       Función: refrescarVisualizacion
       Argumentos: Ninguno
       Objetivo: Forzar una recarga gráfica desde el controlador externo si es necesario.
       Retorno: (void): No retorna datos.
    */
    public void refrescarVisualizacion() {
        if (panelVisualizacion != null) {
            panelVisualizacion.actualizarGrafico();
        }
    }
}