package logica;

/*
 * Clase auxiliar para gestionar la Cola de Prioridad en los algoritmos.
 * Permite ordenar los nodos a explorar basándose en su peso acumulado.
 */
public class DatoCamino implements Comparable<DatoCamino> {
    String idParada;
    double pesoAcumulado;

    public DatoCamino(String idParada, double pesoAcumulado) {
        this.idParada = idParada;
        this.pesoAcumulado = pesoAcumulado;
    }

    public String getIdParada() {
        return idParada;
    }

    @Override
    public int compareTo(DatoCamino otro) {
        return Double.compare(this.pesoAcumulado, otro.pesoAcumulado);
    }
}