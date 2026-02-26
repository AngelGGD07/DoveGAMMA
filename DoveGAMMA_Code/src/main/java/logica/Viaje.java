package logica;

import java.time.LocalDateTime;

public class Viaje {
    private MedioTransporte transporte;
    private Ruta rutica;
    private LocalDateTime horario;

    public Viaje(MedioTransporte transporte, Ruta rutica, LocalDateTime horario) {
        this.transporte = transporte;
        this.rutica = rutica;
        this.horario = horario;
    }

    public MedioTransporte getTransporte() {
        return transporte;
    }

    public void setTransporte(MedioTransporte transporte) {
        this.transporte = transporte;
    }

    public Ruta getRutica() {
        return rutica;
    }

    public void setRutica(Ruta rutica) {
        this.rutica = rutica;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public void setHorario(LocalDateTime horario) {
        this.horario = horario;
    }

    public double calcularRecaudacionMaxima(){
        return rutica.getCosto() * transporte.getCapacidad();
    }
}
