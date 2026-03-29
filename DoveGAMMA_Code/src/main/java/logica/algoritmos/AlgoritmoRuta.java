package logica.algoritmos;

import logica.GrafoTransporte;
import logica.Ruta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/*
 * Interfaz: AlgoritmoRuta
 * Objetivo: Definir el diseño que los algoritmos de busqueda
 * deben seguir.
 */
public interface AlgoritmoRuta {
    /*
       Función: calcularRuta (Metodo Abstracto)
       Argumentos: (GrafoTransporte) grafo: el mapa completo.
                   (String) idInicio: parada de salida.
                   (String) idFinal: parada de destino.
                   (CriterioOptimizacion) criterio: qué factor priorizar.
       Objetivo: Función principal que cada algoritmo específico (Dijkstra, BFS, etc.)
                 está obligado a implementar con su propia lógica matemática.
       Retorno: (List<String>): El camino óptimo resuelto.
    */
    List<String> calcularRuta(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptim.CriterioOptimizacion criterio);

    // --- MÉTODOS HEREDADOS PARA NO REPETIR CÓDIGO ---

    /*
       Función: determinarPeso (Metodo Default)
       Argumentos: (Ruta) ruta: el tramo a evaluar.
                   (CriterioOptimizacion) criterio: el filtro aplicado por el usuario.
       Objetivo: Analizar un tramo y devolver el valor numérico (peso) correcto
                 dependiendo de lo que el usuario quiera optimizar.
       Retorno: (double): El valor numérico a sumar en el algoritmo.
    */
    default double determinarPeso(Ruta ruta, CriterioOptim.CriterioOptimizacion criterio) {
        switch (criterio) {
            case TIEMPO: return ruta.getTiempo();
            case COSTO: return ruta.getCosto();
            case TRANSBORDOS: return ruta.getTransbordo();
            case DISTANCIA:
            default: return ruta.getDistancia();
        }
    }

    /*
       Función: reconstruirCamino (Metodo Default)
       Argumentos: (String) idFinal: la parada de destino donde terminó la búsqueda.
                   (HashMap<String, String>) paradasPrevias: el historial
                   dejado por el algoritmo para saber de dónde vino cada nodo.
       Objetivo: Rastrear el camino desde el destino hacia atrás hasta llegar al
                 origen, y luego invertir la lista para entregarla en el orden correcto
                 (Origen -> Destino). Compartido por todos los algoritmos.
       Retorno: (List<String>): La ruta ordenada de principio a fin.
    */
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
