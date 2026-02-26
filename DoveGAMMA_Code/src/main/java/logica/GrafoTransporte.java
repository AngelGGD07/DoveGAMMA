package logica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Collections;

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
        // si el hashmap de paradas contiene el id de la parada que buscamos, elimina esa parada del hashmap y de la lista
        if(mapaParadas.containsKey(idParada)){

            mapaParadas.remove(idParada);
            listasAdyacencia.remove(idParada);
            for(List<Ruta> rutasDeOtraParada: listasAdyacencia.values()){
                /*se recorre las listas de rutas para eliminar de esas rutas
                y borra las rutas que su destino era la parada que se elimina
                */
                rutasDeOtraParada.removeIf(ruta -> ruta.getIdDestino().equals(idParada));
            }
        }
    }
    public void eliminarRuta(String idOrigen, String idDestino){
        // se verifica que la parada de origen exista
        if(listasAdyacencia.containsKey(idOrigen)){
            List<Ruta> rutasDelOrigen = listasAdyacencia.get(idOrigen); // lista de las rutas que salen de ese origen

            rutasDelOrigen.removeIf(ruta -> ruta.getIdDestino().equals(idDestino)); /* se borra la ruta
            que va a ese destino*/
        }
    }

    // dentro de la clase GrafoTransporte
    public List<String> calcularDijkstra(String idInicio, String idFinal, String criterio) {
        // criterio puede ser "tiempo" o "distancia"

        // cola de prioridad para procesar nodos
        PriorityQueue<DatoCamino> cola = new PriorityQueue<>();

        // mapas para guardar distancias minimas y paradas previas (para reconstruir el camino)
        HashMap<String, Double> distanciasMinimas = new HashMap<>();
        HashMap<String, String> paradasPrevias = new HashMap<>();

        for(String idParada: mapaParadas.keySet()){
            distanciasMinimas.put(idParada, Double.MAX_VALUE);
        }
        distanciasMinimas.put(idInicio, 0.0);
        cola.add(new DatoCamino(idInicio,0.0));
        while(!cola.isEmpty()){
            // saca el nodo con menor peso
            DatoCamino actual = cola.poll();
            if(actual.idParada.equals(idFinal)){
                break;
            }
            // recorre sus vecinos (rutas) usando listasAdyacencia
            List<Ruta> vecinos = listasAdyacencia.getOrDefault(actual.idParada, new ArrayList<>());
            for(Ruta rutaVecina: vecinos){
                double pesoArista = 0.0;
                if(criterio.equalsIgnoreCase("tiempo")){
                    pesoArista = rutaVecina.getTiempo();
                } else if (criterio.equalsIgnoreCase("costo")) {
                    pesoArista = rutaVecina.getCosto();

                } else if (criterio.equalsIgnoreCase("distancia")) {
                    pesoArista = rutaVecina.getDistancia();

                }
                double nuevaDistancia = distanciasMinimas.get(actual.idParada) + pesoArista;

                if (nuevaDistancia < distanciasMinimas.get(rutaVecina.getIdDestino())) {
                    distanciasMinimas.put(rutaVecina.getIdDestino(), nuevaDistancia);
                    paradasPrevias.put(rutaVecina.getIdDestino(), actual.idParada);
                    cola.add(new DatoCamino(rutaVecina.getIdDestino(), nuevaDistancia));
                }
            }
        }
        if (distanciasMinimas.get(idFinal) == Double.MAX_VALUE) {
            return new ArrayList<>();
        }
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

// estructura auxiliar para que dijkstra guarde datos temporales
class DatoCamino implements Comparable<DatoCamino> {
    String idParada;
    double pesoAcumulado;

    public DatoCamino(String idParada, double pesoAcumulado) {
        this.idParada = idParada;
        this.pesoAcumulado = pesoAcumulado;
    }

    // necesario para que la PriorityQueue sepa cual es menor
    @Override
    public int compareTo(DatoCamino otro) {
        return Double.compare(this.pesoAcumulado, otro.pesoAcumulado);
    }
}