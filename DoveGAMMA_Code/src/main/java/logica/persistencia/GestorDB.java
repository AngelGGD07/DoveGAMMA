package logica.persistencia;

import java.sql.*;

public class GestorDB {

    private static final String URL  = "jdbc:mysql://localhost:3306/dovegamma";
    private static final String USER = "dove";
    private static final String PASS = "gamma123";

    private Connection conexion;

    public GestorDB() {
        conectar();
        crearTablas();
    }

    private void conectar() {
        try {
            conexion = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Base de datos MySQL lista.");
        } catch (SQLException e) {
            System.out.println("Error conectando a la BD: " + e.getMessage());
        }
    }

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
                        "  origen    VARCHAR(50) NOT NULL," +
                        "  destino   VARCHAR(50) NOT NULL," +
                        "  tiempo    DOUBLE NOT NULL," +
                        "  distancia DOUBLE NOT NULL," +
                        "  costo     DOUBLE NOT NULL," +
                        "  PRIMARY KEY (origen, destino)" +
                        ");";

        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sqlParadas);
            stmt.execute(sqlRutas);
        } catch (SQLException e) {
            System.out.println("Error creando tablas: " + e.getMessage());
        }
    }

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

    public void eliminarParada(String id) {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM paradas WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error eliminando parada: " + e.getMessage());
        }
    }

    public ResultSet cargarParadas() throws SQLException {
        return conexion.createStatement().executeQuery("SELECT * FROM paradas");
    }

    public void guardarRuta(String origen, String destino,
                            double tiempo, double distancia, double costo) {
        String sql = "INSERT INTO rutas (origen, destino, tiempo, distancia, costo) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE tiempo=VALUES(tiempo), distancia=VALUES(distancia), costo=VALUES(costo)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, origen);
            ps.setString(2, destino);
            ps.setDouble(3, tiempo);
            ps.setDouble(4, distancia);
            ps.setDouble(5, costo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error guardando ruta: " + e.getMessage());
        }
    }

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

    public ResultSet cargarRutas() throws SQLException {
        return conexion.createStatement().executeQuery("SELECT * FROM rutas");
    }

    public void cerrar() {
        try {
            if (conexion != null) conexion.close();
        } catch (SQLException e) {
            System.out.println("Error cerrando BD: " + e.getMessage());
        }
    }
}