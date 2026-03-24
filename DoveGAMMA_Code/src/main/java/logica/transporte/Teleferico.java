package logica.transporte;

public class Teleferico extends VehiculoTransporte {

    public Teleferico() {
        this.nombre       = "Teleférico";
        this.factorPico   = 0.0;
        this.factorLluvia = 0.6;
    }

    @Override public String getAbreviatura()        { return "TF"; }
    @Override public String getColorHex()           { return "#40b09a"; }

    /*
       Función: getDescripcionImpacto
       Argumentos: ninguno
       Objetivo: Describir cómo le afectan las condiciones al teleférico
       Retorno: (String): texto del impacto
    */
    @Override
    public String getDescripcionImpacto() {
        return "Tráfico: sin efecto.  Tormenta: servicio suspendido.";
    }
}