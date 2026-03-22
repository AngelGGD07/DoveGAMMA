package logica;

import java.util.List;

public class Ruta {
    private String idOrigen;
    private String idDestino;
    private double tiempo;
    private double costo;
    private double distancia;
    private boolean transbordo;

    public Ruta(String idOrigen, String idDestino, double tiempo, double costo, double distancia, boolean transbordo) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.distancia = distancia;
        this.transbordo = transbordo;
    }

    public void setIdOrigen(String idOrigen) {
        this.idOrigen = idOrigen;
    }

    public void setIdDestino(String idDestino) {
        this.idDestino = idDestino;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public boolean isTransbordo() {
        return transbordo;
    }

    public void setTransbordo(boolean transbordo) {
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