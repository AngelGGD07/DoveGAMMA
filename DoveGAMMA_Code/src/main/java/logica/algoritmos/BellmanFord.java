package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/*
 Clase: BellmanFord
 Objetivo: Implementar el algoritmo de ruta más corta de Bellman-Ford.
 A diferencia de Dijkstra, este algoritmo es capaz de procesar grafos que
 contienen aristas con pesos negativos.
 */
public class BellmanFord implements AlgoritmoRuta {

    /*
       Función: calcularRuta
       Argumentos: (GrafoTransporte) grafo: red de transporte completa.
                   (String) idInicio: parada de salida.
                   (String) idFinal: parada de destino.
                   (CriterioOptimizacion) criterio: factor a optimizar (el costo)
       Objetivo: Calcular la ruta de menor costo. Funciona mediante la relajación
                 de todas las aristas del grafo repetidas (V - 1) veces, asegurando
                 que se encuentre el costo mínimo absoluto. Posteriormente, realiza
                 una iteración final para detectar ciclos de peso negativo que
                 invalidadarían cualquier resultado.
       Retorno: (List<String>): Lista ordenada de paradas del camino óptimo,
                o lista vacía si hay un ciclo negativo o no existe conexión.
    */
    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();
        Set<String> todasLasParadas = grafo.obtenerIdsParadas();

        // Si el nodo de inicio no existe, abortamos
        if (!todasLasParadas.contains(idInicio)) {
            return new ArrayList<>();
        }

      //Inicializamos todas las paradas
        for (String idParada : todasLasParadas) {
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }
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


                if (distanciasMinimas.get(origen) != Double.MAX_VALUE &&
                        distanciasMinimas.get(origen) + peso < distanciasMinimas.get(destino)) {

                    distanciasMinimas.put(destino, distanciasMinimas.get(origen) + peso);
                    paradasPrevias.put(destino, origen);
                }
            }
        }

        // Verificación de ciclos negativos
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

        // Si el destino sigue en infinito quiere decir que está desconectado
        if (distanciasMinimas.get(idFinal) == null || distanciasMinimas.get(idFinal) == Double.MAX_VALUE) {
            return new ArrayList<>();
        }

        // Reconstruimos el camino hacia atrás usando el metodo heredado
        return reconstruirCamino(idFinal, paradasPrevias);
    }
}