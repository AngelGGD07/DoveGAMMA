package grafica;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FilaRuta {

    private StringProperty origen;
    private StringProperty destino;
    private StringProperty tiempo;
    private StringProperty distancia;
    private StringProperty costo;
    private StringProperty transbordo; // ya no es boolean, es la cantidad

    public FilaRuta(String origen, String destino,
                    double tiempo, double distancia, double costo,
                    int transbordo) {
        this.origen     = new SimpleStringProperty(origen);
        this.destino    = new SimpleStringProperty(destino);
        this.tiempo     = new SimpleStringProperty(String.format("%.1f", tiempo));
        this.distancia  = new SimpleStringProperty(String.format("%.1f", distancia));
        this.costo      = new SimpleStringProperty(String.format("%.2f", costo));
        this.transbordo = new SimpleStringProperty(String.valueOf(transbordo));
    }

    public StringProperty origenProperty()     { return origen; }
    public StringProperty destinoProperty()    { return destino; }
    public StringProperty tiempoProperty()     { return tiempo; }
    public StringProperty distanciaProperty()  { return distancia; }
    public StringProperty costoProperty()      { return costo; }
    public StringProperty transbordoProperty() { return transbordo; }

    public String getOrigen()    { return origen.get(); }
    public String getDestino()   { return destino.get(); }
    public String getTiempo()    { return tiempo.get(); }
    public String getDistancia() { return distancia.get(); }
    public String getCosto()     { return costo.get(); }
    public int    getTransbordo(){ return Integer.parseInt(transbordo.get()); }
}