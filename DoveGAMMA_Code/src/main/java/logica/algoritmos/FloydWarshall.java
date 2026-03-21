package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Implementación del algoritmo de Floyd-Warshall.
 * Calcula las distancias más cortas entre todos los pares de paradas.
 */
public class FloydWarshall implements AlgoritmoRuta {

    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        // 1. Obtener todas las paradas y mapearlas a índices numéricos para la matriz
        List<String> paradas = new ArrayList<>(grafo.obtenerIdsParadas());
        int cantidadNodos = paradas.size();

        // Si el origen o destino no existen, no hay ruta posible
        if (!paradas.contains(idInicio) || !paradas.contains(idFinal)) {
            return new ArrayList<>();
        }

        HashMap<String, Integer> indicePorId = new HashMap<>();
        for (int i = 0; i < cantidadNodos; i++) {
            indicePorId.put(paradas.get(i), i);
        }

        // 2. Crear las matrices de distancias (dist) y de reconstrucción de camino (next)
        double[][] dist = new double[cantidadNodos][cantidadNodos];
        int[][] next = new int[cantidadNodos][cantidadNodos];

        // Inicializamos las matrices con "Infinito" y -1
        for (int i = 0; i < cantidadNodos; i++) {
            for (int j = 0; j < cantidadNodos; j++) {
                if (i == j) {
                    dist[i][j] = 0.0; // Distancia hacia sí mismo es 0
                } else {
                    dist[i][j] = Double.MAX_VALUE;
                }
                next[i][j] = -1;
            }
        }

        // 3. Llenar la matriz con las rutas que ya existen directamente en el grafo
        for (Ruta ruta : grafo.obtenerTodasLasRutas()) {
            int origenIdx = indicePorId.get(ruta.getIdOrigen());
            int destinoIdx = indicePorId.get(ruta.getIdDestino());

            // Usamos el metodo heredado de la interfaz
            dist[origenIdx][destinoIdx] = determinarPeso(ruta, criterio);
            next[origenIdx][destinoIdx] = destinoIdx;
        }

        // 4. El corazón de Floyd-Warshall: Los 3 bucles anidados
        for (int k = 0; k < cantidadNodos; k++) {
            for (int i = 0; i < cantidadNodos; i++) {
                for (int j = 0; j < cantidadNodos; j++) {
                    // Si el atajo pasando por 'k' es mejor que el camino directo...
                    if (dist[i][k] != Double.MAX_VALUE && dist[k][j] != Double.MAX_VALUE &&
                            dist[i][k] + dist[k][j] < dist[i][j]) {

                        dist[i][j] = dist[i][k] + dist[k][j]; // Actualizamos la mejor distancia
                        next[i][j] = next[i][k];              // Guardamos el atajo para reconstruir luego
                    }
                }
            }
        }

        // 5. Extraer el camino específico que el usuario pidió (idInicio a idFinal)
        int startIdx = indicePorId.get(idInicio);
        int endIdx = indicePorId.get(idFinal);

        // Si sigue siendo -1, significa que no existe ninguna ruta que conecte esos dos puntos
        if (next[startIdx][endIdx] == -1) {
            return new ArrayList<>();
        }

        // Reconstruimos el camino saltando de nodo en nodo
        List<String> caminoFinal = new ArrayList<>();
        caminoFinal.add(idInicio);
        while (startIdx != endIdx) {
            startIdx = next[startIdx][endIdx];
            caminoFinal.add(paradas.get(startIdx));
        }

        return caminoFinal;
    }
}