package logica;

import java.util.List;

public class Ruta {
    private String idOrigen;
    private String idDestino;
    private double tiempo;    // Minutos
    private double costo;     // Dinero
    private double distancia; // Kilómetros

    public Ruta(String idOrigen, String idDestino, double tiempo, double costo, double distancia) {
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.tiempo = tiempo;
        this.costo = costo;
        this.distancia = distancia;
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
}