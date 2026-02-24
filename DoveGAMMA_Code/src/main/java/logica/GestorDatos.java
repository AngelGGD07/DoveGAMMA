package logica;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GestorDatos {
    private static final String ARCHIVO_DB = "rutas_db.json";
    private Gson gson = new Gson();

    public void guardarRutas(List<Ruta> rutas) {
        try (Writer writer = new FileWriter(ARCHIVO_DB)) {
            gson.toJson(rutas, writer);
            System.out.println("Datos guardados correctamente en " + ARCHIVO_DB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Ruta> cargarRutas() {
        try (Reader reader = new FileReader(ARCHIVO_DB)) {
            Type tipoLista = new TypeToken<ArrayList<Ruta>>() {
            }.getType();
            List<Ruta> rutas = gson.fromJson(reader, tipoLista);
            return rutas != null ? rutas : new ArrayList<>();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}