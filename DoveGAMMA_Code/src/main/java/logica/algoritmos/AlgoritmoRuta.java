package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/*
 * Define el diseño que todos los algoritmos de búsqueda deben cumplir.
 */
public interface AlgoritmoRuta {
    // El metodo pricipal que cada algoritmo implementara a su manera
    List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio);

    // --- MÉTODOS HEREDADOS PARA NO REPETIR CÓDIGO ---

    default double determinarPeso(Ruta ruta, CriterioOptim.CriterioOptimizacion criterio) {
        switch (criterio) {
            case TIEMPO: return ruta.getTiempo();
            case COSTO: return ruta.getCosto();
            case TRANSBORDOS: return ruta.getTransbordo();
            case DISTANCIA:
            default: return ruta.getDistancia();
        }
    }

    default List<String> reconstruirCamino(String idFinal, HashMap<String, String> paradasPrevias) {
        List<String> caminoFinal = new ArrayList<>();
        String paradaActual = idFinal;

        while (paradaActual != null) {
            caminoFinal.add(paradaActual);
            paradaActual = paradasPrevias.get(paradaActual);
        }
        Collections.reverse(caminoFinal);
        return caminoFinal;
    }
}
