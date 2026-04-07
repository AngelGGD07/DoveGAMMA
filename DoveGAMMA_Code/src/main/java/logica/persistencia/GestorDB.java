package logica.persistencia;

import java.sql.*;

/*
 Clase: GestorDB
 Objetivo: Manejar la capa de persistencia de datos del sistema conectando la
 aplicación Java con una base de datos relacional (MySQL) mediante JDBC.
 Garantiza que las paradas y rutas creadas por el usuario no se pierdan
 al cerrar la aplicación.
 */
public class GestorDB {

    // Credenciales de conexión a la base de datos local
    private static final String URL  = "jdbc:mysql://localhost:3306/dovegamma";
    private static final String USER = "dove";
    private static final String PASS = "gamma123";

    private Connection conexion;

    public GestorDB() {
        conectar();
        crearTablas();
    }

    /*
       Función: conectar
       Argumentos: ninguno
       Objetivo: Establecer el canal de comunicación (Connection) con MySQL
                 utilizando el driver JDBC y las credenciales definidas.
       Retorno: void
    */
    private void conectar() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Base de datos MySQL lista.");
        } catch (SQLException e) {
            System.out.println("Error conectando a la BD: " + e.getMessage());
        }
    }

    /*
       Función: crearTablas
       Argumentos: ninguno
       Objetivo: Ejecutar sentencias DDL (CREATE TABLE IF NOT EXISTS) para asegurar
                 que la base de datos tenga las tablas 'paradas' y 'rutas' con la
                 estructura y llaves primarias correctas.
       Retorno: void
    */
    private void crearTablas() {
        String sqlParadas =
                "CREATE TABLE IF NOT EXISTS paradas (" +
                        "  id      VARCHAR(50) PRIMARY KEY," +
                        "  nombre  VARCHAR(100) NOT NULL," +
                        "  x       DOUBLE NOT NULL," +
                        "  y       DOUBLE NOT NULL" +
                        ");";

        String sqlRutas =
                "CREATE TABLE IF NOT EXISTS rutas (" +
                        "  origen VARCHAR(50)," +
                        "  destino VARCHAR(50)," +
                        "  tiempo DOUBLE," +
                        "  distancia DOUBLE," +
                        "  costo DOUBLE," +
                        "  transbordo INT," +
                        "  PRIMARY KEY(origen, destino)" +
                        ");";

        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sqlParadas);
            stmt.execute(sqlRutas);
        } catch (SQLException e) {
            System.out.println("Error creando tablas: " + e.getMessage());
        }
    }

    /*
       Función: guardarParada
       Argumentos: (String) id, (String) nombre, (double) x, (double) y
       Objetivo: Insertar una nueva parada en la base de datos. Si el 'id' ya existe,
                 se ejecuta un UPDATE automático actualizando su nombre y coordenadas.
       Retorno: void
    */
    public void guardarParada(String id, String nombre, double x, double y) {
        String sql = "INSERT INTO paradas (id, nombre, x, y) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), x=VALUES(x), y=VALUES(y)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, nombre);
            ps.setDouble(3, x);
            ps.setDouble(4, y);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error guardando parada: " + e.getMessage());
        }
    }

    /*
       Función: eliminarParada
       Argumentos: (String) id: identificador de la parada.
       Objetivo: Borrar permanentemente el registro de una parada en la base de datos.
       Retorno: void
    */
    public void eliminarParada(String id) {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM paradas WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error eliminando parada: " + e.getMessage());
        }
    }

    /*
       Función: cargarParadas
       Argumentos: ninguno
       Objetivo: Consultar todos los registros de la tabla 'paradas'.
       Retorno: (ResultSet): Objeto iterable con las filas devueltas por MySQL.
    */
    public ResultSet cargarParadas() throws SQLException {
        return conexion.createStatement().executeQuery("SELECT * FROM paradas");
    }

    /*
       Función: guardarRuta
       Argumentos: (String) origen, (String) destino, (double) tiempo,
                   (double) distancia, (double) costo, (int) transbordo
       Objetivo: Insertar una nueva ruta en la base de datos. Si la llave primaria
                 compuesta (origen, destino) ya existe, actualiza sus pesos y atributos.
       Retorno: void
    */
    public void guardarRuta(String origen, String destino,
                            double tiempo, double distancia, double costo, int transbordo) {

        String sql = "INSERT INTO rutas (origen, destino, tiempo, distancia, costo, transbordo) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE tiempo=VALUES(tiempo), distancia=VALUES(distancia), costo=VALUES(costo), transbordo=VALUES(transbordo)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, origen);
            ps.setString(2, destino);
            ps.setDouble(3, tiempo);
            ps.setDouble(4, distancia);
            ps.setDouble(5, costo);
            ps.setInt(6, transbordo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error guardando ruta: " + e.getMessage());
        }
    }

    /*
       Función: eliminarRuta
       Argumentos: (String) origen, (String) destino
       Objetivo: Borrar permanentemente una conexión (arista) de la base de datos
                 basándose en su llave primaria compuesta.
       Retorno: void
    */
    public void eliminarRuta(String origen, String destino) {
        String sql = "DELETE FROM rutas WHERE origen = ? AND destino = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, origen);
            ps.setString(2, destino);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error eliminando ruta: " + e.getMessage());
        }
    }

    /*
       Función: cargarRutas
       Argumentos: ninguno
       Objetivo: Consultar todos los registros de la tabla 'rutas'.
       Retorno: (ResultSet): Objeto iterable con las aristas almacenadas en MySQL.
    */
    public ResultSet cargarRutas() throws SQLException {
        return conexion.createStatement().executeQuery("SELECT * FROM rutas");
    }
}