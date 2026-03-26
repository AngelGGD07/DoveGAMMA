package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;
import java.util.*;

/*
 * Búsqueda en Anchura (BFS).
 * Ideal para encontrar la ruta con la menor cantidad de paradas/transbordos
 * porque explora el grafo por "niveles" de proximidad.
 */
public class BFS implements AlgoritmoRuta {

    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        Queue<String> cola = new LinkedList<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        // Validar existencia del inicio
        if (!grafo.obtenerIdsParadas().contains(idInicio)) return new ArrayList<>();

        cola.add(idInicio);
        visitados.add(idInicio);

        while (!cola.isEmpty()) {
            String actual = cola.poll();

            // Si llegamos al destino, reconstruimos y terminamos
            if (actual.equals(idFinal)) {
                return reconstruirCamino(idFinal, paradasPrevias);
            }

            // Explorar todos los vecinos inmediatos (Nivel actual)
            for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                String vecino = ruta.getIdDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    paradasPrevias.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return new ArrayList<>(); // No hay camino
    }
}