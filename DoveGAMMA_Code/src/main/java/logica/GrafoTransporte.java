package logica;

import java.util.*;
/*
 Clase: GrafoTransporte
 Objetivo: Actuar como el núcleo estructural del sistema.
 Implementa la estructura de datos de Grafo Dirigido utilizando Listas de Adyacencia
 (mediante HashMaps) para optimizar el consumo de memoria en redes de transporte.
 */
public class GrafoTransporte {
    private HashMap<String, Parada> mapaParadas;

    private HashMap<String, List<Ruta>> listasAdyacencia;

    public GrafoTransporte() {
        this.mapaParadas = new HashMap<>();
        this.listasAdyacencia = new HashMap<>();
    }
    /*
       Función: obtenerIdsParadas
       Argumentos: ninguno
       Objetivo: Retornar un conjunto (Set) con todos los identificadores (códigos)
                 de las paradas registradas en el grafo. Útil para iterar sobre los nodos.
       Retorno: (Set<String>): Conjunto de IDs de las paradas.
    */
    public Set<String> obtenerIdsParadas() {
        return mapaParadas.keySet();
    }

    /*
       Función: obtenerVecinos
       Argumentos: (String) idParada: código de la parada a consultar.
       Objetivo: Obtener la lista de todas las aristas (rutas) que salen directamente
                 de una parada específica. Implementa la búsqueda O(1) en la lista de adyacencia.
       Retorno: (List<Ruta>): Lista de rutas adyacentes. Devuelve lista vacía si la parada no existe.
    */
    public List<Ruta> obtenerVecinos(String idParada) {
        return listasAdyacencia.getOrDefault(idParada, new ArrayList<>());
    }

    /*
       Función: registrarParada
       Argumentos: (Parada) nuevaParada: objeto Parada a insertar en el grafo.
       Objetivo: Añadir un nuevo vértice (nodo) al grafo inicializando su lista
                 de adyacencia vacía. Evita duplicados verificando si el ID ya existe.
       Retorno: (boolean): true si se insertó con éxito, false si ya existía una parada con ese código.
    */
    public boolean registrarParada(Parada nuevaParada) {
        if (!mapaParadas.containsKey(nuevaParada.getCodigo())) {
            mapaParadas.put(nuevaParada.getCodigo(), nuevaParada);
            listasAdyacencia.put(nuevaParada.getCodigo(), new ArrayList<>());
            return true;
        }
        return false;
    }

    /*
       Función: agregarRuta
       Argumentos: (String) idOrigen, (String) idDestino, (double) tiempo,
                   (double) costo, (double) dist, (int) transbordo
       Objetivo: Crear una nueva arista dirigida ponderada entre dos nodos existentes.
                 Se añade a la lista de adyacencia del nodo origen.
       Retorno: (boolean): true si se insertó correctamente, false si alguna parada no existe.
    */
    public boolean agregarRuta(String idOrigen, String idDestino, double tiempo, double costo, double dist, int transbordo) {
        if (mapaParadas.containsKey(idOrigen) && mapaParadas.containsKey(idDestino)) {
            Ruta nuevaRuta = new Ruta(idOrigen, idDestino, tiempo, costo, dist, transbordo);
            listasAdyacencia.get(idOrigen).add(nuevaRuta);
            return true;
        }
        return false;
    }

    /*
       Función: eliminarParada
       Argumentos: (String) idParada: código de la parada a eliminar.
       Objetivo: Eliminar un vértice del grafo y, para mantener la integridad estructural,
                 eliminar todas las aristas (rutas) entrantes y salientes conectadas a él.
       Retorno: (boolean): true si se eliminó con éxito, false si la parada no existía.
    */
    public boolean eliminarParada(String idParada) {
        if(mapaParadas.containsKey(idParada)){

            mapaParadas.remove(idParada);
            listasAdyacencia.remove(idParada);
            // Iteramos sobre todos los demás nodos para borrar las rutas que apuntaban a este
            for(List<Ruta> rutasDeOtraParada: listasAdyacencia.values()){

                rutasDeOtraParada.removeIf(ruta -> ruta.getIdDestino().equals(idParada));
            }
            return true;
        }
        return false;
    }

    /*
       Función: eliminarRuta
       Argumentos: (String) idOrigen: código de la parada inicial,
                   (String) idDestino: código de la parada final.
       Objetivo: Eliminar una arista específica entre dos nodos, buscándola
                 en la lista de adyacencia del origen y removiéndola.
       Retorno: (boolean): true si se eliminó correctamente, false si no existía la conexión.
    */
    public boolean eliminarRuta(String idOrigen, String idDestino){

        if(listasAdyacencia.containsKey(idOrigen)){
            List<Ruta> rutasDelOrigen = listasAdyacencia.get(idOrigen);

            return rutasDelOrigen.removeIf(ruta -> ruta.getIdDestino().equals(idDestino));
        }
        return false;
    }

    /*
       Función: modificarParada
       Argumentos: (String) id, (String) nuevoNombre, (double) nuevaX, (double) nuevaY
       Objetivo: Actualizar los atributos (nombre y coordenadas espaciales) de un
                 vértice existente sin alterar sus conexiones (aristas).
       Retorno: (boolean): true si se actualizó con éxito, false si la parada no existe.
    */
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

    /*
       Función: modificarRuta
       Argumentos: (String) idOrigen, (String) idDestino, (double) tiempo,
                   (double) costo, (double) dist, (int) transbordo
       Objetivo: Actualizar los pesos y atributos de una arista existente. Lo hace
                 eliminando la anterior y creando una nueva con los datos actualizados.
       Retorno: (boolean): true si se actualizó con éxito, false si la ruta original no existía.
    */
    public boolean modificarRuta(String idOrigen, String idDestino,
                              double tiempo, double costo, double dist, int transbordo) {
        boolean sePudoEliminar = eliminarRuta(idOrigen, idDestino);

        // Si la borramos con éxito, creamos la nueva con los datos frescos
        if (sePudoEliminar) {
            return agregarRuta(idOrigen, idDestino, tiempo, costo, dist, transbordo);
        }
        return false;
    }

    /*
       Función: obtenerTodasLasRutas
       Argumentos: ninguno
       Objetivo: Recopilar todas las aristas del grafo en una sola lista plana.
                 Es fundamental para algoritmos como Bellman-Ford que necesitan iterar
                 sobre todas las conexiones globales de la red.
       Retorno: (List<Ruta>): Lista completa de todas las rutas existentes en el grafo.
    */
    public List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (List<Ruta> lista : listasAdyacencia.values()) {
            todas.addAll(lista);
        }
        return todas;
    }

}