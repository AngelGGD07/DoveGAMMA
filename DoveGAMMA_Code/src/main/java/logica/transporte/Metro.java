package logica.transporte;

public class Metro extends VehiculoTransporte {

    public Metro() {
        this.nombre       = "Metro";
        this.factorPico   = 0.15;
        this.factorLluvia = 0.0;
    }

    @Override public String getAbreviatura()        { return "M"; }
    @Override public String getColorHex()           { return "#a060f0"; }

    /*
       Función: getDescripcionImpacto
       Argumentos: ninguno
       Objetivo: Describir cómo le afectan las condiciones al metro
       Retorno: (String): texto del impacto
    */
    @Override
    public String getDescripcionImpacto() {
        return "Hora pico: estaciones congestionadas.  Clima: sin efecto.";
    }
}