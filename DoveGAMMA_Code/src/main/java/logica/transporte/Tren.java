package logica.transporte;

public class Tren extends VehiculoTransporte {

    public Tren() {
        this.nombre       = "Tren";
        this.factorPico   = 0.05;
        this.factorLluvia = 0.0;
    }

    @Override public String getAbreviatura()        { return "TR"; }
    @Override public String getColorHex()           { return "#6090f0"; }

    /*
       Función: getDescripcionImpacto
       Argumentos: ninguno
       Objetivo: Describir cómo le afectan las condiciones al tren
       Retorno: (String): texto del impacto
    */
    @Override
    public String getDescripcionImpacto() {
        return "Tráfico: sin efecto.  Clima: sin efecto.";
    }
}