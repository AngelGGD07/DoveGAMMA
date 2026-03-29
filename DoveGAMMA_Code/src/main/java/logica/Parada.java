package logica;
/*
 * Clase: Parada
 * Objetivo: Representar un nodo (vértice) dentro de la red de transporte (Grafo).
 * Representa una estación física o parada de de transporte en el
 * mundo real. Sirve como los puntos de conexión (origen y destino) para las rutas. */
public class Parada {
    private String codigo;
    private String nombre;
    private double x;
    private double y;


    public Parada(String codigo, String nombre, double x, double y) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.x = x;
        this.y = y;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
    public double getY() { return y; }

    public void setNombre(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}