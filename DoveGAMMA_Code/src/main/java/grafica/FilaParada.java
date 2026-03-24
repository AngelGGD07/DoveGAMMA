package grafica;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FilaParada {

    private StringProperty id;
    private StringProperty nombre;
    private StringProperty x;
    private StringProperty y;

    public FilaParada(String id, String nombre, double x, double y) {
        this.id      = new SimpleStringProperty(id);
        this.nombre  = new SimpleStringProperty(nombre);
        this.x       = new SimpleStringProperty(String.format("%.1f", x));
        this.y       = new SimpleStringProperty(String.format("%.1f", y));
    }

    public StringProperty idProperty()     { return id; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty xProperty()      { return x; }
    public StringProperty yProperty()      { return y; }

    public String getId()     { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public String getY()      { return y.get(); }
}
