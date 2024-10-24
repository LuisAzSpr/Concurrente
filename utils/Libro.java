package org.example.utils;

public class Libro {
    public String contenido;
    public boolean entrenado;
    public String titulo;
    public static int cantidad = 0;

    public Libro(String contenido,String titulo){
        System.out.println("Contenido del libro: "+contenido);
        this.contenido = contenido;
        this.titulo = titulo;
        entrenado = false;
        cantidad += 1 ;
    }

    public String[][] dividirLibro(int nodos){ // divide el libro en train y test
        String[][] dataDividiaActual = new String[nodos][2];
        int division = (int) (contenido.length() * 0.9);
        String train = contenido.substring(0, division);  // Copiar el 90% inicial al train
        String test  = contenido.substring(division); // copiar el 10% restante

        int longitudPorNodo = division / nodos;
        //int resto = division % numNodos;

        for(int i=0;i<nodos;i++){
            int inicio = i*longitudPorNodo;
            int fin = (i==nodos-1)?division:inicio+longitudPorNodo;
            String parteNodo = train.substring(inicio,fin);
            dataDividiaActual[i][0] = parteNodo;
            dataDividiaActual[i][1] = test;
        }
        return dataDividiaActual;
    }

}
