package logica;

/*
 Clase: DatoCamino
 Objetivo: Estructura auxiliar utilizada por algoritmos
 de búsqueda (como Dijkstra) para gestionar los nodos en una
 PriorityQueue. Empaqueta el identificador de un nodo junto con el costo necesario
 para llegar a él.
 Implementa Comparable para permitir que la PriorityQueue ordene los nodos
 automáticamente de menor a mayor peso.
 */
public class DatoCamino implements Comparable<DatoCamino> {
    String idParada;
    double pesoAcumulado;

    public DatoCamino(String idParada, double pesoAcumulado) {
        this.idParada = idParada;
        this.pesoAcumulado = pesoAcumulado;
    }

    public String getIdParada() {
        return idParada;
    }

    /*
       Función: compareTo
       Argumentos: (DatoCamino) otro: el objeto contra el cual se va a comparar.
       Objetivo: Sobrescribir el metodo de la interfaz Comparable. Define la lógica de
                 ordenamiento de la PriorityQueue: el nodo con el menor pesoAcumulado
                 tendrá la mayor prioridad.
       Retorno: (int): número negativo si este objeto es menor, 0 si son iguales,
                positivo si es mayor.
    */
    @Override
    public int compareTo(DatoCamino otro) {
        return Double.compare(this.pesoAcumulado, otro.pesoAcumulado);
    }
}