package logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class CalculadorRuta {

    // Este método ya no está en el grafo, así que le pedimos el grafo por parámetro
    public List<String> calcularDijkstra(GrafoTransporte grafo, String idInicio, String idFinal, String criterio) {

        PriorityQueue<DatoCamino> cola = new PriorityQueue<>();
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();

        // 1. Inicializamos todas las distancias en "Infinito" (Double.MAX_VALUE)
        Set<String> todasLasParadas = grafo.obtenerIdsParadas();
        for(String idParada : todasLasParadas){
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }

        // 2. El punto de inicio tiene distancia 0
        distanciasMinimas.put(idInicio, 0.0);
        cola.add(new DatoCamino(idInicio, 0.0));

        // 3. Comenzamos a recorrer
        while(!cola.isEmpty()){
            // ¡Aquí sacamos el nodo con la menor distancia acumulada!
            DatoCamino actual = cola.poll();

            // Si ya sacamos de la cola nuestro destino final, terminamos de buscar
            if(actual.idParada.equals(idFinal)){
                break;
            }

            // 4. Revisamos los vecinos pidiéndole la lista al grafo
            List<Ruta> vecinos = grafo.obtenerVecinos(actual.idParada);

            for(Ruta rutaVecina : vecinos){
                double pesoArista = determinarPeso(rutaVecina, criterio);
                double nuevaDistancia = distanciasMinimas.get(actual.idParada) + pesoArista;

                // 5. Relajación: Si encontramos un atajo más corto, actualizamos los datos
                if (nuevaDistancia < distanciasMinimas.get(rutaVecina.getIdDestino())) {
                    distanciasMinimas.put(rutaVecina.getIdDestino(), nuevaDistancia);
                    paradasPrevias.put(rutaVecina.getIdDestino(), actual.idParada);
                    // Agregamos el vecino a la cola para explorarlo más adelante
                    cola.add(new DatoCamino(rutaVecina.getIdDestino(), nuevaDistancia));
                }
            }
        }

        // Si el destino sigue en infinito, significa que no hay camino posible
        if (distanciasMinimas.get(idFinal) == Double.MAX_VALUE) {
            return new ArrayList<>();
        }

        // 6. Reconstruir el camino desde el final hacia el principio
        return reconstruirCamino(idFinal, paradasPrevias);
    }

    // --- MÉTODOS AUXILIARES PARA LIMPIAR EL CÓDIGO ---

    // Este método reemplaza todos los "if/else" que tenías, haciéndolo más limpio
    private double determinarPeso(Ruta ruta, String criterio) {
        switch (criterio.toLowerCase()) {
            case "tiempo": return ruta.getTiempo();
            case "costo": return ruta.getCosto();
            case "distancia": return ruta.getDistancia();
            case "transbordos": return 1.0;
            default: return ruta.getDistancia();
        }
    }

    // Este método saca el bucle de reconstrucción para que el método principal no sea tan largo
    private List<String> reconstruirCamino(String idFinal, HashMap<String, String> paradasPrevias) {
        List<String> caminoFinal = new ArrayList<>();
        String paradaActual = idFinal;

        while(paradaActual != null){
            caminoFinal.add(paradaActual);
            paradaActual = paradasPrevias.get(paradaActual);
        }
        Collections.reverse(caminoFinal);
        return caminoFinal;
    }
}