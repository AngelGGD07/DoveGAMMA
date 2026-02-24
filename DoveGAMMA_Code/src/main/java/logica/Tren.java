package logica;

public class Tren extends MedioTransporte{
    private int cantVagones;
    private String claseServicio; // economica, primera clase

    public Tren(int capacidad, String estado, int id, int cantVagones, String claseServicio) {
        super(capacidad, estado, id);
        this.cantVagones = cantVagones;
        this.claseServicio = claseServicio;
    }

    public int getCantVagones() {
        return cantVagones;
    }

    public void setCantVagones(int cantVagones) {
        this.cantVagones = cantVagones;
    }

    public String getClaseServicio() {
        return claseServicio;
    }

    public void setClaseServicio(String claseServicio) {
        this.claseServicio = claseServicio;
    }
}
