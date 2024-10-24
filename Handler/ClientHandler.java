package org.example.Handler;

import org.example.ClientsNode.Nodo;
import org.example.Maestro;
import org.example.utils.Libro;
import org.example.utils.NeuronalNetwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{

    public Socket socket;
    public Libro libro;
    public List<NodoHandler> nodosHandlers;
    public BufferedReader in;
    public PrintWriter out;

    public ClientHandler(Socket socket, List<NodoHandler> nodoHandlers){
        this.socket = socket;
        this.nodosHandlers = nodoHandlers;
    }


    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Se crea un buffer de lectura
            out = new PrintWriter(socket.getOutputStream(), true);
            String inputline;
            String titulo = "";
            String contenido = "";
            while ((inputline = in.readLine()) != null) { // Se lee un mensaje del cliente
                if(inputline.startsWith("Titulo:")){
                    titulo = inputline.substring(7);
                }
                if(inputline.startsWith("Contenido:")){
                    contenido = inputline.substring(10);
                }
                if(!titulo.isEmpty() && !contenido.isEmpty()){
                    libro = new Libro(contenido,titulo);
                    Maestro.clientes += 1;
                    break;
                }
            }
            if(nodosHandlers.size()==0){
                String[][] libroDividido = libro.dividirLibro(1);
                Nodo nodo = new Nodo();
                NeuronalNetwork bestModel = nodo.train(libroDividido[0][0],libroDividido[0][1]);
                Maestro.mejorModeloDeLibro.put(libro,bestModel);
                System.out.println("El error de haber procesado en el Maestro es : "+bestModel.costo);
                return;
            }
            String[][] libroDividido = libro.dividirLibro(nodosHandlers.size());
            System.out.println("Divide a los libros exitosamente");

            double startTime = System.nanoTime();  // Captura el tiempo al inicio
            List<Thread> nodoThreads = new ArrayList<>();
            for (int i = 0; i < nodosHandlers.size(); i++) {
                NodoHandler nodoHandler = nodosHandlers.get(i);
                nodoHandler.llenarData(libroDividido[i][0],libroDividido[i][1],libro);
                Thread nodoThread = new Thread(nodoHandler);
                nodoThreads.add(nodoThread);
                nodoThread.start();
            }
            for (Thread nodoThread : nodoThreads) {
                try {
                    nodoThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<NeuronalNetwork> modelos = new ArrayList<>();
            for(NodoHandler nodoHandler:nodosHandlers){
                modelos.add(nodoHandler.mejorModelo);
            }
            NeuronalNetwork nn = escogerMejorModelo(modelos);
            double endTime = System.nanoTime();  // Captura el tiempo al finalizar
            double duration = (endTime - startTime) / 1_000_000_000;
            System.out.println("Duracion de haber entrenado "+duration+" segundos");
            Maestro.mejorModeloDeLibro.put(libro,nn);
            out.println("EXITO, Se entreno con exito!!");

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
            out.println("ERROR, error al entrenar : "+e.getMessage());
        } catch (Exception e) {
            out.println("ERROR, error al entrenar : "+e.getMessage());
            throw new RuntimeException(e);
        } finally { // Se cierra la conexión con el cliente
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private NeuronalNetwork escogerMejorModelo(List<NeuronalNetwork> modelos){
        NeuronalNetwork mejorModelo = modelos.get(0);
        for(int i=0;i<modelos.size();i++){
            NeuronalNetwork modelo = modelos.get(i);
            System.out.println("Modelo con coste: "+modelo.costo);
            if(modelo.costo<mejorModelo.costo){
                mejorModelo = modelo;
            }
        }
        System.out.println("------------------------------------");
        System.out.println("El mejor modelo es el de coste: "+mejorModelo.costo);

        return mejorModelo;
    }

}

