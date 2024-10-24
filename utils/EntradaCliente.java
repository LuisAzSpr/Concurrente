package org.example.utils;

import java.io.Serializable;

public class EntradaCliente implements Serializable {
    public String nombreLibro;
    public double[] entrada;
    public EntradaCliente(String nombreLibro,double[] entrada){
        this.nombreLibro = nombreLibro;
        this.entrada = entrada;
    }
}
