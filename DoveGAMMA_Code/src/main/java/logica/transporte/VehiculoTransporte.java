package logica.transporte;

/*
 * Clase: VehiculoTransporte
 * Objetivo: Servir como clase base para todos los modos
 * de transporte del sistema (Metro, Taxi, Tren, Teleférico). Define los
 * atributos comunes, mediante polimorfismo, para que el SimuladorCondiciones
 * pueda calcular las penalizaciones de tiempo sin importar qué vehículo se elija.*/
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