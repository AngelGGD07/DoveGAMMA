package logica.transporte;

import java.time.LocalTime;
import java.util.Random;
/*
 * Clase: SimuladorCondiciones
 * Objetivo: Añadir una capa de complejidad y realismo dinámico a la red de transporte.
 * Simula variaciones ambientales (clima) y temporales (tráfico/hora pico) que alteran
 * matemáticamente los pesos de las aristas del grafo en tiempo real.
 */
public class SimuladorCondiciones {

    public enum Clima {
        SOLEADO ("Soleado",   0.0,  "#e09040"),
        NUBLADO ("Nublado",   0.05, "#8888a0"),
        LLUVIOSO("Lluvioso",  0.20, "#6090c0"),
        TORMENTA("Tormenta",  0.45, "#6040a0");

        private final String descripcion;
        private final double factorExtra;
        private final String colorHex;

        Clima(String descripcion, double factorExtra, String colorHex) {
            this.descripcion = descripcion;
            this.factorExtra = factorExtra;
            this.colorHex    = colorHex;
        }

        public String getDescripcion() { return descripcion; }
        public double getFactorExtra() { return factorExtra; }
        public String getColorHex()    { return colorHex; }
    }

    private final Clima climaActual;

    /*
       Función: SimuladorCondiciones (Constructor)
       Argumentos: ninguno
       Objetivo: Inicializar el simulador generando un clima pseudoaleatorio.
                 Utiliza una semilla matemática basada en el reloj del sistema.
    */
    public SimuladorCondiciones() {
        // seed por minuto → el clima cambia cada minuto, no cada segundo
        long semilla = System.currentTimeMillis() / 60000;
        Random rng   = new Random(semilla);
        int dado     = rng.nextInt(10);

        if      (dado < 5) climaActual = Clima.SOLEADO;
        else if (dado < 7) climaActual = Clima.NUBLADO;
        else if (dado < 9) climaActual = Clima.LLUVIOSO;
        else               climaActual = Clima.TORMENTA;
    }

    public Clima getClima() { return climaActual; }

    /*
       Función: esHoraPico
       Argumentos: ninguno
       Objetivo: Detectar si la hora actual cae en hora pico (mañana 7-9:30 o tarde 17-19:30)
       Retorno: (boolean): true si hay hora pico
    */
    public boolean esHoraPico() {
        LocalTime ahora = LocalTime.now();
        boolean manana  = ahora.isAfter(LocalTime.of(7, 0))  && ahora.isBefore(LocalTime.of(9, 30));
        boolean tarde   = ahora.isAfter(LocalTime.of(17, 0)) && ahora.isBefore(LocalTime.of(19, 30));
        return manana || tarde;
    }

    /*
       Función: getDescripcionHorario
       Argumentos: ninguno
       Objetivo: Analizar la hora del sistema y devolver una descripción textual
                 del estado del tráfico general para la interfaz de usuario.
       Retorno: (String): "Hora pico", "Noche tranquila" o "Flujo normal".
    */
    public String getDescripcionHorario() {
        if (esHoraPico()) return "Hora pico";
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(LocalTime.of(6, 0)) || ahora.isAfter(LocalTime.of(22, 0))) {
            return "Noche tranquila";
        }
        return "Flujo normal";
    }

    // color del dot de tráfico para el panel visual
    public String getColorHorario() {
        if (esHoraPico()) return "#e07070";
        LocalTime ahora = LocalTime.now();
        if (ahora.isBefore(LocalTime.of(6, 0)) || ahora.isAfter(LocalTime.of(22, 0))) {
            return "#7090c0";
        }
        return "#70c070";
    }

    /*
       Función: esTormentaConTeleferico
       Argumentos: (VehiculoTransporte) vehiculo: el vehículo elegido
       Objetivo: Verificar si hay tormenta y el vehículo es teleférico (servicio suspendido)
       Retorno: (boolean): true si el servicio no está disponible
    */
    public boolean esTormentaConTeleferico(VehiculoTransporte vehiculo) {
        return climaActual == Clima.TORMENTA && vehiculo instanceof Teleferico;
    }

    /*
       Función: getFactorTotal
       Argumentos: (VehiculoTransporte) vehiculo: el vehículo elegido por el usuario
       Objetivo: Calcular el multiplicador total del tiempo según vehículo + hora + clima
       Retorno: (double): multiplicador (1.0 = sin cambio, 1.5 = +50% al tiempo)
    */
    public double getFactorTotal(VehiculoTransporte vehiculo) {
        double factor = 1.0;
        if (esHoraPico()) factor += vehiculo.getFactorPico();
        factor += vehiculo.getFactorLluvia() * climaActual.getFactorExtra();
        return factor;
    }
}