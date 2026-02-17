package logica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.List;

public class GrafoTransporte {
    private HashMap<String, Parada> mapaParadas;

    private HashMap<String, List<Ruta>> listasAdyacencia;

    public GrafoTransporte() {
        this.mapaParadas = new HashMap<>();
        this.listasAdyacencia = new HashMap<>();
    }

    public void registrarParada(Parada nuevaParada) {
        if (!mapaParadas.containsKey(nuevaParada.getCodigo())) {
            mapaParadas.put(nuevaParada.getCodigo(), nuevaParada);
            listasAdyacencia.put(nuevaParada.getCodigo(), new ArrayList<>());
        }
    }

    public void agregarRuta(String idOrigen, String idDestino, double tiempo, double costo, double dist) {
        if (mapaParadas.containsKey(idOrigen) && mapaParadas.containsKey(idDestino)) {
            Ruta nuevaRuta = new Ruta(idOrigen, idDestino, tiempo, costo, dist);
            listasAdyacencia.get(idOrigen).add(nuevaRuta);
        }
    }

    public void eliminarParada(String idParada) {
        // ojo aqui: si borras parada, tambien borra las rutas que llegan a ella
    }

    // dentro de la clase GrafoTransporte
    public void calcularDijkstra(String idInicio, String idFinal, String criterio) {
        // criterio puede ser "tiempo" o "distancia"

        // cola de prioridad para procesar nodos
        PriorityQueue<DatoCamino> cola = new PriorityQueue<>();

        // mapas para guardar distancias minimas y paradas previas (para reconstruir el camino)
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();

        // INSTRUCCION:
        // 1. inicializa distanciasMinimas en infinito (Double.MAX_VALUE)
        // 2. pon la distancia del inicio en 0 y metela a la cola
        // 3. ciclo while mientras la cola no este vacia:
        //      - saca el nodo con menor peso
        //      - recorre sus vecinos (rutas) usando listasAdyacencia
        //      - aplica relajacion: si (distancia actual + peso ruta) < distancia vecino guardada:
        //          actualiza y mete a la cola

        // al final imprime el camino recorriendo paradasPrevias desde el final hasta el inicio
    }

}

// estructura auxiliar para que dijkstra guarde datos temporales
class DatoCamino implements Comparable<DatoCamino> {
    String idParada;
    double pesoAcumulado;

    // constructor

    // necesario para que la PriorityQueue sepa cual es menor
    @Override
    public int compareTo(DatoCamino otro) {
        return Double.compare(this.pesoAcumulado, otro.pesoAcumulado);
    }
}