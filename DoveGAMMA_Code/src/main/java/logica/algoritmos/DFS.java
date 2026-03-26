package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;
import java.util.*;

/*
 * Búsqueda en Profundidad (DFS).
 * Explora un camino hasta el final antes de retroceder.
 * No garantiza la ruta más corta, pero sirve para evaluar conectividad.
 */
public class DFS implements AlgoritmoRuta {

    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        Stack<String> pila = new Stack<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        if (!grafo.obtenerIdsParadas().contains(idInicio)) return new ArrayList<>();

        pila.push(idInicio);

        while (!pila.isEmpty()) {
            String actual = pila.pop();

            if (!visitados.contains(actual)) {
                visitados.add(actual);

                // Si encontramos el destino, retornamos el camino formado
                if (actual.equals(idFinal)) {
                    return reconstruirCamino(idFinal, paradasPrevias);
                }

                // Agregamos vecinos a la pila para explorarlos en profundidad
                for (Ruta ruta : grafo.obtenerVecinos(actual)) {
                    String vecino = ruta.getIdDestino();
                    if (!visitados.contains(vecino)) {
                        // Guardamos de dónde vinimos solo la primera vez que lo descubrimos
                        paradasPrevias.putIfAbsent(vecino, actual);
                        pila.push(vecino);
                    }
                }
            }
        }
        return new ArrayList<>(); // No hay camino
    }
}