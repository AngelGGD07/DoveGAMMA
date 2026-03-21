package logica;

import logica.algoritmos.AlgoritmoRuta;
import logica.algoritmos.Dijkstra;
import logica.algoritmos.CriterioOptim.CriterioOptimizacion;

import java.util.ArrayList;
import java.util.List;

/*
 *
 * Administra los algoritmos de búsqueda y delega el cálculo matemático a la estrategia seleccionada.
 */
public class CalculadorRuta {

    // Aquí guardamos el algoritmo que se está usando en el momento
    private AlgoritmoRuta algoritmoActual;

    /*
     * Constructor por defecto. Si nadie nos dice nada, usamos Dijkstra.
     */
    public CalculadorRuta() {
        this.algoritmoActual = new Dijkstra();
    }
    /*
     * Otro constructor para flexibilidad y permitir que otra clase pase otro de los algoritmos
     */
    public CalculadorRuta(AlgoritmoRuta algoritmoInicial) {
        this.algoritmoActual = algoritmoInicial;
    }

    /*
     * Permite cambiar el algoritmo en tiempo de ejecución (ej. cambiar a Bellman-Ford si hay descuentos)
     */
    public void setAlgoritmo(AlgoritmoRuta nuevoAlgoritmo) {
        this.algoritmoActual = nuevoAlgoritmo;
    }

    /*
     * Calcula la ruta óptima principal usando la estrategia actual.
     */
    public List<String> calcular(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptimizacion criterio) {
        if (this.algoritmoActual == null) {
            throw new IllegalStateException("¡Error! No se ha definido un algoritmo de búsqueda.");
        }

        // El "Gerente" simplemente le pasa el trabajo al algoritmo seleccionado
        return this.algoritmoActual.calcularRuta(grafo, idInicio, idFinal, criterio);
    }

    /*
     * Calcula una "Ruta Alternativa" (Plan B) bloqueando temporalmente
     * el primer tramo de la ruta óptima original.
     */
    public List<String> calcularRutaAlternativa(GrafoTransporte grafo, String idInicio, String idFinal, CriterioOptimizacion criterio) {
        // 1. Calculamos la ruta óptima original (El Plan A)
        List<String> rutaOriginal = calcular(grafo, idInicio, idFinal, criterio);

        // Si no hay ruta original o es un viaje de una sola parada, no hay alternativa posible
        if (rutaOriginal.isEmpty() || rutaOriginal.size() < 2) {
            return new ArrayList<>();
        }

        // 2. Identificamos el primer tramo para bloquearlo (de la parada 0 a la parada 1)
        String nodoA = rutaOriginal.get(0);
        String nodoB = rutaOriginal.get(1);

        // Buscamos la ruta exacta en el grafo para guardar sus datos antes de borrarla
        Ruta rutaABloquear = null;
        for (Ruta r : grafo.obtenerVecinos(nodoA)) {
            if (r.getIdDestino().equals(nodoB)) {
                rutaABloquear = r;
                break;
            }
        }

        // Si por alguna razón no la encuentra, abortamos
        if (rutaABloquear == null) return new ArrayList<>();

        // 3. ¡Bloqueamos la calle temporalmente!
        grafo.eliminarRuta(nodoA, nodoB);

        // 4. Recalculamos forzando al algoritmo a buscar el "Plan B"
        List<String> rutaAlternativa = calcular(grafo, idInicio, idFinal, criterio);

        // 5. Restauramos la calle para no dañar el grafo original de forma permanente
        grafo.agregarRuta(nodoA, nodoB, rutaABloquear.getTiempo(), rutaABloquear.getCosto(), rutaABloquear.getDistancia());

        // 6. Devolvemos el Plan B
        return rutaAlternativa;
    }
}