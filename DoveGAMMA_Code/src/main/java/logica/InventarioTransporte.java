package logica;

import java.util.ArrayList;
import java.util.List;

public class InventarioTransporte {
    private List <MedioTransporte> listaVehiculos;

    public InventarioTransporte(List listaVehiculos) {
       listaVehiculos = new ArrayList<>();
    }

    public void aggTrans(MedioTransporte vehi){
        listaVehiculos.add(vehi);
    }

    public MedioTransporte buscarTrans(int idBuscado){
        int i = 0;
        boolean encontrado = false;
        MedioTransporte medioEncontrado = null;

        while(!encontrado && i < listaVehiculos.size()){

            MedioTransporte vehiActual = listaVehiculos.get(i);

            if(vehiActual.getId() == idBuscado){
                encontrado = true;
                medioEncontrado = vehiActual;
            }
            i++;
        }
        return medioEncontrado;

    }
    public boolean eliminarTrans(int idEliminar){
        MedioTransporte vehiculo = buscarTrans(idEliminar);
        if(vehiculo != null){
            listaVehiculos.remove(vehiculo);
            return true;
        }
        return false;


    }
    public boolean modificarTrans(int idModificar, int newCapacidad, String newEstado){
        MedioTransporte vehiculoEncontrado = buscarTrans(idModificar);

        if(vehiculoEncontrado != null){
            vehiculoEncontrado.setCapacidad(newCapacidad);
            vehiculoEncontrado.setEstado(newEstado);

            return true;
        }
        return false;
    }
}
