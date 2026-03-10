package logica;

public abstract class MedioTransporte {
    protected int capacidad;
    protected String estado;
    protected int id;

    public MedioTransporte(int capacidad, String estado, int id) {
        this.capacidad = capacidad;
        this.estado = estado;
        this.id = id;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
