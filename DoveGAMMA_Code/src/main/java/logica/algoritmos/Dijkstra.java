package logica.algoritmos;

import logica.DatoCamino;
import logica.GrafoTransporte;
import logica.Ruta;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.ArrayList;

public class Dijkstra implements AlgoritmoRuta{
    @Override
    public List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio) {
        PriorityQueue<DatoCamino> cola = new PriorityQueue<>();
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();

        Set<String> todasLasParadas = grafo.obtenerIdsParadas();
        for (String idParada : todasLasParadas) {
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }

        distanciasMinimas.put(idInicio, 0.0);
        cola.add(new DatoCamino(idInicio, 0.0));

        while (!cola.isEmpty()) {
            DatoCamino actual = cola.poll();

            if (actual.getIdParada().equals(idFinal)) {
                break;
            }

            List<Ruta> vecinos = grafo.obtenerVecinos(actual.getIdParada());
            for (Ruta rutaVecina : vecinos) {
                // determinarpeso es heredado de la interfaz
                double pesoArista = determinarPeso(rutaVecina, criterio);
                double nuevaDistancia = distanciasMinimas.get(actual.getIdParada()) + pesoArista;

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
