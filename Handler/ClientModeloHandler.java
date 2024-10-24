package org.example.Handler;

import org.example.Maestro;
import org.example.utils.EntradaCliente;
import org.example.utils.Libro;
import org.example.utils.NeuronalNetwork;

import java.io.*;
import java.net.Socket;

public class ClientModeloHandler implements Runnable{

    public Socket socket;

    public ClientModeloHandler(Socket socket){
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); // recibe objetos
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // manda objetos
            EntradaCliente entradaCliente = (EntradaCliente) ois.readObject();
            NeuronalNetwork nn = encontrarModeloPorTitulo(entradaCliente.nombreLibro);
            double[] salida = new double[36];
            if(nn!=null){
                salida = nn.feedForward(entradaCliente.entrada);
            }
            oos.writeObject(salida);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static NeuronalNetwork encontrarModeloPorTitulo(String titulo){
        for(Libro libro:Maestro.mejorModeloDeLibro.keySet()){
            if(libro.titulo.equals(titulo)){
                return Maestro.mejorModeloDeLibro.get(libro);
            }
        }
        System.out.println("NO se encontro el libro");
        return null;
    }




}
