package logica.transporte;

public abstract class VehiculoTransporte {

    protected String nombre;
    protected double factorPico;
    protected double factorLluvia;

    public String getNombre()       { return nombre; }
    public double getFactorPico()   { return factorPico; }
    public double getFactorLluvia() { return factorLluvia; }

    // color hex del badge y borde de la card
    public abstract String getColorHex();

    /*
       Función: getDescripcionImpacto
       Argumentos: ninguno
       Objetivo: Retornar texto corto describiendo el impacto de condiciones en este vehículo
       Retorno: (String): descripción del impacto
    */
    public abstract String getDescripcionImpacto();
}