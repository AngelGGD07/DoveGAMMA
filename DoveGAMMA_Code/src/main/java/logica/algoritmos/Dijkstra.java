package logica.algoritmos;

import logica.DatoCamino;
import logica.GrafoTransporte;
import logica.Ruta;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.ArrayList;

/*
 Clase: Dijkstra
 Objetivo: Implementar el algoritmo de Dijkstra para encontrar la ruta más corta
 en grafos con pesos no negativos. Es el algoritmo principal para optimizar
 TIEMPO y DISTANCIA.
 */
public class Dijkstra implements AlgoritmoRuta{
    /*
       Función: calcularRuta
       Argumentos: (GrafoTransporte) grafo, (String) idInicio, (String) idFinal,
                   (CriterioOptimizacion) criterio
       Objetivo: Ejecutar la búsqueda del camino mínimo. Utiliza una PriorityQueue
                 para mantener los nodos ordenados por costo acumulado,
       Retorno: (List<String>): Lista de paradas del camino más corto.
    */
    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        // La Cola de Prioridad usa la lógica de comparación de 'DatoCamino'
        PriorityQueue<DatoCamino> cola = new PriorityQueue<>();
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();

        Set<String> todasLasParadas = grafo.obtenerIdsParadas();
        for (String idParada : todasLasParadas) {
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }

        distanciasMinimas.put(idInicio, 0.0);
        cola.add(new DatoCamino(idInicio, 0.0));

        //Bucle principal de exploración
        while (!cola.isEmpty()) {
            // Extraemos el nodo con el menor peso acumulado
            DatoCamino actual = cola.poll();

            if (actual.getIdParada().equals(idFinal)) {
                break;
            }

            //Revisamos a los vecinos del nodo actual
            List<Ruta> vecinos = grafo.obtenerVecinos(actual.getIdParada());
            for (Ruta rutaVecina : vecinos) {
                // determinarpeso es heredado de la interfaz
                double pesoArista = determinarPeso(rutaVecina, criterio);
                double nuevaDistancia = distanciasMinimas.get(actual.getIdParada()) + pesoArista;

                // Si encontramos un camino más corto hacia el vecino, actualizamos
                if (nuevaDistancia < distanciasMinimas.get(rutaVecina.getIdDestino())) {
                    distanciasMinimas.put(rutaVecina.getIdDestino(), nuevaDistancia);
                    paradasPrevias.put(rutaVecina.getIdDestino(), actual.getIdParada());
                    cola.add(new DatoCamino(rutaVecina.getIdDestino(), nuevaDistancia));
                }
            }
        }

        if (distanciasMinimas.get(idFinal) == Double.MAX_VALUE) {
            return new ArrayList<>();
        }

        // También hereda reconstruirCamino
        return reconstruirCamino(idFinal, paradasPrevias);
    }
}
