package logica;

import logica.algoritmos.AlgoritmoRuta;
import logica.algoritmos.BFS;
import logica.algoritmos.BellmanFord;
import logica.algoritmos.Dijkstra;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;

import java.util.ArrayList;
import java.util.List;

/*
 * Clase: CalculadorRuta
 * Objetivo: Administra los diferentes algoritmos de búsqueda y delega el cálculo matemático
 * basándose en el criterio seleccionado por el usuario.
 */
public class CalculadorRuta {

    // Aquí guardamos el algoritmo que se está usando en el momento
    private AlgoritmoRuta algoritmoActual;

    public CalculadorRuta() {
        this.algoritmoActual = new Dijkstra();
    }

    /*
       Función: calcular
       Argumentos: (GrafoTransporte) grafo: red de transporte sobre la cual buscar.
                   (String) idInicio: código de la parada de salida.
                   (String) idFinal: código de la parada de destino.
                   (CriterioOptimizacion) criterio: factor a priorizar (Tiempo, Costo, etc.)
       Objetivo: Analizar el criterio seleccionado por el usuario y auto-asignar
                 el algoritmo más eficiente para resolverlo. Luego, delega la
                 ejecución matemática a dicho algoritmo.
       Retorno: (List<String>): Lista ordenada con los IDs de las paradas de la ruta óptima.
    */
    public List<String> calcular(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptimizacion criterio) {


        // Elegimos el algoritmosegún lo que pidió el usuario en la interfaz
        switch (criterio) {
            case TRANSBORDOS:
                this.algoritmoActual = new BFS(); // BFS garantiza la menor cantidad de saltos
                break;
            case COSTO:
                this.algoritmoActual = new BellmanFord(); // Bellman-Ford por si manejan descuentos (pesos negativos)
                break;
            case TIEMPO:
            case DISTANCIA:
            default:
                this.algoritmoActual = new Dijkstra(); // Dijkstra es el más rápido para pesos normales
                break;
        }

        if (this.algoritmoActual == null) {
            throw new IllegalStateException("¡Error! No se ha definido un algoritmo de búsqueda.");
        }

        // Pasamos el trabajo al algoritmo que acaba de seleccionar
        return this.algoritmoActual.calcularRuta(grafo, idInicio, idFinal, criterio);
    }
    /*
       Función: calcularRutaAlternativa
       Argumentos: (GrafoTransporte) grafo, (String) idInicio, (String) idFinal,
                   (CriterioOptimizacion) criterio
       Objetivo: Generar un Plan B para el usuario.
       Retorno: (List<String>): Lista con las paradas de la ruta alternativa, o vacía si no hay opciones.
    */
    public List<String> calcularRutaAlternativa(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptimizacion criterio) {
        // Calculamos la ruta óptima original
        List<String> rutaOriginal = calcular(grafo, idInicio, idFinal, criterio);

        if (rutaOriginal.isEmpty() || rutaOriginal.size() < 2) {
            return new ArrayList<>();
        }

        // Identificamos el primer tramo para bloquearlo
        String nodoA = rutaOriginal.get(0);
        String nodoB = rutaOriginal.get(1);

        Ruta rutaABloquear = null;
        for (Ruta r : grafo.obtenerVecinos(nodoA)) {
            if (r.getIdDestino().equals(nodoB)) {
                rutaABloquear = r;
                break;
            }
        }
        if (rutaABloquear == null) return new ArrayList<>();

        // ¡Bloqueamos la calle temporalmente!
        grafo.eliminarRuta(nodoA, nodoB);

        // Recalculamos forzando al algoritmo a buscar el "Plan B"
        List<String> rutaAlternativa = calcular(grafo, idInicio, idFinal, criterio);

        // Restauramos la calle para no dañar el grafo original de forma permanente
        grafo.agregarRuta(nodoA, nodoB, rutaABloquear.getTiempo(), rutaABloquear.getCosto(), rutaABloquear.getDistancia(), rutaABloquear.getTransbordo());

        // Devolvemos la ruta alternativa
        return rutaAlternativa;
    }
}