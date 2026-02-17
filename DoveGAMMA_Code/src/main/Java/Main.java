import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        GestorDatos gestor = new GestorDatos();

        List<Ruta> misRutas = gestor.cargarRutas();
        System.out.println("Rutas cargadas al iniciar: " + misRutas);

        Ruta nuevaRuta = new Ruta("Ruta 66", Arrays.asList("Parada A", "Parada B"));
        misRutas.add(nuevaRuta);

        gestor.guardarRutas(misRutas);
    }
}