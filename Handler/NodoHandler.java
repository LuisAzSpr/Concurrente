package org.example.Handler;

import org.example.Maestro;
import org.example.utils.Data;
import org.example.utils.Libro;
import org.example.utils.NeuronalNetwork;

import java.io.*;
import java.net.Socket;

public class NodoHandler implements Runnable {
    public Socket socket;
    public int id = 0;

    public Data data;
    public NeuronalNetwork mejorModelo;

    public ObjectOutputStream oos;
    public ObjectInputStream ois;


    public NodoHandler(Socket socket,int id) throws IOException {
        this.socket = socket;
        this.id = id;
        oos = new ObjectOutputStream(socket.getOutputStream()); // manda objetos
        ois = new ObjectInputStream(socket.getInputStream()); // recibe objetos`
    }

    public void llenarData(String train,String test,Libro libro){
        data = new Data(train,test);
    }

    @Override
    public void run(){
        try {
            oos.writeObject(data); // Envia la data al nodo
            oos.flush();
            NeuronalNetwork nn = (NeuronalNetwork) ois.readObject(); // Recibe y deserializa el modelo
            System.out.println("Red Neuronal recibida y deserializada con éxito.");
            mejorModelo = nn;
        } catch (EOFException e) {
            System.err.println("EOFException: El flujo se cerró inesperadamente.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: Clase no encontrada al deserializar.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: Error de entrada/salida.");
            e.printStackTrace();
        }
    }



}
