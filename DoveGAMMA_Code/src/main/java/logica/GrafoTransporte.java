package logica;

import java.util.*;
/**
 * Estructura de datos principal del sistema. Representa la red de transporte
 * utilizando un modelo de Grafo Dirigido basado en Listas de Adyacencia.
 */
public class GrafoTransporte {
    private HashMap<String, Parada> mapaParadas;

    private HashMap<String, List<Ruta>> listasAdyacencia;

    public GrafoTransporte() {
        this.mapaParadas = new HashMap<>();
        this.listasAdyacencia = new HashMap<>();
    }
    public Set<String> obtenerIdsParadas() {
        return mapaParadas.keySet();
    }

    public List<Ruta> obtenerVecinos(String idParada) {
        return listasAdyacencia.getOrDefault(idParada, new ArrayList<>());
    }

    public boolean registrarParada(Parada nuevaParada) {
        if (!mapaParadas.containsKey(nuevaParada.getCodigo())) {
            mapaParadas.put(nuevaParada.getCodigo(), nuevaParada);
            listasAdyacencia.put(nuevaParada.getCodigo(), new ArrayList<>());
            return true;
        }
        return false;
    }

    public boolean agregarRuta(String idOrigen, String idDestino, double tiempo, double costo, double dist) {
        if (mapaParadas.containsKey(idOrigen) && mapaParadas.containsKey(idDestino)) {
            Ruta nuevaRuta = new Ruta(idOrigen, idDestino, tiempo, costo, dist);
            listasAdyacencia.get(idOrigen).add(nuevaRuta);
            return true;
        }
        return false;
    }

    public boolean eliminarParada(String idParada) {
        if(mapaParadas.containsKey(idParada)){

            mapaParadas.remove(idParada);
            listasAdyacencia.remove(idParada);
            for(List<Ruta> rutasDeOtraParada: listasAdyacencia.values()){

                rutasDeOtraParada.removeIf(ruta -> ruta.getIdDestino().equals(idParada));
            }
            return true;
        }
        return false;
    }
    public boolean eliminarRuta(String idOrigen, String idDestino){

        if(listasAdyacencia.containsKey(idOrigen)){
            List<Ruta> rutasDelOrigen = listasAdyacencia.get(idOrigen);

            return rutasDelOrigen.removeIf(ruta -> ruta.getIdDestino().equals(idDestino));
        }
        return false;
    }

    public boolean modificarParada(String id, String nuevoNombre, double nuevaX, double nuevaY) {
        if (mapaParadas.containsKey(id)) {
            // Extraemos el objeto Parada original
            Parada parada = mapaParadas.get(id);

            // Actualizamos los atributos
            parada.setNombre(nuevoNombre);
            parada.setX(nuevaX);
            parada.setY(nuevaY);

            // Avisamos que fue un exito
            return true;
        }
        return false;
    }

    public boolean modificarRuta(String idOrigen, String idDestino,
                              double tiempo, double costo, double dist) {
        // Intentamos borrar la ruta vieja. Si devuelve true, es que sí existía.
        boolean sePudoEliminar = eliminarRuta(idOrigen, idDestino);

        // Si la borramos con éxito, creamos la nueva con los datos frescos
        if (sePudoEliminar) {
            return agregarRuta(idOrigen, idDestino, tiempo, costo, dist);
        }
        // Si no se pudo eliminar (porque no existía), devolvemos false
        return false;
    }

    public List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (List<Ruta> lista : listasAdyacencia.values()) {
            todas.addAll(lista);
        }
        return todas;
    }


}