package logica;

public class DatoCamino implements Comparable<DatoCamino> {
    String idParada;
    double pesoAcumulado;

    public DatoCamino(String idParada, double pesoAcumulado) {
        this.idParada = idParada;
        this.pesoAcumulado = pesoAcumulado;
    }

    @Override
    public int compareTo(DatoCamino otro) {
        return Double.compare(this.pesoAcumulado, otro.pesoAcumulado);
    }
}