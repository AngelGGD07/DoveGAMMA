package logica;

public class Parada {
    private String codigo;
    private String nombre;

    public Parada(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nuevoNombre) {
    }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}