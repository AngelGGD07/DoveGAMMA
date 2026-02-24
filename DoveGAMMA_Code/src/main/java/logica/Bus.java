package logica;

public class Bus extends MedioTransporte{
    private String placa;

    public Bus(int capacidad, String estado, int id, String placa) {
        super(capacidad, estado, id);
        this.placa = placa;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }


}
