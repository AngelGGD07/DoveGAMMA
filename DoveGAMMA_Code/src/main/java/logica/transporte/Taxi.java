package logica.transporte;

public class Taxi extends VehiculoTransporte {

    public Taxi() {
        this.nombre       = "Taxi";
        this.factorPico   = 0.50;
        this.factorLluvia = 0.25;
    }

    @Override public String getAbreviatura()        { return "TX"; }
    @Override public String getColorHex()           { return "#e09040"; }

    /*
       Función: getDescripcionImpacto
       Argumentos: ninguno
       Objetivo: Describir cómo le afectan las condiciones al taxi
       Retorno: (String): texto del impacto
    */
    @Override
    public String getDescripcionImpacto() {
        return "Hora pico: alto congestionamiento.  Lluvia: tiempo incrementa.";
    }
}