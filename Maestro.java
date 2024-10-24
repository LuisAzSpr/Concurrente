package org.example;

import org.example.Handler.ClientHandler;
import org.example.Handler.ClientModeloHandler;
import org.example.Handler.NodoHandler;
import org.example.utils.Libro;
import org.example.utils.NeuronalNetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maestro {
    private static final HashMap<String,Integer> puertos = new HashMap<>(){{
        put("CLIENTE_LIBRO",5000);
        put("NODO",30100);
        put("CLIENTE_MODELOS",6000);
    }};

    public static List<NodoHandler> nodosHandlers = new ArrayList<>();
    public static Map<Libro,NeuronalNetwork> mejorModeloDeLibro = new HashMap<>();
    public static int clientes = 0;

    public static void main(String[] args){
        new Thread(Maestro::escucharClientes).start();
        new Thread(Maestro::escucharNodos).start();
        new Thread(Maestro::escucharClientesModelo).start();
    }

    private static void escucharClientesModelo() {
        try (ServerSocket serverSocket = new ServerSocket(puertos.get("CLIENTE_MODELOS"))) {
            System.out.println("Maestro esperando conexión de clientes modelo...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Conexión de cliente modelo recibida.");
                ClientModeloHandler clientModeloHandler = new ClientModeloHandler(socket);
                new Thread(clientModeloHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void escucharNodos(){
        int numNodos = 0;
        try (ServerSocket serverSocket = new ServerSocket(puertos.get("NODO"))) {
            System.out.println("Maestro esperando conexión de nodos...");
            while (true) {
                Socket nodoSocket = serverSocket.accept();
                numNodos+=1;
                System.out.println("Conexión de nodo "+ numNodos +" recibida ");
                NodoHandler nodoHandler = new NodoHandler(nodoSocket,numNodos);
                nodosHandlers.add(nodoHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void escucharClientes() {
        try(ServerSocket serverSocket = new ServerSocket(puertos.get("CLIENTE_LIBRO"))){
            System.out.println("Maestro esperando conexion de clientes");
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexion con cliente libro establecida");
                ClientHandler clientHandler = new ClientHandler(clientSocket,nodosHandlers);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}