package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/*
 * Implementación del algoritmo de Bellman-Ford.
 * Capaz de calcular la ruta óptima incluso con pesos negativos (ej. descuentos).
 */
public class BellmanFord implements AlgoritmoRuta {

    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();
        Set<String> todasLasParadas = grafo.obtenerIdsParadas();

        // Si el nodo de inicio no existe, abortamos
        if (!todasLasParadas.contains(idInicio)) {
            return new ArrayList<>();
        }

      //Inicializamos todas las paradas con distancia "Infinito"
        for (String idParada : todasLasParadas) {
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }
        // El punto de partida tiene distancia 0
        distanciasMinimas.put(idInicio, 0.0);

        List<Ruta> todasLasRutas = grafo.obtenerTodasLasRutas();
        int cantidadParadas = todasLasParadas.size();

       //Relajación de aristas: El núcleo matemático (V - 1 veces)
        for (int i = 1; i < cantidadParadas; i++) {
            for (Ruta ruta : todasLasRutas) {
                String origen = ruta.getIdOrigen();
                String destino = ruta.getIdDestino();

                // Usamos el metodo heredado de la interfaz
                double peso = determinarPeso(ruta, criterio);

                // Si conocemos cómo llegar al origen, y el salto al destino mejora la distancia...
                if (distanciasMinimas.get(origen) != Double.MAX_VALUE &&
                        distanciasMinimas.get(origen) + peso < distanciasMinimas.get(destino)) {

                    distanciasMinimas.put(destino, distanciasMinimas.get(origen) + peso);
                    paradasPrevias.put(destino, origen);
                }
            }
        }

        // 3. Verificación de ciclos negativos (El superpoder de Bellman-Ford)
        // Hacemos una iteración extra. Si aún podemos mejorar un camino, hay un bucle infinito.
        for (Ruta ruta : todasLasRutas) {
            String origen = ruta.getIdOrigen();
            String destino = ruta.getIdDestino();
            double peso = determinarPeso(ruta, criterio);

            if (distanciasMinimas.get(origen) != Double.MAX_VALUE &&
                    distanciasMinimas.get(origen) + peso < distanciasMinimas.get(destino)) {

                System.out.println("¡Alerta! Se detectó un ciclo de peso negativo en la red.");
                return new ArrayList<>(); // Retornamos vacío porque no hay ruta óptima válida
            }
        }

        // Si el destino sigue en infinito, significa que está desconectado y no hay camino posible
        if (distanciasMinimas.get(idFinal) == null || distanciasMinimas.get(idFinal) == Double.MAX_VALUE) {
            return new ArrayList<>();
        }

        // 4. Reconstruimos el camino hacia atrás usando el metodo heredado
        return reconstruirCamino(idFinal, paradasPrevias);
    }
}