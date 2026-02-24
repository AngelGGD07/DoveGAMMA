package logica;

public class Metro extends MedioTransporte{
    private boolean esAutomatico;
    private float longitudMetros;

    public Metro(int capacidad, String estado, int id, boolean esAutomatico, float longitudMetros) {
        super(capacidad, estado, id);
        this.esAutomatico = esAutomatico;
        this.longitudMetros = longitudMetros;
    }

    public boolean isEsAutomatico() {
        return esAutomatico;
    }

    public void setEsAutomatico(boolean esAutomatico) {
        this.esAutomatico = esAutomatico;
    }

    public float getLongitudMetros() {
        return longitudMetros;
    }

    public void setLongitudMetros(float longitudMetros) {
        this.longitudMetros = longitudMetros;
    }
}
