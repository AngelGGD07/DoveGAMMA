package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;
import java.util.*;

/*
 Clase: BFS
 Objetivo: Explorar el grafo por niveles de proximidad.
 A diferencia de Dijkstra, BFS asume que todas las aristas tienen el mismo peso.
 */
public class BFS implements AlgoritmoRuta {

    /*
       Función: calcularRuta
       Argumentos: (GrafoTransporte) grafo: la red de transporte.
                   (String) idInicio: parada de salida.
                   (String) idFinal: parada de llegada.
                   (CriterioOptimizacion) criterio: factor de optimización (Transbordos).
       Objetivo: Ejecutar el recorrido en anchura. Utiliza una Cola para encolar
                 a los vecinos inmediatos nivel por nivel.
       Retorno: (List<String>): Lista de paradas que componen el camino con menos
                transbordos, o una lista vacía si no existe conexión.
    */
    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        Queue<String> cola = new LinkedList<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        // Validar existencia del inicio
        if (!grafo.obtenerIdsParadas().contains(idInicio)) return new ArrayList<>();

        //Agregamos el origen a la cola y lo marcamos como visitado
        cola.add(idInicio);
        visitados.add(idInicio);

        while (!cola.isEmpty()) {
            // Extraemos el primer nodo en la fila
            String actual = cola.poll();

            // Si llegamos al destino, reconstruimos y terminamos
            if (actual.equals(idFinal)) {
                return reconstruirCamino(idFinal, paradasPrevias);
            }

            // Explorar todos los vecinos inmediatos
            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                String vecino = ruta.getIdDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    paradasPrevias.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return new ArrayList<>();
    }
}