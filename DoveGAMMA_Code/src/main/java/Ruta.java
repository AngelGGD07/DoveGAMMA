import java.util.List;

public class Ruta {
    private String nombre;
    private List<String> paradas;

    public Ruta(String nombre, List<String> paradas) {
        this.nombre = nombre;
        this.paradas = paradas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getParadas() {
        return paradas;
    }

    public void setParadas(List<String> paradas) {
        this.paradas = paradas;
    }

    @Override
    public String toString() {
        return "Ruta: " + nombre + " - Paradas: " + paradas;
    }
}