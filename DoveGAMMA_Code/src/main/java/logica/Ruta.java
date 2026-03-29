package logica;
/*
 * Clase: Ruta
 * Objetivo: Representar una conexión directa (arista o tramo) entre dos paradas (nodos)
 * dentro de la red de transporte (Grafo).
 * Actúa como el contenedor de los "pesos" para este
 * trayecto específico. Estos datos son los que usan los algoritmos de
 * búsqueda (Dijkstra, BFS, Bellman-Ford) para calcular las rutas óptimas.
 * */
public class Ruta {
    private String idOrigen;
    private String idDestino;
    private double tiempo;
    private double costo;
    private double distancia;
    private int transbordo;

    public Ruta(String idOrigen, String idDestino, double tiempo, double costo, double distancia, int transbordo) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.distancia = distancia;
        this.transbordo = transbordo;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public int getTransbordo() {
        return transbordo;
    }

    public void setTransbordo(int transbordo) {
        this.transbordo = transbordo;
    }

    public String getIdDestino() {
        return idDestino;
    }

    public double getTiempo() {
        return tiempo;
    }

    public double getDistancia() {
        return distancia;
    }

    public double getCosto() {
        return costo;
    }

    public String getIdOrigen() { return idOrigen; }
}